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

            //TODO will this throw an Exception or use a return value?
            //{"valid": True} 200
            //{"valid": False} 400?
            Response<Result> response = validate.validate(json);
            //TODO check the result of the validation

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
