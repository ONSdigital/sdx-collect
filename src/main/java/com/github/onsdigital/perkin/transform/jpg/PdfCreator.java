package com.github.onsdigital.perkin.transform.jpg;

import com.github.onsdigital.perkin.helper.FileHelper;
import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.transform.TransformException;
import org.apache.fop.apps.*;
import org.xml.sax.SAXException;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class PdfCreator {

    private FopFactory fopFactory;
    private boolean init = false;
    private String pdfTemplate;

    private void init() throws TransformException {
        if (!init) {
            try {
                InputStream in = getClass().getClassLoader().getResourceAsStream("to-jpg/fop-config.xml");
                URI baseUri = new URL("http://survey.ons.gov.uk/whatever").toURI();
                fopFactory = FopFactory.newInstance(baseUri, in);

                //TODO: manage multiple pdf templates for surveys
                pdfTemplate = FileHelper.loadFile("to-jpg/mci.fo");
                init = true;
            } catch (URISyntaxException | IOException | SAXException e) {
                throw new TransformException("error configuring fop", e);
            }
        }
    }

    public byte[] createPdf(Survey survey) throws TransformException {

        init();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            // Step 3: Construct fop with desired output format
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

            // Step 4: Setup JAXP using identity transformer
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(); // identity transformer

            // Step 5: Setup input and output for XSLT transformation
            // Setup input stream

            //TODO: make configurable once > 1 survey
            Source src = getPdfTemplate(survey);

            // Resulting SAX events (the generated FO) must be piped through to FOP
            Result res = new SAXResult(fop.getDefaultHandler());

            // Step 6: Start XSLT transformation and FOP processing
            transformer.transform(src, res);

        } catch (FOPException | TransformerException e) {
            throw new TransformException("problem creating pdf", e);
        } finally {
            //Clean-up
            try {
                out.close();
            } catch (IOException e) {
                throw new TransformException("problem closing output stream", e);
            }
        }

        return out.toByteArray();
    }

    //TODO: we have only one pdf template for now for MCI survey
    private Source getPdfTemplate(Survey survey) {

        String template = pdfTemplate;

        //populate fop template
        //TODO: add question text from the template

        //TODO: need to get the survey template to get the keys
        for (String key : survey.getKeys()) {
            template = populateAnswer(template, key, survey.getAnswer(key));
        }

        return new StreamSource(new ByteArrayInputStream(template.getBytes(StandardCharsets.UTF_8)));
    }

    private String populateAnswer(String template, String key, String answer) {
        if (answer == null) {
            //TODO:
        }

        System.out.println("pdf populating question: " + key + " answer: " + answer);
        return template.replace("$" + key + "$", answer);
    }
}
