package com.github.onsdigital.perkin.transform;

import com.github.davidcarboni.httpino.Response;
import com.github.onsdigital.Json;
import com.github.onsdigital.perkin.decrypt.HttpDecrypt;
import com.github.onsdigital.perkin.json.IdbrReceipt;
import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.transform.pck.Pck;
import com.github.onsdigital.perkin.transform.pck.PckBuilder;
import com.github.onsdigital.perkin.transform.pck.derivator.DerivatorNotFoundException;
import com.github.onsdigital.perkin.publish.FtpPublisher;
import com.github.onsdigital.perkin.transform.idbr.IdbrBuilder;
import com.github.onsdigital.perkin.transform.jpg.Image;
import com.github.onsdigital.perkin.transform.jpg.ImageBuilder;
import com.github.onsdigital.perkin.transform.jpg.ImageInfo;
import org.apache.http.StatusLine;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Transform a Survey into a format for downstream systems.
 */
public class Transformer {

    private static Transformer INSTANCE = new Transformer();

    //TODO: service/api for batchId
    private static AtomicLong batchId = new AtomicLong(35000);

    private HttpDecrypt decrypt = new HttpDecrypt();
    private IdbrBuilder idbrBuilder = new IdbrBuilder();
    private ImageBuilder imageBuilder = new ImageBuilder();
    private PckBuilder pckBuilder = new PckBuilder();
    private FtpPublisher publisher = new FtpPublisher();
    private Audit audit = new Audit();

    private Transformer() {

    }

    public static Transformer getInstance() {
        return INSTANCE;
    }

    public boolean transform(final String data) throws IOException, DerivatorNotFoundException {

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
                System.out.println("transform decrypt returned a null survey - 400, bad request (data was not a valid json Survey)");
                audit.increment("decrypt.400");
                return false;
            }

            //TODO: determine batch number
            long batch = batchId.getAndIncrement();

            //idbr
            IdbrReceipt receipt = idbrBuilder.createIdbrReceipt(survey, batch);
            System.out.println("transform created IDBR receipt: " + Json.format(receipt));
            publisher.publish(receipt);
            System.out.println("transform published IDBR receipt");
            audit.increment("publish.idbr.200");

            //pck
            Pck pck = pckBuilder.build(survey, batch);
            System.out.println("transform created pck file: " + Json.format(pck));
            publisher.publish(pck);
            System.out.println("transform published pck file");
            audit.increment("publish.pck.200");

            //images
            //TODO: create images
            ImageInfo imageInfo = imageBuilder.createImages(survey, batch);
            System.out.println("transform created images: " + imageInfo);
            for (Image image : imageInfo.getImages()) {
                System.out.println("transform created jpg file: " + image.getFilename());
                publisher.publish(image);
                System.out.println("transform published jpg file");
                audit.increment("publish.jpg.200");
            }

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
