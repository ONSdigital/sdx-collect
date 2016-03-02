package com.github.onsdigital.perkin.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.perkin.helpers.Json;
import com.github.onsdigital.perkin.json.FtpInfo;
import com.github.onsdigital.perkin.storage.FtpPublisher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.io.IOException;

/**
 * List files in ftp.
 */
@Api
public class Get {

    private FtpPublisher ftp = new FtpPublisher();

    @GET
    public String get(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String filename = request.getParameter("filename");
        System.out.println("ftp >>>>>>>> get filename: " + filename);
        
        String contents = ftp.get(filename);
        System.out.println("ftp >>>>>>>> get filename: " + filename + " contents: " + contents);

        return contents;
    }
}
