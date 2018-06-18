package com.kyle.was.router;

import com.kyle.was.core.Config;
import com.kyle.was.router.rule.RoutingRule;
import com.kyle.was.units.HttpRequest;
import com.kyle.was.units.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticWebRouter implements RoutingRule {
    private static final Logger logger = LoggerFactory.getLogger(StaticWebRouter.class);
    private final Config config;

    private StaticWebRouter(){
        config = Config.getInstance();


    }
    @Override
    public void route(HttpRequest request, HttpResponse response) {
        String method = request.getMethod();
        String path = request.getLocation();
        String host = request.getHost();
        String root = config.getHttpRoot(host);

        logger.debug("200 OK, html");
        RoutingUtils.responseStaticPage(response, root+path);


    }


    public static StaticWebRouter getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static class LazyHolder {
        private static final StaticWebRouter INSTANCE = new StaticWebRouter();
    }
}
