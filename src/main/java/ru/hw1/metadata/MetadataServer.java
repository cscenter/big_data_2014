package ru.hw1.metadata;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import ru.hw1.metadata.servlet.GetWriteReplicaServlet;
import ru.hw1.metadata.servlet.PingServlet;

/**
 * Evgeny Vanslov
 * vans239@gmail.com
 */
public class MetadataServer {
    private static Logger log = Logger.getLogger(MetadataServer.class);

    public static final long MAX_UNPINGED_TIME = 10 * 1000;

    public static void main(String[] args) throws Exception {
        int masterPort;
        if(args.length != 1){
            log.warn("No explicit ports were provided. ");
            masterPort = 8000;
        } else {
            masterPort = Integer.valueOf(args[1]);
        }

        log.info("Master port :" + masterPort);

        Server server = new Server(masterPort);

        Metadata metadata = new Metadata();

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(new ServletHolder(new GetWriteReplicaServlet(metadata)), "/get-write-replica");
        context.addServlet(new ServletHolder(new PingServlet(metadata)), "/ping");

        server.start();
        server.join();
    }


}
