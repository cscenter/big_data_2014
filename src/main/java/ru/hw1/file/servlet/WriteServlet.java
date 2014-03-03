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
public final class WriteServlet extends HttpServlet {
    private final Replica replica;

    public WriteServlet(Replica replica) {
        this.replica = replica;
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();
        if (replica.isMainReplica()) {
            writer.println("OK");
        } else {
            writer.println("KO");
        }
    }
}
