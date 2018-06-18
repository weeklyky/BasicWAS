package com.kyle.was.router.rule;

import com.kyle.was.core.Config;
import com.kyle.was.router.RoutingUtils;
import com.kyle.was.units.HttpRequest;
import com.kyle.was.units.HttpResponse;
import com.kyle.was.servlet.SimpleServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexPageRoutingRule implements RoutingRule {
    private static final Logger logger = LoggerFactory.getLogger(IndexPageRoutingRule.class);
    private final Config config;
    private final RoutingRule routingRule;

    public IndexPageRoutingRule(RoutingRule routingRule) {
        this.config = Config.getInstance();
        this.routingRule = routingRule;
    }


    @Override
    public void route(HttpRequest request, HttpResponse response) {
        String method = request.getMethod();
        String path = request.getLocation();
        String host = request.getHost();
        String root = config.getHttpRoot(host);


        logger.debug("PATH : {}", request.getLocation());
        if (request.getLocation().isEmpty()) {
            logger.debug("200 OK, Index page");
            RoutingUtils.responseStaticPage(response, root + config.getIndexPage(host));
            return;
        }

        routingRule.route(request,response);
    }
}
