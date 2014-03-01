package ru.hw1.file;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import ru.hw1.metadata.MetadataServer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
* Evgeny Vanslov
* vans239@gmail.com
*/
public class Pinger implements Runnable {
    private Logger log = Logger.getLogger(Pinger.class);

    private final Replica replica;

    public Pinger(Replica replica) {
        this.replica = replica;
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while(true){
            ping();
            try {
                Thread.sleep(MetadataServer.MAX_UNPINGED_TIME / 3);
            } catch (InterruptedException e) {
                log.error("", e);
            }
        }
    }

    private void ping() {
        URI uri;
        try {
            uri = new URIBuilder(replica.getMetadataServer())
                    .setPath("/ping")
                    .setParameter("serverAddress", replica.getReplicaServer().toString())
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        final String responseBody;
        try {
            responseBody = Request.Get(uri).execute().returnContent().asString();
        } catch (IOException e) {
            log.warn("Can't connect to master");
            return ;
        }
        if("YouAreMain".equals(responseBody)){
            replica.setIsMainReplica(true);
            replica.pinged();
        } else {
            replica.setIsMainReplica(false);
        }
    }
}
