package com.github.onsdigital.perkin.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.perkin.SurveyListener;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.io.IOException;

@Api
public class Health {

    @GET
    public String get(HttpServletRequest request, HttpServletResponse response) throws IOException {

        SurveyListener listener = new SurveyListener();
        return listener.test();
    }
}