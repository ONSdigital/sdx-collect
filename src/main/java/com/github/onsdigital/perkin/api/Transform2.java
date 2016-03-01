package com.github.onsdigital.perkin.api;

import com.github.davidcarboni.httpino.Response;
import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.perkin.helpers.HttpDecrypt;
import com.github.onsdigital.perkin.helpers.HttpPublisher;
import com.github.onsdigital.perkin.helpers.IdbrReceiptFactory;
import com.github.onsdigital.perkin.helpers.Json;
import com.github.onsdigital.perkin.json.EncryptedPayload;
import com.github.onsdigital.perkin.json.IdbrReceipt;
import com.github.onsdigital.perkin.json.Result;
import com.github.onsdigital.perkin.json.Survey;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.http.StatusLine;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicLong;

//TODO: remove this - just here to bypass encryption for now
/**
 * Transform a Survey into a format for downstream systems.
 */
@Api
public class Transform2 {

    //TODO: service/api for batchId
    private static AtomicLong batchId = new AtomicLong(35000);

    private HttpPublisher publisher = new HttpPublisher();
    private IdbrReceiptFactory idbrReceiptFactory = new IdbrReceiptFactory();

    @GET
    public Survey get(HttpServletRequest request, HttpServletResponse response) throws IOException {

        return Survey.builder()
                .id("244")
                .name("Monthly Wages and Salary Survey")
                .date("01 Oct 2014")
                .respondentId("99999994188")
                .respondentCheckLetter("F")
                .build();
    }

    @POST
    public Response<Result> transform(HttpServletRequest request, HttpServletResponse response, Survey survey) throws IOException, FileUploadException {

        System.out.println("transform >>>>>>>> request: " + Json.format(survey));

        IdbrReceipt receipt = idbrReceiptFactory.createIdbrReceipt(survey, batchId.getAndIncrement());
        System.out.println("transform created IDBR receipt: " + Json.format(receipt));

        Response<Result> result = publisher.publish(receipt);
        System.out.println("transform <<<<<<<< response: " + Json.format(result));

        if (result.body.isError()) {
            response.setStatus(result.statusLine.getStatusCode());
        }

        return result;
    }
}
