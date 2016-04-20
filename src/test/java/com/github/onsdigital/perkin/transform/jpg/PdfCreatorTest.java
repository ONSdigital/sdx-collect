package com.github.onsdigital.perkin.transform.jpg;

import com.github.onsdigital.perkin.helper.FileHelper;
import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.json.SurveyParser;
import com.github.onsdigital.perkin.transform.TransformContext;
import com.github.onsdigital.perkin.transform.TransformEngine;
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
    public void shouldCreate0203Pdf() throws IOException {
        //given
        Survey survey = new SurveyParser().parse(FileHelper.loadFile("survey.ftp.json"));
        TransformContext context = TransformEngine.getInstance().createTransformContext(survey);

        //when
        byte[] pdf = classUnderTest.createPdf(survey, context);
        FileHelper.saveFile(pdf, "0203.pdf");

        //then
        assertThat(pdf, is(notNullValue()));
    }

    @Test
    public void shouldCreate0205Pdf() throws IOException {
        //given
        Survey survey = new SurveyParser().parse(FileHelper.loadFile("to-pck/valid.023.0205.json"));
        TransformContext context = TransformEngine.getInstance().createTransformContext(survey);

        //when
        byte[] pdf = classUnderTest.createPdf(survey, context);
        FileHelper.saveFile(pdf, "0205.pdf");

        //then
        assertThat(pdf, is(notNullValue()));
    }

    @Test
    public void shouldCreate0213Pdf() throws IOException {
        //given
        Survey survey = new SurveyParser().parse(FileHelper.loadFile("to-pck/valid.0213.json.ignore"));
        TransformContext context = TransformEngine.getInstance().createTransformContext(survey);

        //when
        byte[] pdf = classUnderTest.createPdf(survey, context);
        FileHelper.saveFile(pdf, "0213.pdf");

        //then
        assertThat(pdf, is(notNullValue()));
    }

    @Test
    public void shouldCreate0215Pdf() throws IOException {
        //given
        Survey survey = new SurveyParser().parse(FileHelper.loadFile("to-pck/valid.0215.json.ignore"));
        TransformContext context = TransformEngine.getInstance().createTransformContext(survey);

        //when
        byte[] pdf = classUnderTest.createPdf(survey, context);
        FileHelper.saveFile(pdf, "0215.pdf");

        //then
        assertThat(pdf, is(notNullValue()));
    }


    @Test
    public void shouldCreatePdfWithAnswersNotSupplied() throws IOException {
        //given
        Survey survey = new SurveyParser().parse(FileHelper.loadFile("to-pck/valid.023.0203.no.answers.json"));
        TransformContext context = TransformEngine.getInstance().createTransformContext(survey);

        //when
        byte[] pdf = classUnderTest.createPdf(survey, context);
        FileHelper.saveFile(pdf, "valid.023.0203.no.answers.pdf");

        //then
        assertThat(pdf, is(notNullValue()));
    }
}
