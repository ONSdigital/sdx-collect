package com.github.onsdigital.perkin.json;

import com.github.davidcarboni.httpino.Serialiser;
import com.github.onsdigital.perkin.helper.FileHelper;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

public class SurveyTemplateTest {

    @Test
    public void shouldBuildTemplateFromJson() throws IOException {
        //Given
        String surveyTemplateJson = new String(FileHelper.loadFileAsBytes("surveys/template.023.json"));

        //When
        SurveyTemplate surveyTemplate = Serialiser.deserialise(surveyTemplateJson,SurveyTemplate.class);
        System.out.println(surveyTemplate);
        //Then
        System.out.println(surveyTemplate);
        assertThat(surveyTemplate, instanceOf(SurveyTemplate.class));
    }

    @Test
    public void shouldBuildTemplateIfNoQuestions() throws IOException {
        //Given
        String surveyTemplateJson = new String(FileHelper.loadFileAsBytes("surveys/template.023.no.questions.json"));
        //When
        SurveyTemplate surveyTemplate = Serialiser.deserialise(surveyTemplateJson,SurveyTemplate.class);
        System.out.println(surveyTemplate);

        //Then
        System.out.println(surveyTemplate);
        //TODO Should this be allowed to happen... Should a template be created with no questions?
        assertThat(surveyTemplate, instanceOf(SurveyTemplate.class));
        //TODO should this return null?
        assertThat(surveyTemplate.getPckQuestionTemplates(), is(nullValue()));
    }


    private String loadSurveyTemplateFromFile(String filename) throws IOException {
        return new String(this.getClass().getClassLoader().getResource(filename).getFile().getBytes());
    }
}
