package com.kyle.was;

public interface RequestRouter {

    void route(HttpRequest request, HttpResponse response);
}
