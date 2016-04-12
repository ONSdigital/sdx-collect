package com.github.onsdigital.perkin.transform.pck;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class QuestionTest {

    @Test
    public void shouldLeftPad() {
        //Given
        String questionNumber = "1";
        String answer = "27";

        //When
        Question question = new Question(questionNumber, answer);

        //Then
        assertThat(question.getNumber().length(), is(4));
        assertThat(question.getNumber(), is("0001"));
        assertThat(question.getAnswer().length(), is(11));
        assertThat(question.getAnswer(), is("00000000027"));
    }
}
