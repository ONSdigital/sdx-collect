package com.github.onsdigital.perkin.json;

import com.github.davidcarboni.httpino.Serialiser;
import com.github.onsdigital.perkin.helper.FileHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

@Slf4j
public class SurveyTemplateTest {

    @Test
    public void shouldBuildTemplateFromJson() throws IOException {
        //Given
        String json = FileHelper.loadFile("templates/023.0203.survey.json");

        //When
        SurveyTemplate template = Serialiser.deserialise(json, SurveyTemplate.class);

        //Then
        log.debug("TEST|{}", template);
        assertThat(template, instanceOf(SurveyTemplate.class));
    }

    @Test
    public void shouldBuildTemplateIfNoQuestions() throws IOException {
        //Given
        String json = FileHelper.loadFile("surveys/template.023.no.questions.json");

        //When
        SurveyTemplate template = Serialiser.deserialise(json, SurveyTemplate.class);

        //Then
        log.debug("TEST|{}", template);
        //TODO Should this be allowed to happen... Should a template be created with no questions?
        assertThat(template, instanceOf(SurveyTemplate.class));
        //TODO should this return null?
        assertThat(template.getQuestions(), is(nullValue()));
    }
}
