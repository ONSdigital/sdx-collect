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
import com.github.onsdigital.perkin.helper.Timer;
import com.github.onsdigital.perkin.json.*;
import com.github.onsdigital.perkin.transform.idbr.IdbrTransformer;
import com.github.onsdigital.perkin.transform.jpg.ImageTransformer;
import com.github.onsdigital.perkin.transform.pck.PckTransformer;
import com.github.onsdigital.perkin.publish.FtpPublisher;
import lombok.extern.slf4j.Slf4j;
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
    private NumberService batchNumberService = new NumberService("batch", 30000, 39999);
    private NumberService sequenceNumberService = new NumberService("sequence", 1000, 99999);
    private NumberService scanNumberService = new NumberService("scan", 1, 999999999);

    private TransformEngine() {
        //use getInstance()
        //TODO: make configurable
        //TODO: also, transformers on a per survey id basis?
        transformers = Arrays.asList(new IdbrTransformer(), new PckTransformer(), new ImageTransformer());
        templates = new HashMap<>();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                log.info("SHUTDOWN|storing sequence numbers...");
                batchNumberService.save();
                sequenceNumberService.save();
                scanNumberService.save();
            }
        });
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

            sendReceipt(survey);

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

    public TransformContext createTransformContext(Survey survey) throws TemplateNotFoundException {
        return TransformContext.builder()
                .date(new Date())
                .batch(batchNumberService.getNext())
                .sequence(sequenceNumberService.getNext())
                .scanNumberService(scanNumberService)
                .surveyTemplate(getSurveyTemplate(survey))
                .pdfTemplate(getPdfTemplate(survey))
                .build();
    }

    private String getPdfTemplate(Survey survey) throws TemplateNotFoundException {
        return getTemplate("templates/" + survey.getId() + "." + survey.getCollection().getInstrumentId() + ".pdf.fo");
    }

    private SurveyTemplate getSurveyTemplate(Survey survey) throws TemplateNotFoundException {

        String json = getTemplate("templates/" + survey.getId() + "." + survey.getCollection().getInstrumentId() + ".survey.json");
        return Serialiser.deserialise(json, SurveyTemplate.class);
    }

    private String getTemplate(String templateFilename) throws TemplateNotFoundException {

        //only time if we load the template
        Timer timer = null;

        String pdfTemplate = null;

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

    private Boolean sendReceipt(Survey survey) throws IOException {
        Timer timer = new Timer("receipt.");

        String receiptHost = ConfigurationManager.get("receipt.host");
        String receiptPath = ConfigurationManager.get("receipt.path");

        if (receiptHost.equals("skip")) {
            Audit.getInstance().increment("receipt.host.skipped");
            log.warn("RECEIPT|SKIP|skipping sending receipt to RM");
            return true;
        }

        String receiptURI = receiptPath + "/" + survey.getMetadata().getStatisticalUnitId() + "/collectionexercises/"
                + survey.getCollection().getExerciseSid() + "/receipts";

        Endpoint receiptEndpoint = new Endpoint(new Host(receiptHost), receiptURI);

        String receiptData = getTemplate("templates/receipt.xml");
        String respondentId = survey.getMetadata().getUserId();

        receiptData = receiptData.replace("{respondent_id}", respondentId);

        BasicNameValuePair applicationType = new BasicNameValuePair("Content-Type", "application/vnd.ons.receipt+xml");

        Response<String> receiptResponse = new Http().postString(receiptEndpoint, receiptData, applicationType);

        int status = receiptResponse.statusLine.getStatusCode();

        timer.stopStatus(status);

        audit.increment(timer);

        if (status == HttpStatus.BAD_REQUEST_400) {
            log.error("RECEIPT|RESPONSE|Failed for respondent: {}", respondentId);
        } else if (status != HttpStatus.CREATED_201) {
            throw new TransformException("receipt response indicated an error: " + receiptResponse);
        }

        return status == HttpStatus.CREATED_201;
    }

    private boolean isError(StatusLine statusLine) {
        return statusLine.getStatusCode() != HttpStatus.OK_200;
    }
}
