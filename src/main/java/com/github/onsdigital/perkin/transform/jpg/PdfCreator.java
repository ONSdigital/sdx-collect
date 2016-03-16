package com.github.onsdigital.perkin.transform.jpg;

import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.transform.TransformContext;
import com.github.onsdigital.perkin.transform.TransformException;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public class PdfCreator {

    private FopFactory fopFactory;
    private boolean init = false;

    private void init() throws TransformException {
        if (!init) {
            try {
                InputStream in = getClass().getClassLoader().getResourceAsStream("fop-config.xml");
                URI baseUri = new URL("http://survey.ons.gov.uk/whatever").toURI();
                fopFactory = FopFactory.newInstance(baseUri, in);

                init = true;
            } catch (URISyntaxException | IOException | SAXException e) {
                throw new TransformException("error configuring fop", e);
            }
        }
    }

    public byte[] createPdf(final Survey survey, final TransformContext context) throws TransformException {

        init();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            // Step 3: Construct fop with desired output prettyPrint
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

            // Step 4: Setup JAXP using identity transformer
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(); // identity transformer

            // Step 5: Setup input and output for XSLT transformation
            // Setup input stream
            Source src = populateFopTemplate(survey, context);

            // Resulting SAX events (the generated FO) must be piped through to FOP
            Result res = new SAXResult(fop.getDefaultHandler());

            // Step 6: Start XSLT transformation and FOP processing
            transformer.transform(src, res);

        } catch (FOPException | TransformerException e) {
            throw new TransformException("problem creating pdf", e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                //TODO: should we just log and not re-throw?
                throw new TransformException("problem closing output stream of pdf", e);
            }
        }

        log.info("TRANSFORM|IMAGE|created pdf from survey");
        return out.toByteArray();
    }

    private Source populateFopTemplate(final Survey survey, final TransformContext context) {

        String template = context.getPdfTemplate();

        //populate fop template
        //populate survey information
        template = populate(template, "formType", survey.getCollection().getInstrumentId());
        template = populate(template, "ruRef", survey.getMetadata().getRuRef());
        template = populate(template, "submittedAt", survey.getDate().toString());

        //TODO: add question text from the template

        //TODO: need to get the survey template to get the keys we expect - not just the keys in the answers
        for (String key : survey.getKeys()) {
            template = populate(template, key, survey.getAnswer(key));
        }

        return new StreamSource(new ByteArrayInputStream(template.getBytes(StandardCharsets.UTF_8)));
    }

    private String populate(String template, String key, String value) {
        if (value == null) {
            //TODO: add a test to see what happens
        }

        log.debug("TRANSFORM|IMAGE|pdf populating key: " + key + " value: " + value);
        return template.replace("$" + key + "$", value);
    }
}
