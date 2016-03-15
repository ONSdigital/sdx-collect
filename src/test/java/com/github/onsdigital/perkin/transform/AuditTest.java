package com.github.onsdigital.perkin.transform;

import com.github.onsdigital.perkin.json.SurveyParserException;
import com.google.gson.JsonParseException;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AuditTest {

    private Audit classUnderTest = new Audit();

    @Test
    public void shouldGetExceptionMessage() {
        //given
        Exception cause = new JsonParseException("cause message");
        Exception e = new SurveyParserException("message", "{}", cause);

        //when
        String message = classUnderTest.getExceptionMessage(e);

        //then
        assertThat(message, is("com.github.onsdigital.perkin.json.SurveyParserException message caused by com.google.gson.JsonParseException cause message"));
    }
}
