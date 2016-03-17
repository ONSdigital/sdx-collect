package com.github.onsdigital.perkin.decrypt;

import com.github.davidcarboni.httpino.Endpoint;
import com.github.davidcarboni.httpino.Host;
import com.github.onsdigital.ConfigurationManager;
import com.github.onsdigital.perkin.helper.Http;
import com.github.davidcarboni.httpino.Response;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Decrypt data via HTTP.
 */
@Slf4j
public class HttpDecrypt {

    private String host;
    private String path;

    public HttpDecrypt() {
        host = ConfigurationManager.get("decrypt.host");
        path = ConfigurationManager.get("decrypt.path");
    }

    public Response<String> decrypt(final String data) throws IOException {

        Endpoint endpoint = new Endpoint(new Host(host), path);
        log.debug("DECRYPT|decrypting data using endpoint: {}", endpoint);
        return new Http().postString(endpoint, data);
    }
}
