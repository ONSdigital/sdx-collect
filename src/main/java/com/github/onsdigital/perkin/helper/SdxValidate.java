package com.github.onsdigital.perkin.helper;

import com.github.davidcarboni.httpino.Endpoint;
import com.github.davidcarboni.httpino.Host;
import com.github.davidcarboni.httpino.Response;
import com.github.onsdigital.ConfigurationManager;
import com.github.onsdigital.HttpManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Validate JSON Survey via HTTP.
 */
@Slf4j
public class SdxValidate {

    private String host;
    private String path;

    public SdxValidate() {
        host = ConfigurationManager.get("VALIDATE_HOST");
        path = ConfigurationManager.get("VALIDATE_PATH");
    }

    public Response<String> validate(final String json) throws IOException {

        Endpoint endpoint = new Endpoint(new Host(host), path);
        log.info("VALIDATE|validating survey to endpoint: {}", endpoint);

        return HttpManager.getInstance().postString(endpoint, json);
    }
}
