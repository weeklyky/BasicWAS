package com.kyle.was.router.rule;

import com.kyle.was.core.Config;
import com.kyle.was.router.RoutingUtils;
import com.kyle.was.servlet.ServletInstanceManager;
import com.kyle.was.units.HttpRequest;
import com.kyle.was.units.HttpResponse;
import com.kyle.was.servlet.SimpleServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class ServletByClassNameRoutingRule implements RoutingRule {
    private static final Logger logger = LoggerFactory.getLogger(ServletByClassNameRoutingRule.class);
    private final ConcurrentHashMap<String, SimpleServlet> servletInstances;
    private final Config config;
    private final ServletInstanceManager manager;
    private final RoutingRule routingRule;


    public ServletByClassNameRoutingRule(RoutingRule routingRule){
        this.routingRule = routingRule;

        servletInstances = new ConcurrentHashMap<String, SimpleServlet>();
        this.config = Config.getInstance();
        this.manager = ServletInstanceManager.getInstance();

    }

    @Override
    public void route(HttpRequest request, HttpResponse response) {
        String method = request.getMethod();
        String path = request.getLocation();
        String host = request.getHost();
        String root = config.getHttpRoot(host);
        String name = path;
        SimpleServlet servlet;
        if ((servlet = manager.getServletClass(name, path)) != null) {
            try {
                logger.debug("200 OK, Servlet");
                servlet.service(request, response);
                response.flush();
            } catch (Exception e) {
                logger.warn("500 Server Error, Servlet");
                logger.warn("{}", e);
                RoutingUtils.responseErrorPage(response, host, HttpResponse.INTERNAL_SERVER_ERROR);
            }
            return;
        }
        routingRule.route(request,response);

    }

}
