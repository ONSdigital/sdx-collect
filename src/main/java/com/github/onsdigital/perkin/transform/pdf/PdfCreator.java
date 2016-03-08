package com.github.onsdigital.perkin.transform.pdf;

import com.github.onsdigital.perkin.transform.TransformException;
import org.apache.fop.apps.*;
import org.xml.sax.SAXException;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class PdfCreator {

    private FopFactory fopFactory;

    public PdfCreator() throws IOException, SAXException, URISyntaxException {
        URL url = this.getClass().getResource("/to-jpg/fop-config.xml");
        File config = new File(url.toURI());
        fopFactory = FopFactory.newInstance(config);
    }

    public byte[] createPdf() throws TransformException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            // Step 3: Construct fop with desired output format
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

            // Step 4: Setup JAXP using identity transformer
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(); // identity transformer

            // Step 5: Setup input and output for XSLT transformation
            // Setup input stream

            //TODO: will take an inputstream - load template into byte[] for reuse?
            //TODO: make configurable once > 1 survey
            URL url = this.getClass().getResource("/to-jpg/mci.fo");
            File template = new File(url.toURI());

            Source src = new StreamSource(template);

            // Resulting SAX events (the generated FO) must be piped through to FOP
            Result res = new SAXResult(fop.getDefaultHandler());

            // Step 6: Start XSLT transformation and FOP processing
            transformer.transform(src, res);

        } catch (FOPException | TransformerException | URISyntaxException e) {
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
}
