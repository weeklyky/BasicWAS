package com.kyle.was;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RequestRouterTest {
    private BasicRequestRouter requestRouter;

    @Before
    public void getInstance() throws Exception {
        requestRouter = BasicRequestRouter.getInstance();
    }
    @Test
    public void isMalformedPath() throws Exception {
        assertTrue(BasicRequestRouter.isMalformedPath("../../../man.html"));
        assertFalse(BasicRequestRouter.isMalformedPath("service.Hello"));
        assertFalse(BasicRequestRouter.isMalformedPath("static/error/4041.html"));
    }

    @Test
    public void isNotFound() throws Exception {
        assertFalse(BasicRequestRouter.isNotFound("static/error/404.html"));
        assertFalse(BasicRequestRouter.isNotFound("static/index.html"));
        assertTrue(BasicRequestRouter.isNotFound("static/error/4041.html"));
    }

    @Test
    public void route() throws Exception {
    }


    @Test
    public void getServletClass() throws Exception {
        SimpleServlet obj = requestRouter.getServletClass("service.Hello");
        System.out.println(obj);

    }


}