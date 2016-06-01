package com.github.onsdigital.perkin.transform;

import com.github.onsdigital.perkin.decrypt.Decrypt;
import com.github.onsdigital.perkin.helper.Timer;
import com.github.onsdigital.perkin.json.*;

import com.github.onsdigital.perkin.store.StoreJson;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.*;

/**
 * Transform a Survey into a format for downstream systems.
 */
@Slf4j
public class TransformEngine {

    private static TransformEngine INSTANCE = new TransformEngine();

    private SurveyParser parser = new SurveyParser();
    private StoreJson store = new StoreJson();
    private Decrypt decrypt;

    private Audit audit = Audit.getInstance();

    private TransformEngine() {
        //use getInstance()
    }

    public static TransformEngine getInstance() {
        return INSTANCE;
    }

    public void transform(final String data) throws TransformException, InterruptedException, ExecutionException {

        Timer timer = new Timer("survey.process.");

        ExecutorService taskExecutor = Executors.newFixedThreadPool(4);
        CountDownLatch latch = new CountDownLatch(4);

        try {
            decrypt = new Decrypt(data);
            String json = decrypt.getDecrypted();

            Survey survey = parser.parse(json);

            store.store(survey);
            survey.sendReceipt();

            timer.stopStatus(200);

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
}
