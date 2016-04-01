package com.github.onsdigital.perkin.helper;

import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.json.SurveyParser;
import com.github.onsdigital.perkin.json.SurveyTemplate;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by ian on 01/04/2016.
 */
public class TemplateLoaderTest {

    private TemplateLoader loader;
    
    private static String TEST_TEMPLATE_JSON = "templates/023.0203.survey.json";
    private static String TEST_TEMPLATE_PDF = "templates/023.0203.pdf.fo";
    private static String TEST_SURVEY_JSON = "survey.ftp.json";

    @Before
    public void setUp() {
        loader = TemplateLoader.getInstance();
    }

    @Test
    public void shouldLoadTemplate() throws IOException {
        String surveyContent = FileHelper.loadFile(TEST_TEMPLATE_JSON);

        assertThat(surveyContent, is(loader.getTemplate(TEST_TEMPLATE_JSON)));
    }

    @Test
    public void shouldLoadPdfTemplate() throws IOException {
        String pdfTemplateContent = FileHelper.loadFile(TEST_TEMPLATE_PDF);
        Survey testSurvey = new SurveyParser().parse(FileHelper.loadFile(TEST_SURVEY_JSON));

        assertThat(pdfTemplateContent, is(loader.getPdfTemplate(testSurvey)));
    }
}
