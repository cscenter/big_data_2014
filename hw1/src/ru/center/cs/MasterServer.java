package ru.center.cs;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yuri Denison
 * @date 24.02.2014
 */
public class MasterServer {
    private HttpServer server;
    private final List<String> replicas;
    private String activeReplica;

    public MasterServer(int port) {
        replicas = new ArrayList<>(ConstantsUtil.DEFAULT_REPLICA_NUMBER);
        activeReplica = null;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext(ConstantsUtil.MASTER_WRITE_PATH, new WriteReplicaHandler());
            server.createContext(ConstantsUtil.MASTER_MANAGE_PATH, new ManageReplicaHandler());
        } catch (IOException e) {
            System.out.println("Failed start master server: " + e.getMessage() + "\n Aborted.");
            server = null;
        }

    }

    private void start() {
        if (server == null) {
            return;
        }
        System.out.println("Starting master server...");
        server.start();
    }

    private void chooseActiveReplica() {
        sendReplicaMessage(activeReplica, ConstantsUtil.HEADER_REPLICA_ACTION_SET_INACTIVE);
        activeReplica = replicas.get((int) (replicas.size() * Math.random()));
        sendReplicaMessage(activeReplica, ConstantsUtil.HEADER_REPLICA_ACTION_SET_ACTIVE);
        System.out.println("Successfully updated active replica: " + activeReplica);
    }

    private void sendReplicaMessage(final String replica, final String action) {
        if (replica == null) {
            return;
        }
        final String urlString = replica + ConstantsUtil.REPLICA_MANAGE_PATH;
        try {
            final URL url = new URL(urlString);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty(ConstantsUtil.HEADER_ACTION_TITLE, action);
            connection.connect();
            connection.getResponseCode();
        } catch (IOException e) {
            System.out.println("Failed to connect to " + urlString + ": " + e.getMessage());
        }
    }

    private class WriteReplicaHandler implements HttpHandler {
        @Override
        public void handle(final HttpExchange request) throws IOException {
            request.sendResponseHeaders(200, activeReplica.length());
            OutputStream os = request.getResponseBody();
            os.write(activeReplica.getBytes());
            os.close();
        }
    }

    private class ManageReplicaHandler implements HttpHandler {
        @Override
        public void handle(final HttpExchange exchange) throws IOException {
            final Headers requestHeaders = exchange.getRequestHeaders();
            if (!requestHeaders.containsKey(ConstantsUtil.HEADER_ACTION_TITLE)) {
                return;
            }
            final String action = requestHeaders.getFirst(ConstantsUtil.HEADER_ACTION_TITLE);
            final String replicaAddress = requestHeaders.getFirst(ConstantsUtil.HEADER_REPLICA_ADDRESS);
            switch (action) {
                case ConstantsUtil.HEADER_REPLICA_ACTION_REGISTER:
                    replicas.add(replicaAddress);
                    if (activeReplica == null) {
                        chooseActiveReplica();
                    }
                    System.out.println("Successfully added replica: " + replicaAddress);
                    break;
                case ConstantsUtil.HEADER_REPLICA_ACTION_UNREGISTER:
                    replicas.remove(replicaAddress);
                    if (replicaAddress.equals(activeReplica)) {
                        chooseActiveReplica();
                    }
                    System.out.println("Successfully removed replica: " + replicaAddress);
                    break;
                case ConstantsUtil.HEADER_REPLICA_ACTION_DEPRECATED:
                    if (replicaAddress.equals(activeReplica)) {
                        System.out.println("Replica became deprecated, choosing new active replica...");
                        chooseActiveReplica();
                    }
                    break;
            }
            exchange.sendResponseHeaders(200, 0);
        }
    }

    public static void main(String[] args) throws IOException {
        System.setProperty("java.net.preferIPv4Stack", "true");
        final int port = args.length == 1 ? Integer.valueOf(args[0]) : ConstantsUtil.MASTER_PORT;
        new MasterServer(port).start();
    }
}
