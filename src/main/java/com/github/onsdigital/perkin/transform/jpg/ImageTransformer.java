package com.github.onsdigital.perkin.transform.jpg;

import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.transform.DataFile;
import com.github.onsdigital.perkin.transform.TransformContext;
import com.github.onsdigital.perkin.transform.TransformException;
import com.github.onsdigital.perkin.transform.Transformer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Create one or more images representing the survey questions and answers.
 */
public class ImageTransformer implements Transformer {

    @Override
    public List<DataFile> transform(final Survey survey, final TransformContext context) throws TransformException {
        PdfCreator pdfCreator = new PdfCreator();

        byte[] pdf = pdfCreator.createPdf(survey, context);

        return createImages(pdf, survey, context.getBatch());
    }

    private List<DataFile> createImages(final byte[] pdf, final Survey survey, final long batchId) throws TransformException {

        List<DataFile> files = new ArrayList<>();

        try {
            ByteArrayInputStream is = new ByteArrayInputStream(pdf);
            PDDocument document = PDDocument.load(is);

            List<PDPage> pages = document.getDocumentCatalog().getAllPages();
            int i = 0;
            for (PDPage page : pages) {
                i++;

                //convert pdf page to image
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int dpiImageResolution = 300;
                BufferedImage image = page.convertToImage(BufferedImage.TYPE_INT_RGB, dpiImageResolution);
                ImageIO.write(image, "JPG", baos);
                baos.flush();
                baos.close();

                files.add(
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

        return files;
    }
}
