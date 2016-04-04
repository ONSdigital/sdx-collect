package com.github.onsdigital.perkin.api;

import com.github.davidcarboni.restolino.framework.Api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.io.IOException;
import java.util.Properties;

@Api
public class Version {

    @GET
    public Properties get(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Properties properties = new Properties();
        properties.load(getClass().getClassLoader().getResourceAsStream("build.info"));

        return properties;
    }
}
