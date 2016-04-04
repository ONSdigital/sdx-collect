package com.github.onsdigital.perkin.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.perkin.SurveyListener;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Api
public class Health {

    @GET
    public Map<String, Object> get(HttpServletRequest request, HttpServletResponse response) {

        Map<String, Object> health = new HashMap<>();
        Map<String, String> rabbit = new HashMap<>();

        SurveyListener listener = new SurveyListener();
        try {
            health.put("status", "UP");
            rabbit.put("status", "UP");
            rabbit.put("version", listener.test());
        } catch (IOException e) {
            health.put("status", "DOWN");
            rabbit.put("status", "DOWN");
            rabbit.put("error", e.toString());
        }
        health.put("rabbit", rabbit);

        return health;
    }
}