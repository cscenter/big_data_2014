package ru.hw1.file.servlet;

import ru.hw1.file.Replica;

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
public class YouAreMainServlet extends HttpServlet {
    private final Replica replica;

    public YouAreMainServlet(Replica replica) {
        this.replica = replica;
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        replica.setIsMainReplica(true);
        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();
        writer.print("OK");
    }
}
