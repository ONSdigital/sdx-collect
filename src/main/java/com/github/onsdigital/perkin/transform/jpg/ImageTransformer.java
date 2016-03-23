package com.github.onsdigital.perkin.transform.jpg;

import com.github.onsdigital.perkin.helper.Timer;
import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.transform.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
@Slf4j
public class ImageTransformer implements Transformer {

    @Override
    public List<DataFile> transform(final Survey survey, final TransformContext context) throws TransformException {

        Timer timer = new Timer("transform.images.");

        PdfCreator pdfCreator = new PdfCreator();

        byte[] pdf = pdfCreator.createPdf(survey, context);

        List<DataFile> images = createImages(pdf, survey, context.getBatch());

        timer.stopStatus(200);
        Audit.getInstance().increment(timer);

        return images;
    }

    private List<DataFile> createImages(final byte[] pdf, final Survey survey, final long batch) throws TransformException {

        List<DataFile> files = new ArrayList<>();
        ImageIndexCsvCreator csvCreator = new ImageIndexCsvCreator();

        //TODO: persist the scan number, increment it each time
        int scanNumber = 1;
        String scanId = "S" + StringUtils.leftPad("" + scanNumber, 9, '0');

        try {
            //TODO can this be re-used?
            ByteArrayInputStream is = new ByteArrayInputStream(pdf);
            PDDocument document = PDDocument.load(is);

            List<PDPage> pages = document.getDocumentCatalog().getAllPages();
            int pageNumber = 0;
            for (PDPage page : pages) {
                pageNumber++;

                //convert pdf page to image
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int dpiImageResolution = 72; //TODO: was 300
                BufferedImage bufferedImage = page.convertToImage(BufferedImage.TYPE_INT_RGB, dpiImageResolution);
                ImageIO.write(bufferedImage, "JPG", baos);
                baos.flush();
                baos.close();

                Image image = Image.builder()
                        .filename(scanId + ".JPG")
                        .data(baos.toByteArray())
                        .build();
                files.add(image);

                log.info("TRANSFORM|IMAGE|created image: " + "page" + pageNumber + ".JPG");
                //TODO: think we need a sequenceNumber rather than a batch number - using batch number for now
                csvCreator.addImage((int) batch, survey, image.getFilename(), scanId, pageNumber);
            }

            document.close();

            files.add(csvCreator.getFile());
        } catch (IOException e) {
            throw new TransformException("error creating images from pdf", e);
        }

        return files;
    }
}
