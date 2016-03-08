package com.github.onsdigital.perkin.tranform.jpg;

import com.github.onsdigital.FileHelper;
import com.github.onsdigital.perkin.transform.TransformException;
import com.github.onsdigital.perkin.transform.pdf.PdfCreator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(MockitoJUnitRunner.class)
public class PdfCreatorTest {

    private PdfCreator classUnderTest;

    @Before
    public void setUp() throws IOException, URISyntaxException, SAXException {
        classUnderTest = new PdfCreator();
    }

    @Test
    public void shouldCreatePdf() throws TransformException, IOException {
        //given

        //when
        byte[] pdf = classUnderTest.createPdf();
        FileHelper.saveFile(pdf, "mci.pdf");

        //then
        assertThat(pdf, is(notNullValue()));
    }
}
