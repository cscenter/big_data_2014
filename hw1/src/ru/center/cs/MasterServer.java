package ru.center.cs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * @author Yuri Denison
 * @date 24.02.2014
 */
public class MasterServer {
    public static final String UTF8_ENCODING = "UTF-8";
    public static final int MASTER_PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(MASTER_PORT), 0);
        server.createContext("/get-write-replica", new WriteReplicaHandler());
        server.start();
    }

    private static class WriteReplicaHandler implements HttpHandler {
        @Override
        public void handle(final HttpExchange request) throws IOException {
            String response = "Hello, World!";
            request.sendResponseHeaders(200, response.length());
            OutputStream os = request.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
