package com.github.onsdigital.perkin.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.io.IOException;

@Api
public class Config2 {

    @GET
    public String get(HttpServletRequest request, HttpServletResponse response) throws IOException {

        return "WRITE_BATCH_HEADER=" + Configuration.getBoolean("WRITE_BATCH_HEADER", true);
    }
}
