package com.github.onsdigital.perkin.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.ConfigurationManager;
import com.github.onsdigital.perkin.transform.TransformEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Api
public class Env {

    @GET
    public Map<String, String> get(HttpServletRequest request, HttpServletResponse response) throws IOException {

        return ConfigurationManager.getInstance().getSafeValues();
    }
}
