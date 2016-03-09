package com.github.onsdigital.perkin.pck.derivator;

import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.pck.PckQuestion;
import com.github.onsdigital.perkin.pck.PckQuestionTemplate;
import com.github.onsdigital.perkin.json.SurveyTemplate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.hamcrest.CoreMatchers.instanceOf;

public class DerviatorFactoryTest {

	private DerivatorFactory classUnderTest;

	private Derivator derivator;

	@Before
	public void setUp() {
		classUnderTest = new DerivatorFactory();
	}

    @After
    public void tearDown() {
        classUnderTest = null;
        derivator = null;
    }

	@Test
	public void shouldGetBooleanDerivator() throws DerivatorNotFoundException {
		//given
        String name = "BOOLEAN";

		//when
		derivator = classUnderTest.getDerivator(name);

		//then
		assertThat(derivator, instanceOf(BooleanDerivator.class));
	}

	@Test
	public void getBooleanDerivatorMixedCase() throws DerivatorNotFoundException {
		//given
        String name = "bOolEan";

		//when
		derivator = classUnderTest.getDerivator(name);

		//then
        assertThat(derivator, instanceOf(BooleanDerivator.class));
	}

	@Test
	public void getDefaultDerivator() throws DerivatorNotFoundException{
		//given
        String name = " Default ";

		//when
		derivator = classUnderTest.getDerivator(name);

		//then
        assertThat(derivator, instanceOf(DefaultDerivator.class));

	}

    @Test
    public void shouldGetSameDerivatorInstance() throws DerivatorNotFoundException{
        //given
        String name = "boolean";

        //when
        Derivator derivator1 = classUnderTest.getDerivator(name);
        Derivator derivator2 = classUnderTest.getDerivator(name);
        System.out.println("derivator1: " + derivator1);
        System.out.println("derivator1: " + derivator2);

        //then
        assertThat(derivator1, is(derivator2));
    }

	@Test(expected = DerivatorNotFoundException.class)
	public void shouldThrowDerivatorNotFoundException() throws DerivatorNotFoundException {
		derivator = classUnderTest.getDerivator("no-such-derivator");
	}

    @Test
    public void shouldDeriveBooleanTrue() throws DerivatorNotFoundException{
        //given
        Survey survey = createSurvey("y");
        SurveyTemplate surveyTemplate = createSurveyTemplate();

        //when
        List<PckQuestion> derived = classUnderTest.deriveAllAnswers(survey, surveyTemplate);

        //then
        assertThat(derived, hasSize(1));
        assertThat(derived.get(0).getQuestionNumber(), is("0001"));
        assertThat(derived.get(0).getAnswer(), is("00000000001"));
    }

    @Test
    public void shouldDeriveBooleanFalse() throws DerivatorNotFoundException{
        //given
        Survey survey = createSurvey("n");
        SurveyTemplate surveyTemplate = createSurveyTemplate();

        //when
        List<PckQuestion> derived = classUnderTest.deriveAllAnswers(survey, surveyTemplate);

        //then
        assertThat(derived, hasSize(1));
        assertThat(derived.get(0).getQuestionNumber(), is("0001"));
        assertThat(derived.get(0).getAnswer(), is("00000000002"));
    }

    private SurveyTemplate createSurveyTemplate() {
        String questionNumber = "1";
        PckQuestionTemplate question = new PckQuestionTemplate(questionNumber, "boolean", true);

        return SurveyTemplate.builder().question(question).build();
    }

    private Survey createSurvey(String answer) {
        String questionNumber = "1";

        return Survey.builder().answer(questionNumber, answer).build();
    }
}
