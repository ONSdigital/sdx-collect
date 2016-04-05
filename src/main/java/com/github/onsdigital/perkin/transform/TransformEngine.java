package com.github.onsdigital.perkin.transform;

import com.github.onsdigital.perkin.decrypt.Decryption;
import com.github.onsdigital.perkin.helper.TemplateLoader;
import com.github.onsdigital.perkin.helper.Timer;
import com.github.onsdigital.perkin.json.*;
import com.github.onsdigital.perkin.transform.idbr.IdbrTransformer;
import com.github.onsdigital.perkin.transform.jpg.ImageTransformer;
import com.github.onsdigital.perkin.transform.pck.PckTransformer;
import com.github.onsdigital.perkin.publish.FtpPublisher;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

/**
 * Transform a Survey into a format for downstream systems.
 */
@Slf4j
public class TransformEngine {

    private static TransformEngine INSTANCE = new TransformEngine();

    private SurveyParser parser = new SurveyParser();
    private Decryption decrypt;

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
            decrypt = new Decryption(data);
            String json = decrypt.getDecrypted();
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
}
