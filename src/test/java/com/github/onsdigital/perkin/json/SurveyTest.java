package com.github.onsdigital.perkin.json;

import com.github.davidcarboni.httpino.Endpoint;
import com.github.davidcarboni.httpino.Host;
import com.github.onsdigital.ConfigurationManager;
import com.github.onsdigital.perkin.helper.FileHelper;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.mockito.Mockito.*;

/**
 * Created by ian on 01/04/2016.
 */
public class SurveyTest {

    Survey testSurvey;

    @Before
    public void setUp() throws IOException {
        ConfigurationManager.set("receipt.host", "http://localhost:5000");
        ConfigurationManager.set("receipt.path", "reportingunits");

        testSurvey = new SurveyParser().parse(FileHelper.loadFile("survey.ftp.json"));
    }

    /**
     * Retrieve a receipt header
     * @param key The key to return
     * @return The value for the key
     */
    public String getHeaderValue(String key) {
        BasicNameValuePair[] headers = testSurvey.getReceiptHeaders();

        for(BasicNameValuePair header: headers) {
            if(header.getName() == key) {
                return header.getValue();
            }
        }
        return null;
    }

    @Test
    public void shouldUseCorrectEndpoint() {
        String receiptHost = ConfigurationManager.get("receipt.host");
        String receiptPath = ConfigurationManager.get("receipt.path");

        String receiptURI = receiptPath + "/" + testSurvey.getMetadata().getRuRef() + "/collectionexercises/"
                + testSurvey.getCollection().getExerciseSid() + "/receipts";

        Endpoint receiptEndpoint = new Endpoint(new Host(receiptHost), receiptURI);

        assertThat(testSurvey.getReceiptEndpoint().toString(), is(receiptEndpoint.toString()));
    }

    @Test
    public void shouldSetContentTypeHeader() {
        String contentType = getHeaderValue("Content-Type");

        assertThat(contentType, is("application/vnd.ons.receipt+xml"));
    }

    @Test
    public void hasWellFormedReceiptContent() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Parse will throw an exception if not well formed
        builder.parse(new InputSource(new StringReader(testSurvey.getReceiptContent())));
    }
}
