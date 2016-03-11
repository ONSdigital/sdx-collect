package com.github.onsdigital.perkin.transform.pck;

import com.github.onsdigital.Json;
import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.transform.DataFile;
import com.github.onsdigital.perkin.transform.TransformException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class PckBuilderTest {

	private PckTransformer classUnderTest;

	@Before
	public void setUp(){
		classUnderTest = new PckTransformer();
	}

    @Test
    public void shouldBuildPck() throws TransformException {
        //Given
        long batchId = 30001L;
        Survey survey = createSurvey();
        System.out.println(Json.prettyPrint(survey));
        String expectedDate = PckTransformer.getCurrentDateAsString();
        String expectedPck = "FBFV" + batchId + expectedDate + "\n" +
                "FV\n" +
                "0005:99999994188F:201410\n" +
                "0001 00000000001\n" +
                "0011 00000000001\n" +
                "0020 00000000002\n" +
                "0030 00000000001\n" +
                "0040 00000000700\n" +
                "0050 00000311008\n" +
                "0070 00000000074\n" +
                "0090 00000000074\n" +
                "0100 00000000001\n";

        //When
        List<DataFile> files = classUnderTest.transform(survey, batchId);

        //Then
        assertThat(files, hasSize(1));
        assertThat(files.get(0).toString(), is(expectedPck));
        assertThat(files.get(0).getFilename(), is("30001_respondentId.pck"));
    }

    @Test
    public void shouldBuildPckIfNoAnswers() throws TransformException {
        //Given
        long batchId = 30001L;
        Survey survey = createSurveyNoAnswers();
        String expectedDate = PckTransformer.getCurrentDateAsString();
        //TODO: as we provided no answers, we should get an error for each mandatory answer?
        //TODO: for answers that were optional, should we be adding them to the pck file?
        String expectedPck = "FBFV" + batchId + expectedDate + "\n" +
                "FV\n" +
                "0005:99999994188F:201410\n" +
                "0001 00000000002\n" +
                "0011 00000000002\n" +
                "0020 00000000002\n" +
                "0030 00000000002\n" +
                "0040 00000000000\n" +
                "0050 00000000000\n" +
                "0070 00000000000\n" +
                "0090 00000000000\n" +
                "0100 00000000002\n";

        //When
        List<DataFile> files = classUnderTest.transform(survey, batchId);

        //Then
        assertThat(files, hasSize(1));
        assertThat(files.get(0).toString(), is(expectedPck));
        assertThat(files.get(0).getFilename(), is("30001_respondentId.pck"));
    }

    @Test
    public void shouldBuildPckIfNoQuestionsMatch() throws TransformException {
        //Given
        long batchId = 30001L;
        Survey survey = createSurveyWrongQuestions();
        String expectedDate = PckTransformer.getCurrentDateAsString();
        //TODO: as we provided no matching answers to the template should we get an error when a question can't be found?
        String expectedPck = "FBFV" + batchId + expectedDate + "\n" +
                "FV\n" +
                "0005:99999994188F:201410\n" +
                "0001 00000000002\n" +
                "0011 00000000002\n" +
                "0020 00000000002\n" +
                "0030 00000000002\n" +
                "0040 00000000000\n" +
                "0050 00000000000\n" +
                "0070 00000000000\n" +
                "0090 00000000000\n" +
                "0100 00000000002\n";

        //When
        List<DataFile> files = classUnderTest.transform(survey, batchId);

        //Then
        assertThat(files, hasSize(1));
        assertThat(files.get(0).toString(), is(expectedPck));
        assertThat(files.get(0).getFilename(), is("30001_respondentId.pck"));
    }

    private Survey createSurvey() {
        return Survey.builder()
                .id("id")
                .name("name")
                .respondentId("respondentId")
                .date("date")
                .respondentCheckLetter("A")

                .answer("1", "y")
                .answer("11", "y")
                .answer("20", "n")
                .answer("30", "y")
                .answer("40", "700")
                .answer("50", "311008")
                .answer("70", "74")
                .answer("90", "74")
                .answer("100", "some comment")

                .build();
    }

    private Survey createSurveyWrongQuestions() {
        return Survey.builder()
                .id("id")
                .name("name")
                .respondentId("respondentId")
                .date("date")
                .respondentCheckLetter("A")

                .answer("12", "y")
                .answer("13", "y")
                .answer("14", "n")
                .answer("15", "y")
                .answer("16", "700")
                .answer("17", "311008")
                .answer("18", "74")
                .answer("19", "74")
                .answer("21", "some comment")

                .build();
    }

    private Survey createSurveyNoAnswers() {
        return Survey.builder()
                .id("id")
                .name("name")
                .respondentId("respondentId")
                .date("date")
                .respondentCheckLetter("A")

                .build();
    }
	
	@Test
	public void tryToBuildPCKWithExtraSurveyQuestions(){
		
	}
	
	@Test
	public void tryToBuildPCKWithMissingSurveyQuestions(){
		
	}
	
	@Test
	public void tryToBuildPCKWithUnKnowDerivator(){
		
	}
	
	@Test
	public void tryToBuildPCKWithBadSurveyFile(){
		
	}
	
	@Test
	public void tryTobuildPCKWithBadTemplateFile(){
		
	}
}
