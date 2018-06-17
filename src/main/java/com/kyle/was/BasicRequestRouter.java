package com.kyle.was;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BasicRequestRouter implements RequestRouter{
    private static final Logger logger = LoggerFactory.getLogger(BasicRequestRouter.class);
    private final Config config;
    private Map<String, SimpleServlet> servletInstances;

    private BasicRequestRouter(){
        config = Config.getInstance();
        servletInstances = new ConcurrentHashMap<String, SimpleServlet>();


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


    @Override
    public void route(HttpRequest request, HttpResponse response) {
        String method = request.getMethod();
        String path = request.getLocation();
        String host = request.getHost();
        String root = config.getHttpRoot(host);

        SimpleServlet servlet;
        logger.debug("PATH : {}", request.getLocation());
        if(request.getLocation().isEmpty()){
            logger.debug("200 OK, Index page");
            responseStaticPage(response, root + config.getIndexPage(host));
            return;
        } else if((servlet = getServletClass(path))!=null){
            try {
                logger.debug("200 OK, Servlet");
                servlet.service(request, response);
                response.flush();
                return;
            }
            catch(Exception e){
                logger.warn("500 Server Error, Servlet");
                logger.warn("{}", e);
                responseErrorPage(response, host, HttpResponse.INTERNAL_SERVER_ERROR);
                return;
            }
        }else if(isMalformedPath(root+path)){
            logger.debug("403 Forbidden");
            responseErrorPage(response, host, HttpResponse.FORBIDDEN);
            return;

        }else if(isNotFound(root+path)){
            logger.debug("404 Not Found");
            responseErrorPage(response, host, HttpResponse.NOT_FOUND);
            return;
        }else{
            logger.debug("200 OK, html");
            responseStaticPage(response, root+path);
            return;
        }
    }

    public SimpleServlet getServletClass(String requestPath) {
        servletInstances.computeIfAbsent(requestPath, servletName -> {

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            try {
                Class<?> clazz = classLoader.loadClass(servletName);
                if(SimpleServlet.class.isAssignableFrom(clazz)){
                    return (SimpleServlet) clazz.newInstance();
                }else{
                    return null;
                }
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("Error while loading servlet {} ", e.toString());
                return null;
            } catch (ClassNotFoundException e) {
                logger.debug("Cant' find such class. {}", requestPath);
                return null;
            }
        });

        return servletInstances.get(requestPath);
    }

    private void responseStaticPage(HttpResponse res, String path)  {
        res.setResponseCode(HttpResponse.OK);
        res.setStaticFileOutput(Paths.get(path));
    }

    public void responseErrorPage(HttpResponse res, String host, int errorCode)  {
        String root = config.getHttpRoot(host);
        String page = config.getErrorPages(host).get(errorCode);
        res.setResponseCode(errorCode);
        res.setStaticFileOutput(Paths.get(root + page));
    }


    public static BasicRequestRouter getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static class LazyHolder {
        private static final BasicRequestRouter INSTANCE = new BasicRequestRouter();
    }
}
