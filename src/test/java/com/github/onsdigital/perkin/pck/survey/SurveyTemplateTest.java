package com.github.onsdigital.perkin.pck.survey;

import com.github.davidcarboni.httpino.Serialiser;
import com.github.onsdigital.FileHelper;
import com.github.onsdigital.perkin.pck.PckQuestionTemplate;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.io.IOException;
import java.io.SyncFailedException;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SurveyTemplateTest {


    @Test
    public void shouldBuildTemplateFromJson() throws IOException {
        //Given
        String surveyTemplateJson = new String(FileHelper.loadFileAsBytes("template.023.json"));

        //When
        SurveyTemplate surveyTemplate = Serialiser.deserialise(surveyTemplateJson,SurveyTemplate.class);
        System.out.println(surveyTemplate);

        //Then
        assertThat(surveyTemplate, instanceOf(SurveyTemplate.class));

    }
    @Test
    public void shouldBuildTemplateIfNoQuestions() throws IOException {
        //Given
        String surveyTemplateJson = new String(FileHelper.loadFileAsBytes("template.023.no.questions.json"));

        //When
        SurveyTemplate surveyTemplate = Serialiser.deserialise(surveyTemplateJson,SurveyTemplate.class);
        System.out.println(surveyTemplate);

        //Then


        //TODO Should this be allowed to happen... Should a template be created with no questions?
        assertThat(surveyTemplate, instanceOf(SurveyTemplate.class));
        //TODO should this return null?
        assertNull(surveyTemplate.getPckQuestionTemplates());
    }


}
