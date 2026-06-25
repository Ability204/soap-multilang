// javac Clisoap2.java && java Clisoap2
// http://localhost:9090/?n=10
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class Clisoap2 {
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

            URL soapUrl = new URL("https://www.dataaccess.com/webservicesserver/NumberConversion.wso");
            HttpURLConnection conn = (HttpURLConnection) soapUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            conn.setDoOutput(true);
            conn.getOutputStream().write(payload.getBytes("UTF-8"));

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document doc = factory.newDocumentBuilder().parse(conn.getInputStream());
            String word = doc.getElementsByTagNameNS(
                "http://www.dataaccess.com/webservicesserver/", "NumberToWordsResult")
                .item(0).getTextContent().trim();

            String translateUrl = "https://translate.googleapis.com/translate_a/single" +
                "?client=gtx&sl=en&tl=es&dt=t&q=" + URLEncoder.encode(word, "UTF-8");
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(new URL(translateUrl).openStream(), "UTF-8"));
            String json = reader.readLine();
            reader.close();
            int start = json.indexOf("[[[\""") + 4;
            int end = json.indexOf("\"", start);
            String translated = json.substring(start, end);

            byte[] resp = translated.getBytes("UTF-8");
            exchange.sendResponseHeaders(200, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.getResponseBody().close();
        });
        server.start();
        System.out.println("http://localhost:9090/?n=10");
    }
}
