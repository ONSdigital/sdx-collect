package com.github.onsdigital.perkin.decrypt;

import com.github.davidcarboni.httpino.Endpoint;
import com.github.davidcarboni.httpino.Host;
import com.github.davidcarboni.httpino.Http;
import com.github.davidcarboni.httpino.Response;
import com.github.onsdigital.Configuration;
import com.github.onsdigital.perkin.json.Survey;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Decrypt data via HTTP.
 */
@Slf4j
public class HttpDecrypt {

    protected static final String HOST = "decrypt.host";
    protected static final String PATH = "decrypt.path";

    private String host;
    private String path;

    public HttpDecrypt() {
        host = Configuration.get(HOST, "http://posie:5000/");
        path = Configuration.get(PATH, "/decrypt");
    }

    public Response<String> decrypt(final String data) throws IOException {

        Endpoint endpoint = new Endpoint(new Host(host), path);
        log.debug("DECRYPT|decrypting data using endpoint: {}", endpoint);
        return new Http().postJson(endpoint, data, String.class);
    }
}
