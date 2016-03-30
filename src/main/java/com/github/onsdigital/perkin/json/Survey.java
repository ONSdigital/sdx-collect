package com.github.onsdigital.perkin.json;

import com.github.davidcarboni.httpino.Endpoint;
import com.github.davidcarboni.httpino.Host;
import com.github.onsdigital.ConfigurationManager;
import com.github.onsdigital.perkin.helper.Http;
import com.github.onsdigital.perkin.helper.TemplateLoader;
import com.github.onsdigital.perkin.helper.Timer;
import com.github.onsdigital.perkin.transform.Audit;
import com.github.onsdigital.perkin.transform.TransformException;
import com.github.davidcarboni.httpino.Response;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.jetty.http.HttpStatus;
import java.io.IOException;

/**
 * Simple object to contain Survey data.
 */
@Data
@Builder
@Slf4j
public class Survey {

    private String type;
    private String version;
    private String origin;
    @SerializedName("survey_id")
    private String id;

    private Collection collection;

    @SerializedName("submitted_at")
    private Date date;

    private Metadata metadata;

    @Singular("answer")
    @SerializedName("data")
    private Map<String, String> answers;

    public String getAnswer(String key) {
        if (answers == null) {
            return null;
        }

        return answers.get(key);
    }

    public Set<String> getKeys() {
        if (answers == null) {
            return Collections.emptySet();
        }

        return answers.keySet();
    }

    public Boolean sendReceipt() throws IOException {
        Timer timer = new Timer("receipt.");
        Audit audit = Audit.getInstance();
        TemplateLoader loader = TemplateLoader.getInstance();

        String receiptHost = ConfigurationManager.get("receipt.host");
        String receiptPath = ConfigurationManager.get("receipt.path");

        if (receiptHost.equals("skip")) {
            Audit.getInstance().increment("receipt.host.skipped");
            log.warn("RECEIPT|SKIP|skipping sending receipt to RM");
            return true;
        }

        String receiptURI = receiptPath + "/" + this.getMetadata().getRuRef() + "/collectionexercises/"
                + this.getCollection().getExerciseSid() + "/receipts";

        Endpoint receiptEndpoint = new Endpoint(new Host(receiptHost), receiptURI);

        String receiptData = loader.getTemplate("templates/receipt.xml");
        String respondentId = this.getMetadata().getUserId();

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
}