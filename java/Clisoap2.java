// javac Clisoap2.java && java Clisoap2
// http://localhost:9090/?n=10
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpResponse.BodyHandlers;

public class Clisoap2 {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(9090), 0);
        server.createContext("/", exchange -> {
            try {
                String query = exchange.getRequestURI().getQuery();
                String n = "10";
                if (query != null) {
                    for (String part : query.split("&")) {
                        String[] kv = part.split("=");
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

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.dataaccess.com/webservicesserver/NumberConversion.wso"))
                    .header("Content-Type", "text/xml; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

                HttpResponse<String> soapResp = client.send(req, BodyHandlers.ofString());
                String xml = soapResp.body();

                int s = xml.indexOf("NumberToWordsResult>") + "NumberToWordsResult>".length();
                int e = xml.indexOf("</", s);
                String word = (s > 20 && e > s) ? xml.substring(s, e).trim() : "";

                String translateUrl = "https://translate.googleapis.com/translate_a/single" +
                    "?client=gtx&sl=en&tl=es&dt=t&q=" + URLEncoder.encode(word, "UTF-8");
                HttpRequest tReq = HttpRequest.newBuilder()
                    .uri(URI.create(translateUrl))
                    .GET()
                    .build();
                HttpResponse<String> tResp = client.send(tReq, BodyHandlers.ofString());
                String json = tResp.body();

                int ts = json.indexOf("[[[\"") + 4;
                int te = json.indexOf("\"", ts);
                String translated = (ts >= 4 && te > ts) ? json.substring(ts, te) : word;

                byte[] bytes = translated.getBytes("UTF-8");
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
            } catch (Exception ex) {
                ex.printStackTrace();
                String msg = ex.toString();
                byte[] bytes = msg.getBytes("UTF-8");
                exchange.sendResponseHeaders(500, bytes.length);
                exchange.getResponseBody().write(bytes);
            }
            exchange.getResponseBody().close();
        });
        server.start();
        System.out.println("Servidor iniciado: http://localhost:9090/?n=10");
    }
}
