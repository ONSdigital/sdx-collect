package com.github.onsdigital.perkin.transform;

import com.github.davidcarboni.httpino.Response;
import com.github.davidcarboni.httpino.Serialiser;
import com.github.onsdigital.Json;
import com.github.onsdigital.perkin.decrypt.HttpDecrypt;
import com.github.onsdigital.perkin.helper.FileHelper;
import com.github.onsdigital.perkin.json.*;
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

/**
 * Transform a Survey into a format for downstream systems.
 */
@Slf4j
public class TransformEngine {

    private static TransformEngine INSTANCE = new TransformEngine();

    private SurveyParser parser = new SurveyParser();
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

    public void transform(final String data) throws TransformException {

        try {
            String json = decrypt(data);
            Survey survey = parser.parse(json);

            TransformContext context = createTransformContext(survey);

            List<DataFile> files = new ArrayList<>();
            //TODO: use executors (multithreading)
            for (Transformer transformer : transformers) {
                files.addAll(transformer.transform(survey, context));
            }

            publish(files);

            audit.increment("transform.200");

        } catch (SurveyParserException e) {
            audit.increment("transform.400", e);
            throw e;
        } catch (TransformException e) {
            audit.increment("transform.500", e);
            throw e;
        } catch (IOException e) {
            audit.increment("transform.500", e);
            throw new TransformException("Problem transforming survey", e);
        }
    }

    private void publish(List<DataFile> files) throws IOException {
        publisher.publish(files);
        audit.increment("publish.200", files.size());
    }

    //TODO: make private
    public TransformContext createTransformContext(Survey survey) throws TemplateNotFoundException {
        return TransformContext.builder()
                .batch(batchNumberService.getNext())
                .surveyTemplate(getSurveyTemplate(survey))
                .pdfTemplate(getPdfTemplate(survey))
                .build();
    }

    private String getPdfTemplate(Survey survey) throws TemplateNotFoundException {

        String pdfTemplate = null;

        try {
            //TODO: only load a template once
            pdfTemplate = FileHelper.loadFile("to-jpg/mci.fo");
        } catch (IOException e) {
            throw new TemplateNotFoundException("problem loading pdf template: to-jpg/mci.fo");
        }

        return pdfTemplate;
    }

    private SurveyTemplate getSurveyTemplate(Survey survey) throws TemplateNotFoundException {

        //TODO: only load a template once
        try {
            //we only have the MCI survey template for now
            String json = new String(FileHelper.loadFileAsBytes("surveys/template.023.json"));
            return Serialiser.deserialise(json, SurveyTemplate.class);
        } catch (IOException e) {
            throw new TemplateNotFoundException("surveys/template.023.json", e);
        }
    }

    private String decrypt(String data) throws IOException {
        log.debug("DECRYPT|REQUEST|decrypt: {}", data);
        Response<String> decryptResponse = decrypt.decrypt(data);
        log.debug("DECRYPT|RESPONSE|survey: {}", Json.prettyPrint(decryptResponse));
        audit.increment("decrypt." + decryptResponse.statusLine.getStatusCode());

        if (isError(decryptResponse.statusLine)) {
            throw new TransformException("decrypt response indicated an error: " + decryptResponse);
        }

        //TODO audit time taken

        return decryptResponse.body;
    }

    private boolean isError(StatusLine statusLine) {
        return statusLine.getStatusCode() != HttpStatus.OK_200;
    }

    public Audit getAudit() {
        return audit;
    }
}
