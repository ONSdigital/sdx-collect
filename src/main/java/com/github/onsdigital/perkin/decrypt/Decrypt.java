package com.github.onsdigital.perkin.decrypt;

import com.github.davidcarboni.httpino.Endpoint;
import com.github.davidcarboni.httpino.Host;
import com.github.onsdigital.ConfigurationManager;
import com.github.onsdigital.HttpManager;
import com.github.davidcarboni.httpino.Response;
import com.github.onsdigital.perkin.helper.Timer;
import com.github.onsdigital.perkin.transform.Audit;
import com.github.onsdigital.perkin.transform.TransformException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.StatusLine;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;

/**
 * Decrypt data via HTTP.
 */
@Slf4j
public class Decrypt {

    private String encrypted;

    private String host;
    private String path;

    private Audit audit = Audit.getInstance();

    public Decrypt(String encryptedData) {
        encrypted = encryptedData;

        host = ConfigurationManager.get("DECRYPT_HOST");
        path = ConfigurationManager.get("DECRYPT_PATH");
    }

    public Endpoint getEndpoint() {
        return new Endpoint(new Host(host), path);
    }

    public String getDecrypted() throws IOException {
        return decryptPayload(encrypted);
    }

    private String decryptPayload(String encryptedData) throws IOException {
        Endpoint endpoint = getEndpoint();

        // Make call
        log.debug("DECRYPT|decrypting data using endpoint: {}", endpoint);

        log.info("DECRYPT|REQUEST|decrypt: {}", encryptedData);

        Timer timer = new Timer("decrypt.");

        Response<String> decryptResponse = HttpManager.getInstance().postString(endpoint, encryptedData);
        timer.stopStatus(decryptResponse.statusLine.getStatusCode());
        audit.increment(timer);

        log.debug("DECRYPT|RESPONSE|survey: {}", decryptResponse);

        if (isError(decryptResponse.statusLine)) {
            log.warn("DECRYPT|RESPONSE|FAIL|failed to decrypt data: {} response: {}", encryptedData, decryptResponse);
            throw new TransformException("decrypt response indicated an error: " + decryptResponse);
        }

        return decryptResponse.body;
    }

    private boolean isError(StatusLine statusLine) {
        return statusLine.getStatusCode() != HttpStatus.OK_200;
    }
}
