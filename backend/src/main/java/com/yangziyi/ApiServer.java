package com.yangziyi;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP API front end for the Lab 3 calculator (Lab 7).
 *
 * The business logic is still the unchanged {@link Calculator} class from
 * Lab 3; this class only wraps it in a tiny HTTP server so that the web
 * frontend container can call it over the network. It uses the JDK built-in
 * com.sun.net.httpserver.HttpServer, so the pom.xml needs no new dependency.
 *
 * Endpoints (port 8000):
 *   GET /api/health                 -> {"status":"ok"}
 *   GET /api/eval?a=2&op=%2B&b=3    -> {"a":"2","op":"+","b":"3","result":4.0}
 *
 * The LOG_LEVEL environment variable controls request logging:
 *   INFO (default) - one log line per request, visible in docker compose logs
 *   OFF            - no request logging
 */
public class ApiServer {

    /** Port the API listens on; matches EXPOSE 8000 and the compose mapping. */
    private static final int PORT = 8000;

    private final Calculator calculator = new Calculator();
    private final boolean logRequests;

    public ApiServer(boolean logRequests) {
        this.logRequests = logRequests;
    }

    /**
     * Starts the HTTP server. Reads the LOG_LEVEL environment variable
     * (injected by docker-compose "environment:") to decide whether
     * requests are logged to stdout.
     */
    public static void main(String[] args) throws IOException {
        String level = System.getenv().getOrDefault("LOG_LEVEL", "INFO");
        boolean logging = !"OFF".equalsIgnoreCase(level.trim());
        new ApiServer(logging).start();
    }

    private void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        server.createContext("/api/health", this::handleHealth);
        server.createContext("/api/eval", this::handleEval);
        server.createContext("/", this::handleNotFound);
        server.start();
        log("INFO", "Calculator API listening on 0.0.0.0:" + PORT);
    }

    /** GET /api/health - simple liveness probe. */
    private void handleHealth(HttpExchange ex) throws IOException {
        log("INFO", ex.getRequestMethod() + " " + ex.getRequestURI().getPath());
        sendJson(ex, 200, "{\"status\":\"ok\"}");
    }

    /** GET /api/eval?a=..&op=..&b=.. - evaluates the expression and answers JSON. */
    private void handleEval(HttpExchange ex) throws IOException {
        Map<String, String> q = parseQuery(ex.getRequestURI().getRawQuery());
        String a = q.getOrDefault("a", "");
        String op = q.getOrDefault("op", "");
        String b = q.getOrDefault("b", "");
        log("INFO", "eval a=" + a + " op=" + op + " b=" + b);
        try {
            double result = calculator.eval(a, op, b);
            sendJson(ex, 200, jsonResult(a, op, b, Double.toString(result)));
        } catch (NumberFormatException e) {
            // must be caught BEFORE IllegalArgumentException,
            // because NumberFormatException extends it
            sendJson(ex, 400, jsonError("operands must be numbers"));
        } catch (IllegalArgumentException e) {
            String reason = e.getMessage() == null || e.getMessage().isBlank()
                    ? "invalid operator or operand" : e.getMessage();
            sendJson(ex, 400, jsonError(reason));
        } catch (ArithmeticException e) {
            sendJson(ex, 400, jsonError(e.getMessage()));
        }
    }

    /** Any other path answers 404. */
    private void handleNotFound(HttpExchange ex) throws IOException {
        sendJson(ex, 404, jsonError("unknown endpoint, use /api/eval?a=..&op=..&b=.."));
    }

    /** Parses "a=1&op=%2B&b=2" into a map; '+' must arrive URL-encoded (%2B). */
    private Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> map = new HashMap<>();
        if (rawQuery == null) {
            return map;
        }
        for (String pair : rawQuery.split("&")) {
            int eq = pair.indexOf('=');
            if (eq < 0) {
                continue;
            }
            String key = URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8);
            String value = URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
            map.put(key, value);
        }
        return map;
    }

    private String jsonResult(String a, String op, String b, String result) {
        return "{\"a\":" + quote(a) + ",\"op\":" + quote(op) + ",\"b\":" + quote(b)
                + ",\"result\":" + result + "}";
    }

    private String jsonError(String message) {
        return "{\"error\":" + quote(message == null ? "bad request" : message) + "}";
    }

    /** Minimal JSON string escaping for echoed user input. */
    private String quote(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }

    private void sendJson(HttpExchange ex, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void log(String level, String message) {
        if (logRequests) {
            System.out.println("[" + level + "] " + LocalTime.now() + " " + message);
        }
    }
}
