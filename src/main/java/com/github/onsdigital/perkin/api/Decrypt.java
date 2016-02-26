package com.github.onsdigital.perkin.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.perkin.helpers.Json;
import com.github.onsdigital.perkin.json.EncryptedPayload;
import com.github.onsdigital.perkin.json.Survey;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Decrypt a Survey.
 */
@Api
public class Decrypt {

    @POST
    public Survey decrypt(HttpServletRequest request, HttpServletResponse response, EncryptedPayload payload) throws IOException {

        //TODO: decrypt payload - not encrypted yet, just base64 encoded Survey json

        System.out.println("decrypt >>>>>>>> request: " + Json.format(payload));

        String base64 = payload.getContents();

        String json = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);

        Gson gson = new GsonBuilder().create();
        Survey survey = gson.fromJson(json, Survey.class);

        System.out.println("decrypt <<<<<<<< response: " + Json.format(survey));

        return survey;
    }
}
