package com.github.onsdigital.perkin.api;

import com.github.davidcarboni.cryptolite.Password;
import com.github.davidcarboni.restolino.framework.Api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;

/**
 * Temporary Github webhook.
 */
@Api
public class Deploy {

    @POST
    public void deploy(HttpServletRequest request, HttpServletResponse response, String body) {
        System.out.println(body);
        if (Password.verify("", "otRceZmbMMDJVUkQ1w8zmrbiD3WDOx0a6FPCwH5dTEhGV4htkkwJdPXVFyVxpApK")) {
            // Do some deployment thing.
            System.out.println("Secret checks out.");
        } else {
            System.out.println("Not this time.");
        }
    }
}
