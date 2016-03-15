package com.github.onsdigital.perkin.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.perkin.helper.FileHelper;
import com.github.onsdigital.perkin.json.Survey2;
import com.github.onsdigital.perkin.json.SurveyParser;
import com.github.onsdigital.perkin.publish.FtpPublisher;
import com.github.onsdigital.perkin.transform.DataFile;
import com.github.onsdigital.perkin.transform.TransformContext;
import com.github.onsdigital.perkin.transform.TransformEngine;
import com.github.onsdigital.perkin.transform.jpg.*;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.io.IOException;
import java.util.List;

@Api
@Slf4j
public class Ftp {

    private FtpPublisher ftp = new FtpPublisher();

    // generate image from pdf, ftp save, ftp load, stream it
    @GET
    public String get(HttpServletRequest request, HttpServletResponse response) throws IOException {

        //spoof survey
        Survey2 survey = createSurvey2();
        TransformContext context = TransformEngine.getInstance().createTransformContext(survey);

        //create image
        List<DataFile> files = new ImageTransformer().transform(survey, context);
        com.github.onsdigital.perkin.transform.jpg.Image image = (Image) files.get(0);
        log.info("image >>>>>>>> generated image: " + image.getFilename() + " size: " + image.getData().length);

        //save to ftp
        ftp.publish(files);

        //get back from ftp
        byte[] imageFromFtp = ftp.get(image.getFilename());

        //stream image
        log.info("image >>>>>>>> image retrieved from FTP: " + image.getFilename() + " size: " + imageFromFtp.length);
        response.setContentType("image/jpeg");
        response.setContentLength(imageFromFtp.length);

        ServletOutputStream out = response.getOutputStream();
        out.write(imageFromFtp);
        out.flush();

        return null;
    }

    private com.github.onsdigital.perkin.json.Survey2 createSurvey2() throws IOException {
        return new SurveyParser().parse(FileHelper.loadFile("survey2.json"));
    }
}
