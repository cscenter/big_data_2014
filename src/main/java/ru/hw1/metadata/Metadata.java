package ru.hw1.metadata;

import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
* Evgeny Vanslov
* vans239@gmail.com
*/
public class Metadata {
    private Logger log = Logger.getLogger(Metadata.class);

    private String mainFileServerAddress;
    private Map<String, Long> pings = new ConcurrentHashMap<>();

    public void pinged(String fileServerAddress) {
        log.debug("Ping from " + fileServerAddress);
        pings.put(fileServerAddress, new Date().getTime());
    }

    public synchronized String getMainServerAddress() {
        if (mainFileServerAddress == null || pings.get(mainFileServerAddress) + MetadataServer.MAX_UNPINGED_TIME < new Date().getTime()) {
            mainFileServerAddress = null;
            findMainServer();
        }
        return mainFileServerAddress;
    }

    private void findMainServer() {
        for (String fileServerAddress : pings.keySet()) {
            if (tryMakeMainServer(fileServerAddress)) {
                pinged(fileServerAddress);
                mainFileServerAddress = fileServerAddress;
                return;
            }
        }
    }

    private boolean tryMakeMainServer(String fileServerAddress) {
        String responseBody;
        try {
            responseBody = Request.Get(fileServerAddress + "/youAreMain").execute().returnContent().asString();
        } catch (IOException e) {
            return false;
        }
        return "OK".equals(responseBody);
    }

}
