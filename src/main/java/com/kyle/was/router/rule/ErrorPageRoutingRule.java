package com.kyle.was.router.rule;

import com.kyle.was.core.Config;
import com.kyle.was.router.RoutingUtils;
import com.kyle.was.units.HttpRequest;
import com.kyle.was.units.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorPageRoutingRule implements RoutingRule {
    private static final Logger logger = LoggerFactory.getLogger(ErrorPageRoutingRule.class);
    private final Config config;
    private final RoutingRule routingRule;


    public ErrorPageRoutingRule(RoutingRule routingRule){
        this.routingRule = routingRule;
        this.config = Config.getInstance();
    }

    @Override
    public void route(HttpRequest request, HttpResponse response) {
        String method = request.getMethod();
        String path = request.getLocation();
        String host = request.getHost();
        String root = config.getHttpRoot(host);
        if (isMalformedPath(root + path)) {
            logger.debug("403 Forbidden");
            RoutingUtils.responseErrorPage(response, host, HttpResponse.FORBIDDEN);
            return;

        } else if (isNotFound(root + path)) {
            logger.debug("404 Not Found");
            RoutingUtils.responseErrorPage(response, host, HttpResponse.NOT_FOUND);
            return;
        }

        routingRule.route(request,response);

    }
    public static boolean isMalformedPath(String path) {
        Pattern p = Pattern.compile("(\\.\\./|\\.exe$)");
        Matcher matcher = p.matcher(path);
        if (matcher.find()) {
            return true;
        } else {
            return false;
        }
    }


    public static boolean isNotFound(String path) {
        Path p = Paths.get(path);
        if(Files.exists(p) && !Files.isDirectory(Paths.get(path))){
            return false;
        }else{
            return true;
        }
    }
}
