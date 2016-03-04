package com.github.onsdigital.perkin.transform;

import com.github.davidcarboni.httpino.Response;
import com.github.onsdigital.Json;
import com.github.onsdigital.perkin.decrypt.HttpDecrypt;
import com.github.onsdigital.perkin.json.IdbrReceipt;
import com.github.onsdigital.perkin.json.Result;
import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.publish.FtpPublisher;
import org.apache.http.StatusLine;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Transform a Survey into a format for downstream systems.
 */
public class Transformer {

    private static Transformer INSTANCE = new Transformer();

    //TODO: service/api for batchId
    private static AtomicLong batchId = new AtomicLong(35000);

    private HttpDecrypt decrypt = new HttpDecrypt();
    private IdbrBuilder idbrReceiptFactory = new IdbrBuilder();
    private FtpPublisher publisher = new FtpPublisher();
    private Audit audit = new Audit();

    private Transformer() {

    }

    public static Transformer getInstance() {
        return INSTANCE;
    }

    public boolean transform(final String data) throws IOException {

        try {
            System.out.println("transform data " + data);

            Response<Survey> decryptResponse = decrypt.decrypt(data);
            System.out.println("decrypt <<<<<<<< response: " + Json.format(decryptResponse));
            audit.increment("decrypt." + decryptResponse.statusLine.getStatusCode());

            //TODO 400 is bad request - add to DLQ, 500 is server error, retry
            if (isError(decryptResponse.statusLine)) {
                throw new IOException("problem decrypting");
            }

            Survey survey = decryptResponse.body;
            if (survey == null) {
                return false;
            }
            IdbrReceipt receipt = idbrReceiptFactory.createIdbrReceipt(survey, batchId.getAndIncrement());
            System.out.println("transform created IDBR receipt: " + Json.format(receipt));

            publisher.publish(receipt);
            System.out.println("transform published IDBR receipt");
            audit.increment("publish.idbr.200");
            audit.increment("transform.200");

            System.out.println("transform <<<<<<<< success");
            return true;

        } catch (IOException e) {
            audit.increment("transform.500");
            System.out.println("transform <<<<<<<< ERROR: " + e.toString());
            throw e;
        }
    }

    private boolean isError(StatusLine statusLine) {
        return statusLine.getStatusCode() != HttpStatus.OK_200;
    }

    public Audit getAudit() {
        return audit;
    }
}
