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

    //TODO: make this a parameterized file based test, cope with expected exceptions

    @Test
    public void shouldParseValidSurveyVersion() throws IOException {
        //given
        String json = getSurvey("survey.valid.json");

        //when
        Survey survey = classUnderTest.parse(json);

        //then
        log.debug("TEST|survey as json: {}", classUnderTest.prettyPrint(survey));
        assertThat(survey, is(notNullValue()));
    }

    @Test
    public void shouldPassLongPeriodDownstream() throws IOException {
        //given
        String json = getSurvey("survey.valid.period.long.json");

        //when
        Survey survey = classUnderTest.parse(json);

        //then
        log.debug("TEST|survey as json: {}", classUnderTest.prettyPrint(survey));
        assertThat(survey.getCollection().getPeriod(), is("longValueButPassDownstream"));
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
        String json = getSurvey("survey.invalid.version.json");

        //when
        classUnderTest.parse(json);
    }

    @Test(expected = SurveyParserException.class)
    public void shouldRejectInvalidSubmittedAtDate() throws IOException {
        //given
        String json = getSurvey("survey.invalid.submitted_at.json");

        //when
        classUnderTest.parse(json);
    }

    //TODO: should we reject a survey with no submitted_at date?
//    @Test(expected = SurveyParserException.class)
//    public void shouldRejectNoSubmittedAtDate() throws IOException {
//        //given
//        String json = getSurvey("survey.invalid.no.submitted_at.json");
//
//        //when
//        classUnderTest.parse(json);
//    }

    @Test(expected = SurveyParserException.class)
    public void shouldRejectNoCollection() throws IOException {
        //given
        String json = getSurvey("survey.invalid.no.collection.json");

        //when
        classUnderTest.parse(json);
    }

    @Test(expected = SurveyParserException.class)
    public void shouldRejectNoMetadata() throws IOException {
        //given
        String json = getSurvey("survey.invalid.no.metadata.json");

        //when
        classUnderTest.parse(json);
    }

    //TODO: should we reject a survey with no period date?
//    @Test(expected = SurveyParserException.class)
//    public void shouldRejectNoPeriodDate() throws IOException {
//        //given
//        String json = getSurvey("survey.invalid.no.period.json");
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
