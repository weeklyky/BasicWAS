package com.kyle.was;

import java.io.IOException;

public interface SimpleServlet {
    void service(HttpRequest req, HttpResponse res) throws IOException;
}
