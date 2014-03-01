package ru.hw1.file;

import org.apache.log4j.Logger;
import ru.hw1.metadata.MetadataServer;

import java.net.URI;
import java.util.Date;

/**
 * Evgeny Vanslov
 * vans239@gmail.com
 */
public class Replica {
    private Logger log = Logger.getLogger(Replica.class);

    private boolean isMainReplica = false;
    private long lastPingTime = 0;
    private final URI metadataServer;
    private final URI replicaServer;

    public Replica(URI metadataServer, URI replicaServer) {
        this.metadataServer = metadataServer;
        this.replicaServer = replicaServer;
    }

    public URI getReplicaServer() {
        return replicaServer;
    }

    public URI getMetadataServer() {
        return metadataServer;
    }

    public synchronized void updateMainReplica() {
        if (isMainReplica && lastPingTime + MetadataServer.MAX_UNPINGED_TIME < new Date().getTime()) {
            log.debug("I am not main replica");
            isMainReplica = false;
        }
    }

    public synchronized boolean isMainReplica() {
        updateMainReplica();
        return isMainReplica;
    }

    public synchronized void pinged() {
        log.debug("Ping by " + getReplicaServer());
        lastPingTime = new Date().getTime();
    }

    public synchronized void setIsMainReplica(boolean isMain) {
        if(isMain != isMainReplica){
            log.debug("My status " + (isMain ? "Main" : "Not main"));
        }
        isMainReplica = isMain;
    }
}
