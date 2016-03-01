package com.github.onsdigital.perkin.helpers;

import com.github.davidcarboni.httpino.Endpoint;
import com.github.davidcarboni.httpino.Host;
import com.github.davidcarboni.httpino.Http;
import com.github.davidcarboni.httpino.Response;
import com.github.onsdigital.perkin.json.EncryptedPayload;
import com.github.onsdigital.perkin.json.IdbrReceipt;
import com.github.onsdigital.perkin.json.Result;
import com.github.onsdigital.perkin.json.Survey;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Decrypt via HTTP.
 */
public class HttpDecrypt {

    protected static final String HOST = "decrypt.host";
    protected static final String PATH = "decrypt.path";

    private static final NameValuePair APPLICATION_JSON = new BasicNameValuePair("Content-Type", "application/json");

    private String host;
    private String path;

    public HttpDecrypt() {
        //TODO: integrate with posie
        //host = Configuration.get(HOST, "http://posie:80/");
        host = Configuration.get(HOST, "http://localhost:8080/");
        path = Configuration.get(PATH, "/decrypt");
        //TODO: remove, Ian Wooten's machine
        //host = Configuration.get(HOST, "http://172.28.47.39:5000/");
        //path = Configuration.get(PATH, "/import");
    }

    public Response<Survey> decrypt(final EncryptedPayload payload) throws IOException {

        Endpoint endpoint = new Endpoint(new Host(host), path);
        System.out.println("decrypt endpoint " + endpoint);
        return new Http().postJson(endpoint, payload, Survey.class, APPLICATION_JSON);
    }
}
