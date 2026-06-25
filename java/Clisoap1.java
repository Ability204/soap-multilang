// javac Clisoap1.java && java Clisoap1
// http://localhost:9090/?n=10
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class Clisoap1 {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(9090), 0);
        server.createContext("/", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String n = "10";
            if (query != null) {
                for (String p : query.split("&")) {
                    String[] kv = p.split("=");
                    if (kv.length == 2 && kv[0].equals("n")) n = kv[1];
                }
            }

            String payload =
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soap:Body>" +
                "<NumberToWords xmlns=\"http://www.dataaccess.com/webservicesserver/\">" +
                "<ubiNum>" + n + "</ubiNum>" +
                "</NumberToWords></soap:Body></soap:Envelope>";

            URL url = new URL("https://www.dataaccess.com/webservicesserver/NumberConversion.wso");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            conn.setDoOutput(true);
            conn.getOutputStream().write(payload.getBytes("UTF-8"));

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document doc = factory.newDocumentBuilder().parse(conn.getInputStream());
            String result = doc.getElementsByTagNameNS(
                "http://www.dataaccess.com/webservicesserver/", "NumberToWordsResult")
                .item(0).getTextContent().trim();

            byte[] resp = result.getBytes("UTF-8");
            exchange.sendResponseHeaders(200, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.getResponseBody().close();
        });
        server.start();
        System.out.println("http://localhost:9090/?n=10");
    }
}
