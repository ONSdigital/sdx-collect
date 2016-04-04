package com.github.onsdigital.perkin.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.perkin.SurveyListener;
import com.github.onsdigital.perkin.json.Status;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.io.IOException;

@Api
public class Health {

    @GET
    public Status get(HttpServletRequest request, HttpServletResponse response) {

        Status.StatusBuilder builder = Status.builder();

        SurveyListener listener = new SurveyListener();
        try {
            builder.message("rabbit version " + listener.test()).status("UP");
        } catch (IOException e) {
            builder.message(e.getMessage()).status("DOWN");
        }

        return builder.build();
    }
}