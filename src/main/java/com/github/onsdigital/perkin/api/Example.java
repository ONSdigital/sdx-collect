package com.github.onsdigital.perkin.api;

import com.github.davidcarboni.restolino.framework.Api;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import java.io.IOException;

/**
 * Example API.
 */
@Api
public class Example {

    // NB this value will be reinitialised on every reload of the code:
    public static String message = "Hello world";

    @GET
    public String message(HttpServletRequest request, HttpServletResponse response) throws IOException, FileUploadException {
        return message;
    }

    @POST
    public String updateMessage(HttpServletRequest request, HttpServletResponse response) throws IOException, FileUploadException {

        String result = request.getParameter("message");
        if (StringUtils.isNotBlank(message)) {
            message = result;
        } else {
            response.setStatus(HttpStatus.BAD_REQUEST_400);
            result = "Please provide a 'message' parameter.";
        }

        return result;
    }
}
