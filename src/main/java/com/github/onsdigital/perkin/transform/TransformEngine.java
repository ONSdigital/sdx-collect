package com.github.onsdigital.perkin.transform;

import com.github.davidcarboni.httpino.Response;
import com.github.davidcarboni.httpino.Serialiser;
import com.github.onsdigital.Json;
import com.github.onsdigital.perkin.decrypt.HttpDecrypt;
import com.github.onsdigital.perkin.helper.FileHelper;
import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.json.SurveyTemplate;
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

    private HttpDecrypt decrypt = new HttpDecrypt();

    private List<Transformer> transformers;

    private FtpPublisher publisher = new FtpPublisher();

    private Audit audit = new Audit();
    private BatchNumberService batchNumberService = new BatchNumberService();

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
            Survey survey = decrypt(data);

            SurveyTemplate template = getTemplate(survey);

            long batch = batchNumberService.getNext();

            List<DataFile> files = new ArrayList<>();
            //TODO: use executors (multithreading)
            for (Transformer transformer : transformers) {
                files.addAll(transformer.transform(survey, template, batch));
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

    private Survey decrypt(String data) throws IOException {
        log.debug("DECRYPT|REQUEST|decrypt: {}", data);
        Response<Survey> decryptResponse = decrypt.decrypt(data);
        log.debug("DECRYPT|RESPONSE|survey: {}", Json.prettyPrint(decryptResponse));
        audit.increment("decrypt." + decryptResponse.statusLine.getStatusCode());

        if (isError(decryptResponse.statusLine)) {
            throw new TransformException("decrypt response indicated an error: " + decryptResponse);
        }

        //TODO audit time taken

        Survey survey = decryptResponse.body;
        if (survey == null) {
            audit.increment("decrypt.400");
            throw new TransformException("transform decrypt did not parse to a Survey. JSON mismatch? data: " + data);
        }

        return survey;
    }

    //TODO: make private
    public SurveyTemplate getTemplate(Survey survey) throws TemplateNotFoundException {

        //TODO: only load a survey template once
        try {
            //we only have the MCI survey template for now
            String json = new String(FileHelper.loadFileAsBytes("surveys/template.023.json"));
            return Serialiser.deserialise(json, SurveyTemplate.class);
        } catch (IOException e) {
            throw new TemplateNotFoundException("surveys/template.023.json", e);
        }
    }

    private boolean isError(StatusLine statusLine) {
        return statusLine.getStatusCode() != HttpStatus.OK_200;
    }

    public Audit getAudit() {
        return audit;
    }
}
