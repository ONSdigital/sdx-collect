package com.github.onsdigital.perkin.transform;

import com.github.davidcarboni.httpino.Response;
import com.github.onsdigital.Json;
import com.github.onsdigital.perkin.decrypt.HttpDecrypt;
import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.transform.idbr.IdbrTransformer;
import com.github.onsdigital.perkin.transform.jpg.ImageTransformer;
import com.github.onsdigital.perkin.transform.pck.PckTransformer;
import com.github.onsdigital.perkin.publish.FtpPublisher;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.StatusLine;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Transform a Survey into a format for downstream systems.
 */
@Slf4j
public class TransformEngine {

    private static TransformEngine INSTANCE = new TransformEngine();

    //TODO: service/api for batchId - starts at 30000, to 39999 then back to 30000
    private static AtomicLong batchId = new AtomicLong(35000);

    private HttpDecrypt decrypt = new HttpDecrypt();

    private List<Transformer> transformers;

    private FtpPublisher publisher = new FtpPublisher();

    private Audit audit = new Audit();

    private TransformEngine() {
        //use getInstance()
        //TODO: make configurable
        //TODO: also, transformers on a per survey id basis?
        transformers = Arrays.asList(new IdbrTransformer(), new PckTransformer(), new ImageTransformer());
    }

    public static TransformEngine getInstance() {
        return INSTANCE;
    }

    public boolean transform(final String data) throws TransformException {

        try {
            log.debug("transform data: {}", data);

            Response<Survey> decryptResponse = decrypt.decrypt(data);
            log.debug("decrypt <<<<<<<< response: {}", Json.prettyPrint(decryptResponse));
            audit.increment("decrypt." + decryptResponse.statusLine.getStatusCode());

            //TODO 400 is bad request - add to DLQ, 500 is server error, retry
            if (isError(decryptResponse.statusLine)) {
                throw new TransformException("problem decrypting");
            }

            //TODO audit time taken

            Survey survey = decryptResponse.body;
            if (survey == null) {
                log.warn("transform decrypt did not parse to a Survey. JSON mismatch? data: {}", data);
                audit.increment("decrypt.400");
                return false;
            }

            long batch = batchId.getAndIncrement();

            List<DataFile> files = new ArrayList<>();
            //TODO: use executors (multithreading)
            for (Transformer transformer : transformers) {
                files.addAll(transformer.transform(survey, batch));
            }

            for (DataFile file : files) {
                publisher.publish(file);
            }

            audit.increment("transform.200");
            return true;

        } catch (TransformException e) {
            audit.increment("transform.500");
            throw e;
        } catch (IOException e) {
            audit.increment("transform.500");
            throw new TransformException("Problem transforming survey", e);
        }
    }

    private boolean isError(StatusLine statusLine) {
        return statusLine.getStatusCode() != HttpStatus.OK_200;
    }

    public Audit getAudit() {
        return audit;
    }
}
