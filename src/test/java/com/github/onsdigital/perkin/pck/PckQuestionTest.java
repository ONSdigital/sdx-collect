package com.github.onsdigital.perkin.pck;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class PckQuestionTest {

    @Test
    public void shouldLeftPad() {
        //Given
        String questionNumber = "1";
        String answer = "27";

        //When
        PckQuestion question = new PckQuestion(questionNumber, answer);

        //Then
        assertThat(question.getQuestionNumber().length(), is(4));
        assertThat(question.getQuestionNumber(), is("0001"));
        assertThat(question.getAnswer().length(), is(11));
        assertThat(question.getAnswer(), is("00000000027"));
    }

    //TODO what if question number is > 4 chars?
    //TODO what if question number is > 11 chars?
}
