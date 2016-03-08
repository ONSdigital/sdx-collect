package com.github.onsdigital.perkin.pck;

import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.pck.Pck;
import com.github.onsdigital.perkin.pck.PckBuilder;
import com.github.onsdigital.perkin.pck.derivator.DerivatorNotFoundException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


public class PckBuilderTest {

	private PckBuilder classUnderTest;

	@Before
	public void setUp(){
		classUnderTest = new PckBuilder();
	}

    @Test
    public void shouldBuildPck() throws IOException, DerivatorNotFoundException {
        //Given
        long batchId = 30001L;
        Survey survey = createSurvey();
        String expectedDate = PckBuilder.getCurrentDateAsString();
        String expectedPck = "FBFV" + batchId + expectedDate + "\n" +
                "FV\n" +
                "0005:99999994188F:201410\n" +
                "0001 00000000001\n" +
                "0011 00000000001\n" +
                "0020 00000000002\n" +
                "0030 00000000001\n" +
                "0040 00000000700\n" +
                "0050 00000311008\n" +
                "0070 00000000002\n" +
                "0090 00000000002\n" +
                "0100 00000000001\n";

        //When
        Pck pck = classUnderTest.build(survey, batchId);

        //Then
        assertThat(pck.toString(), is(expectedPck));
        assertThat(pck.getFilename(), is("30001_respondentId.pck"));
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

    @Test
	public void tryToBuildPCKWithCorrectData(){
		
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
