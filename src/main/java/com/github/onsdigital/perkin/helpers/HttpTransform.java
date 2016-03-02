package com.github.onsdigital.perkin.helpers;

import com.github.davidcarboni.httpino.Endpoint;
import com.github.davidcarboni.httpino.Http;
import com.github.davidcarboni.httpino.Response;
import com.github.onsdigital.perkin.json.EncryptedPayload;
import com.github.onsdigital.perkin.json.Result;

import java.io.IOException;

/**
 * Decrypt via HTTP.
 */
public class HttpTransform {

    public Response<Result> transform(final String data) throws IOException {

        Endpoint endpoint = new Endpoint("/transform");
        System.out.println("transform endpoint " + endpoint);

        EncryptedPayload payload = new EncryptedPayload();
        payload.setContents(data);
        System.out.println("transform data " + data);

        return new Http().postJson(endpoint, data, Result.class);
    }
}
