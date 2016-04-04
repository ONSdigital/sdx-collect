package com.github.onsdigital.perkin.transform;

import com.github.onsdigital.Json;
import com.github.onsdigital.perkin.decrypt.HttpDecrypt;
import com.github.onsdigital.perkin.helper.TemplateLoader;
import com.github.onsdigital.perkin.helper.Timer;
import com.github.onsdigital.perkin.json.*;
import com.github.onsdigital.perkin.transform.idbr.IdbrTransformer;
import com.github.onsdigital.perkin.transform.jpg.ImageTransformer;
import com.github.onsdigital.perkin.transform.pck.PckTransformer;
import com.github.onsdigital.perkin.publish.FtpPublisher;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.StatusLine;
import org.eclipse.jetty.http.HttpStatus;

import com.github.davidcarboni.httpino.Response;

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

    private Audit audit = Audit.getInstance();
    private TemplateLoader loader = TemplateLoader.getInstance();

    private List<Transformer> transformers;

    private FtpPublisher publisher = new FtpPublisher();

    private NumberService batchNumberService = new NumberService("batch", 30000, 39999);
    private NumberService sequenceNumberService = new NumberService("sequence", 1000, 99999);
    private NumberService scanNumberService = new NumberService("scan", 1, 999999999);

    private TransformEngine() {
        //use getInstance()
        //TODO: make configurable
        //TODO: also, transformers on a per survey id basis?
        transformers = Arrays.asList(new IdbrTransformer(), new PckTransformer(), new ImageTransformer());

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

            survey.sendReceipt();

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
                .surveyTemplate(loader.getSurveyTemplate(survey))
                .pdfTemplate(loader.getPdfTemplate(survey))
                .build();
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
