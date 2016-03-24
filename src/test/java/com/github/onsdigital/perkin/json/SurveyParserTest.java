package com.github.onsdigital.perkin.json;

import com.github.onsdigital.perkin.test.FileHelper;
import com.github.onsdigital.perkin.test.ParameterizedTestHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static junit.framework.TestCase.fail;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
@Slf4j
public class SurveyParserTest {

    private SurveyParser classUnderTest;

    private File survey;
    private File error;

    public SurveyParserTest(File survey, File error){
        this.survey = survey;
        this.error = error;
    }

    @Before
    public void setUp(){
        classUnderTest = new SurveyParser();
    }

    @Parameterized.Parameters(name = "{index}: {0} should produce {1}")
    public static Collection<Object[]> data() throws IOException {

        return ParameterizedTestHelper.getFiles("survey-parser", "json", "error");
    }

    @Test
    public void shouldParseSurveyJson() throws IOException {

        log.debug("TEST|survey-parser: " + survey.getName() + " error: " + error);

        //Given
        String json = FileHelper.loadFile(survey);

        //When
        Survey survey = null;
        try {
            survey = classUnderTest.parse(json);
        } catch (SurveyParserException e) {
            //Then - if error
            if (error.exists()) {
                String expectedErrorMessage = FileHelper.loadFile(error);
                assertThat(e.getMessage(), is(expectedErrorMessage));
                return;
            } else {
                //fail test
                throw e;
            }
        }

        if (error.exists()) {
            String expectedErrorMessage = FileHelper.loadFile(error);
            fail("expected error: " + expectedErrorMessage);
        }

        //Then - if valid
        assertThat(survey, is(notNullValue()));
    }
}
