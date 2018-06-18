package com.kyle.was.servlet;

import com.kyle.was.units.HttpRequest;
import com.kyle.was.units.HttpResponse;

import java.io.IOException;

public interface SimpleServlet {
    void service(HttpRequest req, HttpResponse res) throws IOException;
}
