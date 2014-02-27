package ru.center.cs;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Yuri Denison
 * @date 24.02.2014
 */
public class FileServer {
    private HttpServer server;
    private final String masterServer;
    private boolean isActive;
    private Timer deprecationTimer;

    private String selfHost;
    private int selfPort;


    public FileServer(final String masterServer, int port) {
        this.masterServer = masterServer;
        deprecationTimer = new Timer();
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext(Constants.REPLICA_WRITE_PATH, new WriteReplicaHandler());
            server.createContext(Constants.REPLICA_MANAGE_PATH, new ManageReplicaHandler());
        } catch (IOException e) {
            System.out.println("Failed start file server: " + e.getMessage() + "\n Aborted.");
            server = null;
        }

    }

    private void start() {
        if (server == null) {
            return;
        }
        try {
            selfHost = InetAddress.getLocalHost().getHostAddress();
            selfPort = server.getAddress().getPort();
        } catch (UnknownHostException e) {
            System.out.println("Can't determine localhost address");
            return;
        }
        System.out.println("Starting replica...");
        server.start();
        sendMasterMessage(Constants.HEADER_REPLICA_ACTION_REGISTER);
    }

    private void sendMasterMessage(final String action) {
        final String urlString = masterServer + Constants.MASTER_MANAGE_PATH;
        try {
            final URL url = new URL(urlString);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty(Constants.HEADER_ACTION_TITLE, action);
            connection.addRequestProperty(Constants.HEADER_REPLICA_ADDRESS, "http://" + selfHost + ":" + selfPort);
            connection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
            connection.connect();
            connection.getResponseCode();
        } catch (IOException e) {
            System.out.println("Failed to connect to " + urlString + ": " + e.getMessage());
        }
    }

    private class WriteReplicaHandler implements HttpHandler {
        @Override
        public void handle(final HttpExchange request) throws IOException {
            final String response = isActive ? Constants.REPLICA_ACTIVE_RESPONSE : Constants.REPLICA_INACTIVE_RESPONSE;
            request.sendResponseHeaders(200, response.length());
            OutputStream os = request.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private class ManageReplicaHandler implements HttpHandler {
        @Override
        public void handle(final HttpExchange exchange) throws IOException {
            final Headers requestHeaders = exchange.getRequestHeaders();
            if (!requestHeaders.containsKey(Constants.HEADER_ACTION_TITLE)) {
                return;
            }
            final String action = requestHeaders.getFirst(Constants.HEADER_ACTION_TITLE);
            switch (action) {
                case Constants.HEADER_REPLICA_ACTION_SET_ACTIVE:
                    isActive = true;
                    deprecationTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            isActive = false;
                            System.out.println("Replica is deprecated now.");
                            sendMasterMessage(Constants.HEADER_REPLICA_ACTION_DEPRECATED);
                        }
                    }, Constants.REPLICA_DEPRECATION_TIME);
                    System.out.println("Replica is active now.");
                    break;
                case Constants.HEADER_REPLICA_ACTION_SET_INACTIVE:
                    isActive = false;
                    System.out.println("Replica is inactive now.");
                    break;
                case Constants.HEADER_REPLICA_ACTION_SHUTDOWN:
                    sendMasterMessage(Constants.HEADER_REPLICA_ACTION_UNREGISTER);
                    System.exit(0);
                    break;
            }
            exchange.sendResponseHeaders(200, 0);
        }
    }

    public static void main(String[] args) throws IOException {
        System.setProperty("java.net.preferIPv4Stack", "true");
        if (args.length != 2) {
            System.out.println("Should have 2 parameters: master server address and port for replica");
            return;
        }
        final String masterServer = args[0];
        final int port = Integer.valueOf(args[1]);
        new FileServer(masterServer, port).start();
    }
}
