package com.github.onsdigital.perkin.helper;

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

    @Before
    public void setUp() {
        loader = TemplateLoader.getInstance();
    }

    @Test
    public void shouldLoadTemplate() throws IOException {
        String surveyContent = FileHelper.loadFile(TEST_TEMPLATE_JSON);

        assertThat(surveyContent, is(loader.getTemplate(TEST_TEMPLATE_JSON)));
    }
}
