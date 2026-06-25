// javac Conintl.java && java Conintl
// http://localhost:9090/?n=10
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.*;
import java.text.RuleBasedNumberFormat;
import java.util.Locale;

public class Conintl {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(9090), 0);
        server.createContext("/", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            int n = 10;
            if (query != null) {
                for (String p : query.split("&")) {
                    String[] kv = p.split("=");
                    if (kv.length == 2 && kv[0].equals("n")) {
                        try { n = Integer.parseInt(kv[1]); } catch (Exception ignored) {}
                    }
                }
            }
            RuleBasedNumberFormat fmt = new RuleBasedNumberFormat(
                new Locale("es"), RuleBasedNumberFormat.SPELLOUT);
            String result = fmt.format(n);
            byte[] resp = result.getBytes("UTF-8");
            exchange.sendResponseHeaders(200, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.getResponseBody().close();
        });
        server.start();
        System.out.println("http://localhost:9090/?n=10");
    }
}
