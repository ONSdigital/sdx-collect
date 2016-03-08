package com.github.onsdigital.perkin.pck.pckbuilder;

import com.github.onsdigital.perkin.pck.derivator.Derivator;
import com.github.onsdigital.perkin.pck.derivator.DerivatorFactory;
import com.github.onsdigital.perkin.pck.derivator.DerivatorNotFoundException;
import com.github.onsdigital.perkin.pck.questions.PCKQuestion;
import com.github.onsdigital.perkin.pck.questions.PCKQuestionTemplate;
import com.github.onsdigital.perkin.pck.survey.Survey;
import com.github.onsdigital.perkin.pck.survey.SurveyTemplate;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class converts the survey questionnaire data from key/answer pairs into PCK format String.
 * 
 * @author altafm
 * 
 */
final class PCKBuilder {

	
	private static final String HEADER_SEPARATOR = ":";
	private static final String FORM_LEAD = "FV";
	private static final int QUESTION_PAD_LENGTH = 4;

	private DerivatorFactory derivatorFactory = new DerivatorFactory();

	PCKBuilder() {
	}

	
	public Pck build(Survey survey, SurveyTemplate surveytemplate) throws DerivatorNotFoundException {

		Pck pck = new Pck();
		pck.setHeader(generateHeader());
		pck.setFormIdentifier(generateFormIdentifier());
		pck.setQuestions(generatePCKQuestions(survey, surveytemplate));
		pck.setFormLead(FORM_LEAD);
		
		return pck;
	}
	
	private List <PCKQuestion> generatePCKQuestions(Survey survey, SurveyTemplate surveytemplate) throws DerivatorNotFoundException {
		
		List <PCKQuestion> pckQuestions = new ArrayList<>();
		
		for (PCKQuestionTemplate questionTemplate: surveytemplate.getPckQuestionTemplates()){
			
			String questionNumber = questionTemplate.getQuestionNumber();
			String answer = survey.getAnswer(questionNumber);
			if (!answer.isEmpty()){
				pckQuestions.add(new PCKQuestion(questionNumber, deriveAnswer(questionTemplate, answer)));
			}
		}
		
		return pckQuestions;
	}
	
	private String deriveAnswer(PCKQuestionTemplate question, String surveyAnswer) throws DerivatorNotFoundException {
		Derivator derivator = derivatorFactory.getDerivator(question.getDerivator());
		return derivator.deriveValue(surveyAnswer);
	}
	
		
	private String generateHeader() {
		
		String batchHeader = "FBFV";
		int batchNumber = 30001;
		String date = getCurrentDateAsString();
	
		String header = batchHeader+String.valueOf(batchNumber)+date; 
		return header;
	}

	private String generateFormIdentifier() {
		StringBuilder formIdentifer = new StringBuilder();
				
		formIdentifer.append(StringUtils.leftPad("5", QUESTION_PAD_LENGTH, "0"));
		formIdentifer.append(HEADER_SEPARATOR);
		formIdentifer.append("99999994188");
		formIdentifer.append("F");
		formIdentifer.append(HEADER_SEPARATOR);
		String refpStartDate ="01 Oct 2014";
		formIdentifer.append(getFormattedPeriod(refpStartDate));

		return formIdentifer.toString();
	}

	

	/**
	 * Converts the refp start date String (dd MMM yyyy) into period in 'yyyyMM' format
	 * 
	 * @param refpStartDateStr
	 *            the date String
	 * 
	 * @return String formatted period i.e. yyyyMM
	 * 
	 */
	private String getFormattedPeriod(String refpStartDateStr) {
		DateFormat inputFormatter = new SimpleDateFormat("dd MMMM yyyy");
		DateFormat outputFormatter = new SimpleDateFormat("yyyyMM");
		String period = null;
		try {
			Date refpStartDate = inputFormatter.parse(refpStartDateStr);
			period = outputFormatter.format(refpStartDate);
		} catch (ParseException pe) {
			System.out.println("Date parser error :"+ pe.getMessage());
		}
		
		return period;
	}
	
	private String getCurrentDateAsString(){
		LocalDate date = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");
		String text = date.format(formatter);
		return text;

	}
	
}
