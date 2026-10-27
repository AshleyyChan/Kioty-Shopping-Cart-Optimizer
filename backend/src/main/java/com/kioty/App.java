package com.kioty;

import com.kioty.algorithm.KnapsackOptimizer;
import com.kioty.model.Item;
import com.kioty.model.OptimizeResult;
import com.kioty.service.HistoryService;
import com.kioty.util.AppLogger;
import com.kioty.util.JsonUtil;
import com.kioty.util.Validator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Main HTTP Server for KIOTY Shopping Cart Optimizer.
 * Pure Java 21 implementation replacing the Python Flask backend.
 */
public class App {

    public static void main(String[] args) {
        String host = System.getenv().getOrDefault("HOST", "0.0.0.0");
        int port = 5050;
        try {
            String portEnv = System.getenv("PORT");
            if (portEnv != null && !portEnv.trim().isEmpty()) {
                port = Integer.parseInt(portEnv.trim());
            }
        } catch (NumberFormatException e) {
            AppLogger.warning("Invalid PORT environment variable. Falling back to 5050.");
        }

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(host, port), 0);

            // Use virtual threads on Java 21 for peak throughput and responsiveness
            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

            // API Routes
            server.createContext("/", new RootHandler());
            server.createContext("/optimize", new OptimizeHandler());
            server.createContext("/history", new HistoryHandler());

            server.start();

            AppLogger.info("Starting Shopping Cart Optimizer API at http://" + host + ":" + port);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                AppLogger.info("Stopping Shopping Cart Optimizer server...");
                server.stop(1);
            }));

        } catch (IOException e) {
            AppLogger.error("Failed to start HTTP server on " + host + ":" + port, e);
            System.exit(1);
        }
    }

    // -------------------------------------------------------------
    // Shared CORS & Response Helpers
    // -------------------------------------------------------------

    private static void setCorsHeaders(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
        headers.set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
    }

    private static boolean handlePreflight(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        setCorsHeaders(exchange);
        byte[] bytes = JsonUtil.toJson(data).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendTextResponse(HttpExchange exchange, int statusCode, String text, String contentType) throws IOException {
        setCorsHeaders(exchange);
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    // -------------------------------------------------------------
    // Route Handlers
    // -------------------------------------------------------------

    /**
     * Root handler: returns health status or serves frontend static files if requested.
     */
    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handlePreflight(exchange)) return;

            String path = exchange.getRequestURI().getPath();

            // Direct root path -> Health Check
            if ("/".equals(path) || path.isEmpty()) {
                sendTextResponse(exchange, 200, "🟢 Shopping Cart Optimizer API is running.", "text/plain; charset=utf-8");
                return;
            }

            // Check if user is requesting a frontend file (e.g. /app.html, /script.js, /style.css)
            File staticFile = findFrontendFile(path);
            if (staticFile != null && staticFile.exists() && !staticFile.isDirectory()) {
                serveStaticFile(exchange, staticFile);
                return;
            }

            // Fallback 404
            Map<String, String> err = Collections.singletonMap("error", "Route not found");
            sendJsonResponse(exchange, 404, err);
        }

        private File findFrontendFile(String path) {
            if (path.startsWith("/")) path = path.substring(1);
            if (path.startsWith("frontend/")) path = path.substring("frontend/".length());

            File file1 = new File("frontend", path);
            if (file1.exists()) return file1;

            File file2 = new File("../frontend", path);
            if (file2.exists()) return file2;

            return null;
        }

        private void serveStaticFile(HttpExchange exchange, File file) throws IOException {
            String name = file.getName().toLowerCase();
            String mime = "application/octet-stream";
            if (name.endsWith(".html")) mime = "text/html; charset=utf-8";
            else if (name.endsWith(".css")) mime = "text/css; charset=utf-8";
            else if (name.endsWith(".js")) mime = "application/javascript; charset=utf-8";
            else if (name.endsWith(".png")) mime = "image/png";
            else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) mime = "image/jpeg";
            else if (name.endsWith(".svg")) mime = "image/svg+xml";

            setCorsHeaders(exchange);
            exchange.getResponseHeaders().set("Content-Type", mime);
            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    /**
     * Optimization endpoint: /optimize [POST]
     */
    static class OptimizeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handlePreflight(exchange)) return;

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, Collections.singletonMap("error", "Method not allowed. Use POST."));
                return;
            }

            try {
                String body = readRequestBody(exchange);
                AppLogger.info("Incoming request: " + body);

                Map<String, Object> data;
                try {
                    data = JsonUtil.parseObject(body);
                } catch (Exception e) {
                    AppLogger.warning("Invalid JSON received: " + e.getMessage());
                    sendJsonResponse(exchange, 400, Collections.singletonMap("error", "Malformed JSON request body."));
                    return;
                }

                // Input validation
                String errorMsg = Validator.validateInput(data);
                if (!errorMsg.isEmpty()) {
                    AppLogger.warning("Validation failed: " + errorMsg);
                    sendJsonResponse(exchange, 400, Collections.singletonMap("error", errorMsg));
                    return;
                }

                int budget = Validator.toInt(data.get("budget"));
                List<?> rawItems = (List<?>) data.get("items");
                List<Item> items = Validator.extractItems(rawItems);

                String sessionId = HistoryService.generateSessionId();
                KnapsackOptimizer.Result optResult = KnapsackOptimizer.optimize(items, budget);

                String timestamp = LocalDateTime.now().toString();
                OptimizeResult result = new OptimizeResult(
                        sessionId,
                        timestamp,
                        budget,
                        optResult.getSelectedItems(),
                        optResult.getTotalPrice(),
                        optResult.getTotalValue()
                );

                AppLogger.info(String.format("Session %s optimized: %d items, Value=%d, Cost=%d",
                        sessionId, result.getCount(), result.getTotalValue(), result.getTotalPrice()));

                // Save session result
                HistoryService.saveSessionResult(sessionId, result);

                sendJsonResponse(exchange, 200, result);

            } catch (Exception e) {
                AppLogger.error("Unexpected error during optimization: " + e.getMessage(), e);
                sendJsonResponse(exchange, 500, Collections.singletonMap("error", "Internal server error occurred."));
            }
        }
    }

    /**
     * History endpoint: /history [GET]
     */
    static class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handlePreflight(exchange)) return;

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, Collections.singletonMap("error", "Method not allowed. Use GET."));
                return;
            }

            try {
                List<Object> historyData = HistoryService.getHistory("guest_user");
                Map<String, Object> resp = Collections.singletonMap("history", historyData);
                sendJsonResponse(exchange, 200, resp);
            } catch (Exception e) {
                AppLogger.error("Failed to retrieve history: " + e.getMessage(), e);
                sendJsonResponse(exchange, 500, Collections.singletonMap("error", "Could not load history."));
            }
        }
    }
}
