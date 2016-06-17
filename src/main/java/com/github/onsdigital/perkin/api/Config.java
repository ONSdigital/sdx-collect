package com.github.onsdigital.perkin.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.Configuration;
import com.github.onsdigital.ConfigurationManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.io.IOException;
import java.util.Map;

@Api
public class Config {

    @GET
    public String get(HttpServletRequest request, HttpServletResponse response) throws IOException {

        return "WRITE_BATCH_HEADER=" + Configuration.get("WRITE_BATCH_HEADER");
    }
}
