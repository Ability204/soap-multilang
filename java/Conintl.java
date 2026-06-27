// javac Conintl.java && java Conintl
// http://localhost:9090/?n=10
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.*;

public class Conintl {

    static final String[] ONES = {
        "", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete",
        "ocho", "nueve", "diez", "once", "doce", "trece", "catorce",
        "quince", "dieciséis", "diecisiete", "dieciocho", "diecinueve"
    };
    static final String[] TENS = {
        "", "", "veinte", "treinta", "cuarenta", "cincuenta",
        "sesenta", "setenta", "ochenta", "noventa"
    };
    static final String[] HUNDREDS = {
        "", "ciento", "doscientos", "trescientos", "cuatrocientos",
        "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos"
    };

    static String toWordsES(int n) {
        if (n == 0) return "cero";
        if (n < 20)  return ONES[n];
        if (n < 100) {
            if (n % 10 == 0) return TENS[n / 10];
            return TENS[n / 10] + " y " + ONES[n % 10];
        }
        if (n < 1000) {
            if (n == 100) return "cien";
            int rem = n % 100;
            return rem == 0 ? HUNDREDS[n / 100] : HUNDREDS[n / 100] + " " + toWordsES(rem);
        }
        return String.valueOf(n);
    }

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
            String result = toWordsES(n);
            byte[] resp = result.getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(200, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.getResponseBody().close();
        });
        server.start();
        System.out.println("http://localhost:9090/?n=10");
    }
}
