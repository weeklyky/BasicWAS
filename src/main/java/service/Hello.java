package service;

import com.kyle.was.HttpRequest;
import com.kyle.was.HttpResponse;
import com.kyle.was.SimpleServlet;

import java.io.IOException;

public class Hello implements SimpleServlet {

    @Override
    public void service(HttpRequest req, HttpResponse res) throws IOException {
        java.io.Writer writer = res.getWriter();
        writer.write("Hello, ");
        writer.write(req.getParameter("name"));
    }

}