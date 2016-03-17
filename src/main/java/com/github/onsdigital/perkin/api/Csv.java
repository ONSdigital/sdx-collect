package com.github.onsdigital.perkin.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.perkin.helper.FileHelper;
import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.json.SurveyParser;
import com.github.onsdigital.perkin.publish.FtpPublisher;
import com.github.onsdigital.perkin.transform.DataFile;
import com.github.onsdigital.perkin.transform.TransformContext;
import com.github.onsdigital.perkin.transform.TransformEngine;
import com.github.onsdigital.perkin.transform.jpg.Image;
import com.github.onsdigital.perkin.transform.jpg.ImageIndexCsv;
import com.github.onsdigital.perkin.transform.jpg.ImageTransformer;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.io.IOException;
import java.util.List;

@Api
@Slf4j
public class Csv {

    private FtpPublisher ftp = new FtpPublisher();

    // generate image from pdf, ftp save, ftp load, stream it
    @GET
    public String get(HttpServletRequest request, HttpServletResponse response) throws IOException {

        //spoof survey
        Survey survey = createSurvey();
        TransformContext context = TransformEngine.getInstance().createTransformContext(survey);

        //create image
        List<DataFile> files = new ImageTransformer().transform(survey, context);
        ImageIndexCsv csv = (ImageIndexCsv) files.get(1);
        log.info("csv >>>>>>>> generated csv: " + csv.getFilename() + " size: " + csv.getBytes().length);

        //save to ftp
        ftp.publish(files, null);

        //get back from ftp
        byte[] csvFromFtp = ftp.get(csv.getFilename());

        //stream image
        log.info("csv >>>>>>>> csv retrieved from FTP: " + csv.getFilename() + " size: " + csvFromFtp.length);
        response.setContentType("text/plain");
        response.setContentLength(csvFromFtp.length);

        ServletOutputStream out = response.getOutputStream();
        out.write(csvFromFtp);
        out.flush();

        return null;
    }

    private Survey createSurvey() throws IOException {
        return new SurveyParser().parse(FileHelper.loadFile("survey.json"));
    }
}
