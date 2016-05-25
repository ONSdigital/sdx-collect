package com.github.onsdigital.perkin.transform;

import com.github.onsdigital.ConfigurationManager;
import com.github.onsdigital.perkin.decrypt.Decrypt;
import com.github.onsdigital.perkin.helper.TemplateLoader;
import com.github.onsdigital.perkin.helper.Timer;
import com.github.onsdigital.perkin.json.*;
import com.github.onsdigital.perkin.transform.idbr.IdbrTransformer;
import com.github.onsdigital.perkin.transform.jpg.ImageTransformer;
import com.github.onsdigital.perkin.transform.pck.PckTransformer;
import com.github.onsdigital.perkin.publish.FtpPublisher;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Transform a Survey into a format for downstream systems.
 */
@Slf4j
public class TransformEngine {

    private static TransformEngine INSTANCE = new TransformEngine();

    private SurveyParser parser = new SurveyParser();
    private Decrypt decrypt;

    private Audit audit = Audit.getInstance();
    private TemplateLoader loader = TemplateLoader.getInstance();

    //private List<Transformer> transformers;

    private FtpPublisher publisher = new FtpPublisher();

    private NumberService batchNumberService = new NumberService("batch", 30000, 39999);
    private NumberService sequenceNumberService = new NumberService("sequence", 1000, 99999);
    private NumberService scanNumberService = new NumberService("scan", 1, 999999999);

    private TransformEngine() {
        //use getInstance()

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

    public List<DataFile> transform(final String data) throws TransformException, InterruptedException, ExecutionException {

        Timer timer = new Timer("survey.process.");

        ExecutorService taskExecutor = Executors.newFixedThreadPool(4);
        CountDownLatch latch = new CountDownLatch(4);

        try {
            decrypt = new Decrypt(data);
            String json = decrypt.getDecrypted();
            Survey survey = parser.parse(json);

            TransformContext context = createTransformContext(survey);

            List<Callable<List<DataFile>>> tasks = new ArrayList<>();
            List<DataFile> files = new ArrayList<>();

            //TODO: make configurable per survey
            //TOOO: put latch and survey in context?
            tasks.add(new IdbrTransformer(survey, context, latch));
            tasks.add(new PckTransformer(survey, context, latch));
            tasks.add(new ImageTransformer(survey, context, latch));

            List<Future<List<DataFile>>> futures = taskExecutor.invokeAll(tasks);

            for (Future<List<DataFile>> future : futures) {
                files.addAll(future.get());
            }

            //TODO: multithreading for the ftp as well? - probably most to gain here
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

        MDC.clear();
        MDC.put("statUnitId", survey.getMetadata().getStatisticalUnitId());
        MDC.put("userId", survey.getMetadata().getUserId());
        MDC.put("exerciseSid", survey.getCollection().getExerciseSid());

        String env = ConfigurationManager.get("SDX_ENV");
        log.debug("TRANSFORM|SDX_ENV:{}", env);

        return TransformContext.builder()
                .date(new Date())
                .batch(batchNumberService.getNext())
                .sequence(sequenceNumberService.getNext())
                .scanNumberService(scanNumberService)
                .surveyTemplate(loader.getSurveyTemplate(survey))
                .pdfTemplate(loader.getPdfTemplate(survey))
                .idbrPath("\\\\NP3RVWAPXX370\\SDX_" + env + "\\EDC_QReceipts\\")
                .pckPath("\\\\NP3RVWAPXX370\\SDX_" + env + "\\EDC_QData\\")
                .imagePath("\\\\NP3RVWAPXX370\\SDX_" + env + "\\EDC_QImages\\Images\\")
                .indexPath("\\\\NP3RVWAPXX370\\SDX_" + env + "\\EDC_QImages\\Index\\")
                .build();
    }
}
