package service;

import com.kyle.was.units.HttpRequest;
import com.kyle.was.units.HttpResponse;
import com.kyle.was.servlet.SimpleServlet;

import java.io.IOException;

public class Hello implements SimpleServlet {

    @Override
    public void service(HttpRequest req, HttpResponse res) throws IOException {
        java.io.Writer writer = res.getWriter();
        writer.write("Hello, ");
        writer.write(req.getParameter("name"));
    }

}