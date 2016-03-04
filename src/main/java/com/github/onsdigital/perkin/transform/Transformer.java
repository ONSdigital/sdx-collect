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
    private IdbrReceiptBuilder idbrReceiptFactory = new IdbrReceiptBuilder();
    private FtpPublisher publisher = new FtpPublisher();
    private Audit audit = new Audit();

    private Transformer() {

    }

    public static Transformer getInstance() {
        return INSTANCE;
    }

    public Response<Result> transform(final String data) throws IOException {

        System.out.println("transform data " + data);

        Response<Survey> decryptResponse = decrypt.decrypt(data);
        System.out.println("decrypt <<<<<<<< response: " + Json.format(decryptResponse));
        audit.increment("decrypt." + decryptResponse.statusLine.getStatusCode());

        if (isError(decryptResponse.statusLine)) {
            return new Response<>(decryptResponse.statusLine, Result.builder().error(true).message("problem decrypting").build());
        }

        Survey survey = decryptResponse.body;
        IdbrReceipt receipt = idbrReceiptFactory.createIdbrReceipt(survey, batchId.getAndIncrement());
        System.out.println("transform created IDBR receipt: " + Json.format(receipt));

        Response<Result> response = null;
        try {
            publisher.publish(receipt);
            audit.increment("publish.idbr.200");
            response = new Response<>(decryptResponse.statusLine, Result.builder().error(true).message("published " + receipt.getFilename()).build());

        } catch (IOException e) {
            audit.increment("publish.idbr.500");
            response = new Response<>(decryptResponse.statusLine, Result.builder().error(true).message("problem publishing").build());
        }

        System.out.println("transform <<<<<<<< response: " + Json.format(response));

        return response;
    }

    private boolean isError(StatusLine statusLine) {
        return statusLine.getStatusCode() != HttpStatus.OK_200;
    }

    public Map<String, AtomicLong> getInfo() {
        return audit.getInfo();
    }
}
