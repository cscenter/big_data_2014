package ru.hw1.metadata.servlet;

import org.apache.log4j.Logger;
import ru.hw1.metadata.Metadata;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
* Evgeny Vanslov
* vans239@gmail.com
*/
public class GetWriteReplicaServlet extends HttpServlet {
    private Logger log = Logger.getLogger(PingServlet.class);

    private final Metadata metadata;

    public GetWriteReplicaServlet(Metadata metadata) {
        this.metadata = metadata;
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();
        String mainServerAddress = metadata.getMainServerAddress();
        log.debug("Main server = " + mainServerAddress);
        writer.println(mainServerAddress);
    }
}
