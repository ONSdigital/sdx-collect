package com.github.onsdigital.perkin.transform.jpg;

import com.github.onsdigital.perkin.json.Survey;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Create one or more images representing the survey questions and answers.
 */
public class ImageBuilder {

    public ImageInfo createImages(final Survey survey, final long batchId) throws IOException {

        byte[] pdf = createPdf(survey, batchId);

        return createImages(pdf, survey, batchId);
    }

    //TODO: change to throw TransformException
    //TODO: make it a runtime exception?

    private byte[] createPdf(final Survey survey, final long batchId) throws IOException {
        //TODO: create pdf for the survey

        String filename = "to-jpg/2page.pdf";
        System.out.println("loading pdf:  " + filename);
        InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
        System.out.println("loaded pdf:  " + filename + " as: " + in);
        return IOUtils.toByteArray(in);
    }

    public ImageInfo createImages(final byte[] pdf, final Survey survey, final long batchId) throws IOException {

        ImageInfo.ImageInfoBuilder builder = ImageInfo.builder();

        ByteArrayInputStream is = new ByteArrayInputStream(pdf);
        PDDocument document = PDDocument.load(is);

        List<PDPage> pages = document.getDocumentCatalog().getAllPages();
        int i = 0;
        for (PDPage page : pages) {
            i++;

            //TODO: convert pdf page to image
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int dpiImageResolution = 300;
            BufferedImage image = page.convertToImage(BufferedImage.TYPE_INT_RGB, dpiImageResolution);
            ImageIO.write(image, "JPG", baos);

            builder.image(
                    Image.builder()
                            //TODO: generate proper image filename (info from Rachel)
                            .filename("page" + i + ".jpg")
                            .data(baos.toByteArray())
                            .build()
            );

            System.out.println("created image: " + "page" + i + ".jpg");
            //TODO: create/append to image index csv
        }

        return builder.build();
    }
}
