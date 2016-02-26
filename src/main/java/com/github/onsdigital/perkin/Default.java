package com.github.onsdigital.perkin;

import com.github.davidcarboni.restolino.framework.Home;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Default implements Home {

    @Override
    public String get(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.sendRedirect("/index.html");
        return null;
    }
}
