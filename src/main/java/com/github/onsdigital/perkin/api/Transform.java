package com.github.onsdigital.perkin.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.perkin.helper.FileHelper;
import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.json.SurveyParser;
import com.github.onsdigital.perkin.publish.FtpPublisher;
import com.github.onsdigital.perkin.transform.DataFile;
import com.github.onsdigital.perkin.transform.TransformEngine;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.io.IOException;
import java.util.List;

@Api
@Slf4j
public class Transform {

    private FtpPublisher ftp = new FtpPublisher();

    //TODO: disable by default - testing tool
    // transform survey.ftp.json, save files via ftp, retrieve the given file extension and display
    @GET
    public String get(HttpServletRequest request, HttpServletResponse response) throws IOException {

        //which file to display
        String ext = request.getParameter("ext");
        if (ext != null) {
            log.info("** displaying file extension: " + ext);
        } else {
            ext = ".jpg";
        }

        String mime = "text/plain";
        if (ext.equalsIgnoreCase("jpg")) {
            mime = "image/jpeg";
        }

        //spoof survey
        Survey survey = createSurvey();
        String surveyId = request.getParameter("survey");
        if (surveyId != null) {
            log.info("** setting surveyId to: " + surveyId);
            survey.setId(surveyId);
        }
        String formType = request.getParameter("form");
        if (formType != null) {
            log.info("** setting formType to: " + formType);
            survey.getCollection().setInstrumentId(formType);
        }

        //transform
        String data = new SurveyParser().prettyPrint(survey);
        List<DataFile> files = TransformEngine.getInstance().transform(data);

        //output the first image
        for (DataFile file : files) {
            if (file.getFilename().endsWith(ext)) {
                log.info("ftp >>>>>>>> getting file from ftp image: " + file.getFilename() + " size: " + file.getBytes().length);

                //get back from ftp
                byte[] bytes = ftp.get(file.getFilename());

                //stream image
                log.info("ftp >>>>>>>> file retrieved from FTP: " + file.getFilename() + " size: " + bytes.length);
                response.setContentType(mime);
                response.setContentLength(bytes.length);

                ServletOutputStream out = response.getOutputStream();
                out.write(bytes);
                out.flush();
                break;
            }
        }

        return null;
    }

    private Survey createSurvey() throws IOException {
        return new SurveyParser().parse(FileHelper.loadFile("survey.ftp.json"));
    }
}
