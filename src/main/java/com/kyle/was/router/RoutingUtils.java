package com.kyle.was.router;

import com.kyle.was.core.Config;
import com.kyle.was.units.HttpResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RoutingUtils {
    private static final Config config = Config.getInstance();




    public static void responseStaticPage(HttpResponse res, String path)  {
        res.setResponseCode(HttpResponse.OK);
        res.setStaticFileOutput(Paths.get(path));
    }

    public static void responseErrorPage(HttpResponse res, String host, int errorCode)  {
        String root = config.getHttpRoot(host);
        String page = config.getErrorPages(host).get(errorCode);
        res.setResponseCode(errorCode);
        res.setStaticFileOutput(Paths.get(root + page));
    }
}
