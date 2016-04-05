package com.github.onsdigital.perkin.json;

import com.github.davidcarboni.httpino.Endpoint;
import com.github.davidcarboni.httpino.Host;
import com.github.davidcarboni.httpino.Response;
import com.github.onsdigital.ConfigurationManager;
import com.github.onsdigital.HttpManager;
import com.github.onsdigital.perkin.helper.Http;
import com.github.onsdigital.perkin.helper.TemplateLoader;
import com.github.onsdigital.perkin.helper.Timer;
import com.github.onsdigital.perkin.transform.Audit;
import com.github.onsdigital.perkin.transform.TemplateNotFoundException;
import com.github.onsdigital.perkin.transform.TransformException;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
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

    public NameValuePair[] getReceiptHeaders() {
        String receiptUser = ConfigurationManager.get("RECEIPT_USER");
        String receiptPass = ConfigurationManager.get("RECEIPT_PASS");

        NameValuePair[] headers = new NameValuePair[2];

        String auth = Base64.getEncoder().encodeToString((receiptUser + ":" + receiptPass).getBytes());

        headers[0] = new BasicNameValuePair("Authorization", "Basic " + auth);
        headers[1] = new BasicNameValuePair("Content-Type", "application/vnd.collections+xml");

        log.debug("RECEIPT|AUTH: {}:***", receiptUser);
        log.debug("RECEIPT|AUTH.ENCODED: {}", auth);

        return headers;
    }

    public Endpoint getReceiptEndpoint() {

        String receiptHost = ConfigurationManager.get("RECEIPT_HOST");
        String receiptPath = ConfigurationManager.get("RECEIPT_PATH");

        String receiptURI = receiptPath + "/" + this.getMetadata().getStatisticalUnitId() + "/collectionexercises/"
                + this.getCollection().getExerciseSid() + "/receipts";

        log.debug("RECEIPT|HOST/PATH: {}/{}", receiptHost, receiptPath);

        return new Endpoint(new Host(receiptHost), receiptURI);
    }

    public String getReceiptContent() throws TemplateNotFoundException {
        TemplateLoader loader = TemplateLoader.getInstance();
        String respondentId = this.getMetadata().getUserId();
        String template = loader.getTemplate("templates/receipt.xml");

        return template.replace("{respondent_id}", respondentId);
    }

    public Boolean sendReceipt() throws IOException {
        Timer timer = new Timer("receipt.");
        Audit audit = Audit.getInstance();

        String receiptHost = ConfigurationManager.get("RECEIPT_HOST");

        if (receiptHost.equals("skip")) {
            Audit.getInstance().increment("receipt.host.skipped");
            log.warn("RECEIPT|SKIP|skipping sending receipt to RM");
            return true;
        }

        Http http = HttpManager.getInstance();
        Response receiptResponse = http.postString(this.getReceiptEndpoint(),
                this.getReceiptContent(), this.getReceiptHeaders());

        int status = receiptResponse.statusLine.getStatusCode();

        timer.stopStatus(status);

        audit.increment(timer);

        if (status == HttpStatus.BAD_REQUEST_400) {
            log.error("RECEIPT|RESPONSE|Failed for respondent: {}", this.getMetadata().getUserId());
        } else if (status != HttpStatus.CREATED_201) {
            throw new TransformException("receipt response indicated an error: " + receiptResponse);
        }

        return status == HttpStatus.CREATED_201;
    }
}