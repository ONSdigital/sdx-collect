package com.github.onsdigital.perkin.helpers;

import com.github.davidcarboni.httpino.Endpoint;
import com.github.davidcarboni.httpino.Host;
import com.github.davidcarboni.httpino.Http;
import com.github.davidcarboni.httpino.Response;
import com.github.onsdigital.perkin.json.IdbrReceipt;
import com.github.onsdigital.perkin.json.Result;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Publish a file via HTTP.
 */
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
        System.out.println("publish endpoint " + endpoint);

        return new Http().post(endpoint, inputStream, filename, Result.class);
    }
}
