package com.github.onsdigital.perkin.api;

import com.github.davidcarboni.restolino.framework.Api;
import com.github.onsdigital.perkin.transform.jpg.ImageBuilder;
import com.github.onsdigital.perkin.transform.jpg.ImageInfo;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.io.IOException;

@Api
public class Image {

    // generate image from pdf and stream it
    @GET
    public String get(HttpServletRequest request, HttpServletResponse response) throws IOException {

        ImageInfo imageInfo = new ImageBuilder().createImages(null, -1L);
        com.github.onsdigital.perkin.transform.jpg.Image image = imageInfo.getImages().get(0);

        System.out.println("image >>>>>>>> stream image: " + image.getFilename() + " size: " + image.getData().length);
        response.setContentType("image/jpeg");
        response.setContentLength(image.getData().length);

        ServletOutputStream out = response.getOutputStream();
        out.write(image.getData());
        out.flush();

        return null;
    }
}
