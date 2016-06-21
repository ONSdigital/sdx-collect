package com.github.onsdigital.perkin.transform;

import com.github.davidcarboni.httpino.Response;
import com.github.onsdigital.perkin.decrypt.Decrypt;
import com.github.onsdigital.perkin.helper.SdxValidate;
import com.github.onsdigital.perkin.helper.Timer;
import com.github.onsdigital.perkin.json.*;

import com.github.onsdigital.perkin.helper.SdxStore;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.*;

/**
 * Transform a Survey into a format for downstream systems.
 */
@Slf4j
public class TransformEngine {

    private static TransformEngine INSTANCE = new TransformEngine();

    private SdxValidate validate = new SdxValidate();
    private SdxStore store = new SdxStore();
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

        try {
            decrypt = new Decrypt(data);
            String json = decrypt.getDecrypted();

            Response<String> response = validate.validate(json);
            switch (response.statusLine.getStatusCode()) {
                case 400:
                    //TODO: do we want to store this on another failed queue?
                case 500:
                    throw new TransformException("Problem validating json: " + response.toString());
            }

            Survey survey = Survey.deserialize(json);

            store.store(json);

            survey.sendReceipt();

            timer.stopStatus(200);

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
