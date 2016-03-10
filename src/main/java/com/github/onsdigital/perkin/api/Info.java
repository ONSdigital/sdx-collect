package com.github.onsdigital.perkin.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.perkin.transform.Audit;
import com.github.onsdigital.perkin.transform.TransformEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.io.IOException;

@Api
public class Info {

    @GET
    public Audit get(HttpServletRequest request, HttpServletResponse response) throws IOException {

        return TransformEngine.getInstance().getAudit();
    }
}
