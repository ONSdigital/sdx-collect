package com.github.onsdigital.perkin.json;

import com.github.davidcarboni.httpino.Endpoint;
import com.github.davidcarboni.httpino.Host;
import com.github.onsdigital.ConfigurationManager;
import com.github.onsdigital.perkin.helper.Http;
import com.github.onsdigital.perkin.helper.TemplateLoader;
import com.github.onsdigital.perkin.helper.Timer;
import com.github.onsdigital.perkin.transform.Audit;
import com.github.onsdigital.perkin.transform.TemplateNotFoundException;
import com.github.onsdigital.perkin.transform.TransformException;
import com.github.davidcarboni.httpino.Response;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
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

    public BasicNameValuePair[] getReceiptHeaders() {
        BasicNameValuePair[] headers = new BasicNameValuePair[1];

        headers[0] = new BasicNameValuePair("Content-Type", "application/vnd.ons.receipt+xml");

        return headers;
    }

    public Endpoint getReceiptEndpoint() {

        String receiptHost = ConfigurationManager.get("receipt.host");
        String receiptPath = ConfigurationManager.get("receipt.path");

        String receiptURI = receiptPath + "/" + this.getMetadata().getRuRef() + "/collectionexercises/"
                + this.getCollection().getExerciseSid() + "/receipts";

        return new Endpoint(new Host(receiptHost), receiptURI);
    }

    public String getReceiptContent() throws TemplateNotFoundException {
        TemplateLoader loader = TemplateLoader.getInstance();

        return loader.getTemplate("templates/receipt.xml");
    }

    public Boolean sendReceipt() throws IOException {
        Timer timer = new Timer("receipt.");
        Audit audit = Audit.getInstance();
        TemplateLoader loader = TemplateLoader.getInstance();

        String receiptHost = ConfigurationManager.get("receipt.host");

        if (receiptHost.equals("skip")) {
            Audit.getInstance().increment("receipt.host.skipped");
            log.warn("RECEIPT|SKIP|skipping sending receipt to RM");
            return true;
        }

        String receiptData = this.getReceiptContent();
        String respondentId = this.getMetadata().getUserId();

        receiptData = receiptData.replace("{respondent_id}", respondentId);

        Response<String> receiptResponse = new Http().postString(this.getReceiptEndpoint(), receiptData,
                this.getReceiptHeaders());

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