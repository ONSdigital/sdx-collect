package com.github.onsdigital.perkin.transform;

import com.github.onsdigital.perkin.json.SurveyParserException;
import com.google.gson.JsonParseException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Slf4j
public class AuditTest {

    private Audit classUnderTest;

    @Before
    public void setUp() {
        classUnderTest = Audit.getInstance();
    }

    @Test
    public void shouldGetExceptionMessage() {
        //given
        Exception cause = new JsonParseException("cause message");
        Exception e = new SurveyParserException("message", "{}", cause);

        //when
        String message = classUnderTest.getExceptionMessage(e);

        //then
        assertThat(message, is(" com.github.onsdigital.perkin.json.SurveyParserException message caused by com.google.gson.JsonParseException cause message"));
    }

    @Test
    public void shouldAuditMessagesInReverse() {
        //given
        classUnderTest.increment("test");
        classUnderTest.increment("test2");

        //when
        List<String> messages = classUnderTest.getMessages();
        log.debug("TEST|messages: {}", messages);

        //then
        assertThat(messages.get(0).contains("test2"), is(true));
        assertThat(messages.get(1).contains("test"), is(true));
    }
}
