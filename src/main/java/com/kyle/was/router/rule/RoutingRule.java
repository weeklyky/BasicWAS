package com.kyle.was.router.rule;

import com.kyle.was.units.HttpRequest;
import com.kyle.was.units.HttpResponse;

public interface RoutingRule {

    void route(HttpRequest request, HttpResponse response);
}
