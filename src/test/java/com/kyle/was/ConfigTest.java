package com.kyle.was;

import com.kyle.was.core.Config;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class ConfigTest {
    private Config config;

    @Before
    public void getInstance() throws Exception {
 config = Config.getInstance();


        System.out.println(config);
    }


    @Test
    public void getPort(){
        int port = config.getPort();
        assertEquals(8080,port);
    }


    @Test
    public void getDefault(){
        Map<Integer, String> errorPages = config.getErrorPages("default");

        assertEquals("/error/404.html",errorPages.get("404"));
    }

}