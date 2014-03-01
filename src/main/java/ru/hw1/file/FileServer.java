package ru.hw1.file;

import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import ru.hw1.file.servlet.WriteServlet;
import ru.hw1.file.servlet.YouAreMainServlet;

import java.net.URI;

/**
 * Evgeny Vanslov
 * vans239@gmail.com
 */
public class FileServer {
    private static Logger log = Logger.getLogger(FileServer.class);

    public static void main(String[] args) throws Exception {

        int replicaPort;
        int masterPort;
        if(args.length != 2){
            log.warn("No explicit ports were provided. ");
            replicaPort = 8002;
            masterPort = 8000;
        } else {
            replicaPort = Integer.valueOf(args[0]);
            masterPort = Integer.valueOf(args[1]);
        }

        log.info("Master port :" + masterPort);
        log.info("Replica port : " + replicaPort);


        URI replicaServer = new URIBuilder("http://localhost").setPort(replicaPort).build();
        URI masterServer = new URIBuilder("http://localhost").setPort(masterPort).build();
        Replica replica = new Replica(masterServer, replicaServer);
        Server server = new Server(replicaPort);


        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(new ServletHolder(new WriteServlet(replica)), "/write");
        context.addServlet(new ServletHolder(new YouAreMainServlet(replica)), "/youAreMain");

        server.start();
        new Thread(new Pinger(replica)).start();
        server.join();
    }
}
