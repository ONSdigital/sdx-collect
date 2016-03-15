package com.github.onsdigital.perkin.json;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SurveyTest {

    @Test
    public void shouldReturnNullAnswerIfNoAnswers() throws IOException {
        //Given
        Survey survey = Survey.builder().build();

        //When
        String answer = survey.getAnswer("any-question");

        //Then
        assertThat(answer, is(nullValue()));
    }
}
