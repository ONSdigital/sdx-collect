package com.github.onsdigital.perkin.transform.jpg;

import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.transform.DataFile;
import com.github.onsdigital.perkin.transform.TransformException;
import com.github.onsdigital.perkin.transform.Transformer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Create one or more images representing the survey questions and answers.
 */
public class ImageBuilder implements Transformer {

    @Override
    public DataFile transform(final Survey survey, final long batchId) throws TransformException {
        byte[] pdf = createPdf(survey, batchId);

        return createImages(pdf, survey, batchId);
    }

    //TODO: change to throw TransformException
    //TODO: make it a runtime exception?

    private byte[] createPdf(final Survey survey, final long batchId) throws TransformException {
        PdfCreator pdfCreator = new PdfCreator();
        return pdfCreator.createPdf(survey);
    }

    public ImageInfo createImages(final byte[] pdf, final Survey survey, final long batchId) throws TransformException {

        ImageInfo.ImageInfoBuilder builder = ImageInfo.builder();

        try {
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
                baos.flush();
                baos.close();

                builder.image(
                        Image.builder()
                                //TODO: generate proper image filename (info from Rachel)
                                .filename(batchId + "_page" + i + ".jpg")
                                .data(baos.toByteArray())
                                .build()
                );

                System.out.println("created image: " + "page" + i + ".jpg");
                //TODO: create/append to image index csv
            }

            document.close();
        } catch (IOException e) {
            throw new TransformException("error creating images from pdf", e);
        }

        return builder.build();
    }
}
