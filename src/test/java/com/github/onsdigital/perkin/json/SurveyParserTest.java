package com.github.onsdigital.perkin.json;

import com.github.onsdigital.perkin.helper.FileHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@Slf4j
public class SurveyParserTest {

    private SurveyParser classUnderTest;

    @Before
    public void setUp() {
        classUnderTest = new SurveyParser();
    }

    @Test
    public void shouldParseValidSurveyVersion() throws IOException {
        //given
        String json = getSurvey("survey2.json");

        //when
        Survey2 survey2 = classUnderTest.parse(json);

        //then
        log.debug("TEST|survey as json: {}", classUnderTest.prettyPrint(survey2));
        assertThat(survey2, is(notNullValue()));
    }

    @Test(expected = SurveyParserException.class)
    public void shouldRejectInvalidJson() throws SurveyParserException {
        //given
        String json = "invalid-json";

        //when
        classUnderTest.parse(json);
    }

    @Test(expected = SurveyParserException.class)
    public void shouldRejectUnsupportedSurveyVersion() throws IOException {
        //given
        String json = getSurvey("survey2.invalid.version.json");

        //when
        classUnderTest.parse(json);
    }

    @Test(expected = SurveyParserException.class)
    public void shouldRejectInvalidSubmittedAtDate() throws IOException {
        //given
        String json = getSurvey("survey2.invalid.submitted_at.json");

        //when
        classUnderTest.parse(json);
    }

    //TODO: should we reject a survey with no submitted_at date?
//    @Test(expected = SurveyParserException.class)
//    public void shouldRejectNoSubmittedAtDate() throws IOException {
//        //given
//        String json = getSurvey("survey2.invalid.no.submitted_at.json");
//
//        //when
//        classUnderTest.parse(json);
//    }

    @Test(expected = SurveyParserException.class)
    public void shouldRejectInvalidPeriodDate() throws IOException {
        //given
        String json = getSurvey("survey2.invalid.period.json");

        //when
        classUnderTest.parse(json);
    }

    //TODO: should we reject a survey with no period date?
//    @Test(expected = SurveyParserException.class)
//    public void shouldRejectNoPeriodDate() throws IOException {
//        //given
//        String json = getSurvey("survey2.invalid.no.period.json");
//
//        //when
//        classUnderTest.parse(json);
//    }

    //TODO: origin - multiple messages in exception?
    //TODO: type - multiple messages in exception?

    private String getSurvey(String filename) throws IOException {
        return FileHelper.loadFile(filename);
    }
}
