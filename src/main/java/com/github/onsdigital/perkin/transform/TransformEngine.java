package com.github.onsdigital.perkin.transform;

import com.github.davidcarboni.httpino.Response;
import com.github.davidcarboni.httpino.Serialiser;
import com.github.onsdigital.Json;
import com.github.onsdigital.perkin.decrypt.HttpDecrypt;
import com.github.onsdigital.perkin.helper.FileHelper;
import com.github.onsdigital.perkin.helper.Timer;
import com.github.onsdigital.perkin.json.*;
import com.github.onsdigital.perkin.transform.idbr.IdbrTransformer;
import com.github.onsdigital.perkin.transform.jpg.ImageTransformer;
import com.github.onsdigital.perkin.transform.pck.PckTransformer;
import com.github.onsdigital.perkin.publish.FtpPublisher;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.StatusLine;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.util.*;

/**
 * Transform a Survey into a format for downstream systems.
 */
@Slf4j
public class TransformEngine {

    private static TransformEngine INSTANCE = new TransformEngine();

    private SurveyParser parser = new SurveyParser();
    private HttpDecrypt decrypt = new HttpDecrypt();

    private Map<String, String> templates;
    private List<Transformer> transformers;

    private FtpPublisher publisher = new FtpPublisher();

    private Audit audit = Audit.getInstance();
    private BatchNumberService batchNumberService = new BatchNumberService();
    private SequenceNumberService sequenceNumberService = new SequenceNumberService();

    private TransformEngine() {
        //use getInstance()
        //TODO: make configurable
        //TODO: also, transformers on a per survey id basis?
        transformers = Arrays.asList(new IdbrTransformer(), new PckTransformer(), new ImageTransformer());
        templates = new HashMap<>();
    }

    public static TransformEngine getInstance() {
        return INSTANCE;
    }

    public List<DataFile> transform(final String data) throws TransformException {

        Timer timer = new Timer("survey.process.");

        try {
            //TODO: move into decrypt and make configurable
            String json;
            if (data != null && data.trim().startsWith("{")) {
                audit.increment("surveys.plaintext");
                log.info("DECRYPT|SKIPPING|json is plain text, not encrypted: {}", data);
                json = data;
            } else {
                json = decrypt(data);
            }
            Survey survey = parser.parse(json);

            TransformContext context = createTransformContext(survey);

            List<DataFile> files = new ArrayList<>();
            //TODO: use executors (multithreading)
            for (Transformer transformer : transformers) {
                files.addAll(transformer.transform(survey, context));
            }

            publisher.publish(files);

            timer.stopStatus(200);
            return files;
        } catch (SurveyParserException e) {
            timer.stopStatus(400, e);
            throw e;
        } catch (TransformException e) {
            timer.stopStatus(500, e);
            throw e;
        } catch (IOException e) {
            timer.stopStatus(500, e);
            throw new TransformException("Problem transforming survey", e);
        } finally {
            audit.increment(timer);
        }
    }

    //TODO: make private
    public TransformContext createTransformContext(Survey survey) throws TemplateNotFoundException {
        return TransformContext.builder()
                .batch(batchNumberService.getNext())
                .sequence(sequenceNumberService.getNext())
                .surveyTemplate(getSurveyTemplate(survey))
                .pdfTemplate(getPdfTemplate(survey))
                .build();
    }

    private String getPdfTemplate(Survey survey) throws TemplateNotFoundException {

        //only time if we load the template
        Timer timer = null;

        String pdfTemplate = null;
        String templateFilename = "templates/" + survey.getId() + "." + survey.getCollection().getInstrumentId() + ".pdf.fo";

        try {
            //only load a template once
            pdfTemplate = templates.get(templateFilename);
            if (pdfTemplate == null) {

                timer = new Timer("template.pdf.load.");
                timer.addInfo(templateFilename);

                pdfTemplate = FileHelper.loadFile(templateFilename);
                templates.put(templateFilename, pdfTemplate);

                timer.stopStatus(200);
                log.debug("TEMPLATE|storing template: " + templateFilename);
            }
        } catch (IOException e) {
            if (timer != null) {
                timer.stopStatus(500, e);
            }
            throw new TemplateNotFoundException("problem loading pdf template: " + templateFilename);
        } finally {
            audit.increment(timer);
        }

        return pdfTemplate;
    }

    private SurveyTemplate getSurveyTemplate(Survey survey) throws TemplateNotFoundException {

        //only time if we load the template
        Timer timer = null;

        //only load a template once
        String templateFilename = "templates/" + survey.getId() + "." + survey.getCollection().getInstrumentId() + ".survey.json";

        try {
            String json = templates.get(templateFilename);
            if (json == null) {
                timer = new Timer("template.survey.load.");
                timer.addInfo(templateFilename);

                json = FileHelper.loadFile(templateFilename);
                templates.put(templateFilename, json);

                timer.stopStatus(200);
                log.debug("TEMPLATE|storing template: " + templateFilename);
            }
            return Serialiser.deserialise(json, SurveyTemplate.class);
        } catch (IOException e) {
            if (timer != null ) {
                timer.stopStatus(500, e);
            }
            throw new TemplateNotFoundException(templateFilename, e);
        } finally {
            audit.increment(timer);
        }
    }

    private String decrypt(String data) throws IOException {
        log.debug("DECRYPT|REQUEST|decrypt: {}", data);

        Timer timer = new Timer("decrypt.");
        Response<String> decryptResponse = decrypt.decrypt(data);
        timer.stopStatus(decryptResponse.statusLine.getStatusCode());
        audit.increment(timer);

        log.debug("DECRYPT|RESPONSE|survey: {}", Json.prettyPrint(decryptResponse));

        if (isError(decryptResponse.statusLine)) {
            throw new TransformException("decrypt response indicated an error: " + decryptResponse);
        }

        return decryptResponse.body;
    }

    private boolean isError(StatusLine statusLine) {
        return statusLine.getStatusCode() != HttpStatus.OK_200;
    }
}
