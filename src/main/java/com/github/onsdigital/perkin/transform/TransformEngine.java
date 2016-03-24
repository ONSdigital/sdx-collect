package com.github.onsdigital.perkin.transform;

import com.github.davidcarboni.httpino.Endpoint;
import com.github.davidcarboni.httpino.Host;
import com.github.davidcarboni.httpino.Response;
import com.github.davidcarboni.httpino.Serialiser;
import com.github.onsdigital.ConfigurationManager;
import com.github.onsdigital.Json;
import com.github.onsdigital.perkin.decrypt.HttpDecrypt;
import com.github.onsdigital.perkin.helper.FileHelper;
import com.github.onsdigital.perkin.helper.Http;
import com.github.onsdigital.perkin.json.*;
import com.github.onsdigital.perkin.transform.idbr.IdbrTransformer;
import com.github.onsdigital.perkin.transform.jpg.ImageTransformer;
import com.github.onsdigital.perkin.transform.pck.PckTransformer;
import com.github.onsdigital.perkin.publish.FtpPublisher;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicNameValuePair;
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

        try {
            audit.increment("surveys");

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

            //publisher.publish(files);

            sendReceipt(survey);

            audit.increment("transform.200");

            return files;
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
        String templateFilename = "templates/" + survey.getId() + "." + survey.getCollection().getInstrumentId() + ".pdf.fo";

        try {
            //only load a template once
            pdfTemplate = templates.get(templateFilename);
            if (pdfTemplate == null) {
                pdfTemplate = FileHelper.loadFile(templateFilename);
                log.debug("TEMPLATE|storing template: " + templateFilename);
                templates.put(templateFilename, pdfTemplate);
            }
        } catch (IOException e) {
            throw new TemplateNotFoundException("problem loading pdf template: " + templateFilename);
        }

        return pdfTemplate;
    }

    private SurveyTemplate getSurveyTemplate(Survey survey) throws TemplateNotFoundException {

        //only load a template once
        String templateFilename = "templates/" + survey.getId() + "." + survey.getCollection().getInstrumentId() + ".survey.json";
        try {
            String json = templates.get(templateFilename);
            if (json == null) {
                json = FileHelper.loadFile(templateFilename);
                log.debug("TEMPLATE|storing template: " + templateFilename);
                templates.put(templateFilename, json);
            }
            return Serialiser.deserialise(json, SurveyTemplate.class);
        } catch (IOException e) {
            throw new TemplateNotFoundException(templateFilename, e);
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

    private Boolean sendReceipt(Survey survey) throws IOException {

        String receiptHost = ConfigurationManager.get("receipt.host");
        String receiptPath = ConfigurationManager.get("receipt.path");

        String receiptURI = receiptPath + "/" + survey.getMetadata().getRuRef() + "/collectionexercises/"
                + survey.getCollection().getExerciseSid() + "/receipts";

        Endpoint receiptEndpoint = new Endpoint(new Host(receiptHost), receiptURI);

        String receiptData = FileHelper.loadFile("receipt.xml");
        receiptData = receiptData.replace("{respondent_id}", survey.getMetadata().getUserId());

        BasicNameValuePair applicationType = new BasicNameValuePair("Content-Type", "application/vnd.ons.receipt+xml");

        Response<String> receiptResponse = new Http().postString(receiptEndpoint, receiptData, applicationType);

        boolean success = receiptResponse.statusLine.getStatusCode() == HttpStatus.CREATED_201;

        if (!success) {
            throw new TransformException("receipt response indicated an error: " + receiptResponse);
        }

        return success;
    }

    private boolean isError(StatusLine statusLine) {
        return statusLine.getStatusCode() != HttpStatus.OK_200;
    }
}
