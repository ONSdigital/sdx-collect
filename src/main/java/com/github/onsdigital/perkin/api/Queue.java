package com.github.onsdigital.perkin.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.perkin.helpers.*;
import com.github.onsdigital.perkin.json.EncryptedPayload;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import java.io.IOException;

/**
 * Add a message to the queue.
 */
@Api
public class Queue {

    @POST
    public String queue(HttpServletRequest request, HttpServletResponse response, EncryptedPayload payload) throws IOException, InterruptedException {

        System.out.println("queue >>>>>>>> request: " + Json.format(payload));

        System.out.println("queue >>>>>>>> send message: " + payload.getContents());
        Tx.sendMessage(payload.getContents());

        return "{ }";
    }
}
