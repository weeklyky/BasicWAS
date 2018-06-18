package com.kyle.was.servlet;

import com.kyle.was.core.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class ServletInstanceManager {
    private static final Logger logger = LoggerFactory.getLogger(ServletInstanceManager.class);

    private final ConcurrentHashMap<String, SimpleServlet> servletInstances;
    private final Config config;

    private ServletInstanceManager () {
        servletInstances = new ConcurrentHashMap<String, SimpleServlet>();
        config = Config.getInstance();
    }

    public SimpleServlet getServletClass(String servletName, String actualClassName) {
        servletInstances.computeIfAbsent(servletName, name -> {

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            try {
                Class<?> clazz = classLoader.loadClass(actualClassName);
                if(SimpleServlet.class.isAssignableFrom(clazz)){
                    return (SimpleServlet) clazz.newInstance();
                }else{
                    return null;
                }
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("Error while loading servlet {} ", e.toString());
                return null;
            } catch (ClassNotFoundException e) {
                logger.debug("Cant' find such class. {}", actualClassName);
                return null;
            }
        });

        return servletInstances.get(servletName);
    }


    public static ServletInstanceManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static class LazyHolder {
        private static final ServletInstanceManager INSTANCE = new ServletInstanceManager();
    }
}
