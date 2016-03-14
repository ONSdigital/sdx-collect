package com.github.onsdigital.perkin.publish;

import com.github.davidcarboni.httpino.Endpoint;
import com.github.davidcarboni.httpino.Host;
import com.github.davidcarboni.httpino.Http;
import com.github.davidcarboni.httpino.Response;
import com.github.onsdigital.Configuration;
import com.github.onsdigital.perkin.transform.idbr.IdbrReceipt;
import com.github.onsdigital.perkin.json.Result;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Publish a file via HTTP.
 */
@Slf4j
public class HttpPublisher {

    protected static final String HOST = "publish.host";
    protected static final String PATH = "publish.path";

    private String host;
    private String path;

    public HttpPublisher() {
        host = Configuration.get(HOST, "http://pootle:8080/");
        path = Configuration.get(PATH, "/publish");
    }

    public Response<Result> publish(final IdbrReceipt receipt) throws IOException {

        InputStream inputStream = new ByteArrayInputStream(receipt.getReceipt().getBytes(StandardCharsets.UTF_8));

        return publish(inputStream, receipt.getFilename());
    }

    public Response<Result> publish(final InputStream inputStream, String filename) throws IOException {

        Endpoint endpoint = new Endpoint(new Host(host), path);
        log.info("PUBLISH|publishing file: {} to endpoint: {}", filename, endpoint);

        return new Http().post(endpoint, inputStream, filename, Result.class);
    }
}
