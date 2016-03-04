package com.github.onsdigital.perkin.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.perkin.transform.Transformer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Api
public class Info {

    @GET
    public Map<String, AtomicLong> get(HttpServletRequest request, HttpServletResponse response) throws IOException {

        return Transformer.getInstance().getInfo();
    }
}
