package com.github.onsdigital.perkin.tranform.jpg;

import com.github.onsdigital.perkin.helper.FileHelper;
import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.transform.TransformContext;
import com.github.onsdigital.perkin.transform.TransformEngine;
import com.github.onsdigital.perkin.transform.jpg.PdfCreator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PdfCreatorTest {

    private PdfCreator classUnderTest;

    @Mock
    private Survey survey;

    @Before
    public void setUp() throws IOException, URISyntaxException, SAXException {
        classUnderTest = new PdfCreator();
    }

    @Test
    public void shouldCreatePdf() throws IOException {
        //given
        when(survey.getAnswer(anyString())).thenReturn("answer");
        when(survey.getKeys()).thenReturn(new HashSet(Arrays.asList("1", "11")));
        TransformContext context = TransformEngine.getInstance().createTransformContext(survey);

        //when
        byte[] pdf = classUnderTest.createPdf(survey, context);
        FileHelper.saveFile(pdf, "mci.pdf");

        //then
        assertThat(pdf, is(notNullValue()));
    }
}
