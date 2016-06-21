package com.github.onsdigital.perkin.helper;

import com.github.davidcarboni.httpino.Endpoint;
import com.github.davidcarboni.httpino.Host;
import com.github.davidcarboni.httpino.Response;
import com.github.onsdigital.ConfigurationManager;
import com.github.onsdigital.HttpManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Store JSON Survey via HTTP.
 */
@Slf4j
public class SdxStore {

    private String host;
    private String path;

    public SdxStore() {
        host = ConfigurationManager.get("STORE_HOST");
        path = ConfigurationManager.get("STORE_PATH");
    }

    public Response<String> store(final String json) throws IOException {

        Endpoint endpoint = new Endpoint(new Host(host), path);
        log.info("STORE|storing survey to endpoint: {}", endpoint);

        return HttpManager.getInstance().postString(endpoint, json);
    }
}
