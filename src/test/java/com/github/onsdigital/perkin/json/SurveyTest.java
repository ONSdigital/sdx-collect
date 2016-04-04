package com.github.onsdigital.perkin.json;

import com.github.davidcarboni.httpino.Endpoint;
import com.github.davidcarboni.httpino.Host;
import com.github.davidcarboni.httpino.Response;
import com.github.onsdigital.ConfigurationManager;
import com.github.onsdigital.HttpManager;
import com.github.onsdigital.perkin.helper.FileHelper;
import com.github.onsdigital.perkin.helper.Http;
import com.github.onsdigital.perkin.transform.TemplateNotFoundException;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.Base64;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.mockito.Mockito.*;

/**
 * Created by ian on 01/04/2016.
 */
public class SurveyTest {

    private Survey testSurvey;

    private static final String RECEIPT_HOST = "http://localhost:5000";
    private static final String RECEIPT_PATH = "reportingunits";

    private static final String RECEIPT_USER = "test";
    private static final String RECEIPT_PASS = "test";

    @Before
    public void setUp() throws IOException {
        ConfigurationManager.set("RECEIPT_HOST", RECEIPT_HOST);
        ConfigurationManager.set("RECEIPT_PATH", RECEIPT_PATH);

        ConfigurationManager.set("RECEIPT_USER", RECEIPT_USER);
        ConfigurationManager.set("RECEIPT_PASS", RECEIPT_PASS);

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
        String receiptURI = RECEIPT_PATH + "/" + testSurvey.getMetadata().getStatisticalUnitId() + "/collectionexercises/"
                + testSurvey.getCollection().getExerciseSid() + "/receipts";

        Endpoint receiptEndpoint = new Endpoint(new Host(RECEIPT_HOST), receiptURI);

        assertThat(testSurvey.getReceiptEndpoint().toString(), is(receiptEndpoint.toString()));
    }

    @Test
    public void shouldSetContentTypeHeader() {
        String contentType = getHeaderValue("Content-Type");

        assertThat(contentType, is("application/vnd.collections+xml"));
    }

    @Test
    public void shouldSetAuthHeader() {
        String authHeader = getHeaderValue("Authorization");
        String auth = Base64.getEncoder().encodeToString((RECEIPT_USER + ":" + RECEIPT_PASS).getBytes());

        assertThat(authHeader, is("Basic " + auth));
    }

    @Test
    public void shouldContainRespondentId() throws TemplateNotFoundException {
        String content = testSurvey.getReceiptContent();

        String respondentId = content.split("<respondent_id>")[1].split("</respondent_id>")[0];

        assertThat(respondentId, is(testSurvey.getMetadata().getUserId()));
    }

    @Test
    public void hasWellFormedReceiptContent() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Parse will throw an exception if not well formed
        builder.parse(new InputSource(new StringReader(testSurvey.getReceiptContent())));
    }

    @Test
    public void shouldCallHttpWithCorrectParams() throws Exception {
        Http mockedHttp = mock(Http.class);
        HttpManager.setInstance(mockedHttp);

        Response mockResponse = new MockedResponse(mock(StatusLine.class), "Some Mock Response");

        when(mockedHttp.postString(any(), any(), (NameValuePair) anyVararg())).thenReturn(mockResponse);

        when(mockResponse.statusLine.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);

        testSurvey.sendReceipt();

        verify(mockedHttp).postString(testSurvey.getReceiptEndpoint(), testSurvey.getReceiptContent(),
                (NameValuePair[]) testSurvey.getReceiptHeaders());
    }
}
