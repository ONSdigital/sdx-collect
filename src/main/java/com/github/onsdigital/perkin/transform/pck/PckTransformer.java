package com.github.onsdigital.perkin.transform.pck;

import com.github.davidcarboni.httpino.Serialiser;
import com.github.onsdigital.perkin.helper.FileHelper;
import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.transform.*;
import com.github.onsdigital.perkin.transform.pck.derivator.DerivatorFactory;
import com.github.onsdigital.perkin.json.SurveyTemplate;
import org.apache.commons.lang3.StringUtils;


import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Convert a survey into PCK prettyPrint.
 */
public class PckTransformer implements Transformer {

	private static final String HEADER_SEPARATOR = ":";
	private static final String FORM_LEAD = "FV";
	private static final int QUESTION_PAD_LENGTH = 4;

	private DerivatorFactory derivatorFactory = new DerivatorFactory();

    @Override
    public List<DataFile> transform(final Survey survey, final TransformContext context) throws TransformException {
        System.out.println("pck building from survey: " + survey);

        //we only have the MCI survey template for now
        Pck pck = new Pck();
        pck.setHeader(generateHeader(context.getBatch()));
        pck.setFormIdentifier(generateFormIdentifier());
        pck.setQuestions(derivatorFactory.deriveAllAnswers(survey, context.getSurveyTemplate()));
        pck.setFormLead(FORM_LEAD);

        //TODO: made up a filename structure for now
        pck.setFilename(context.getBatch() + "_" + survey.getRespondentId() + ".pck");

        System.out.println("pck built: " + pck);

        return Arrays.asList(pck);
    }

    private String generateHeader(long batchId) {

        //TODO: the date should be a date from the survey
        //TODO: think the batchId should be 6 chars? (we have 5 - or less if the number is e.g. 100) - need to add tests
	
		return "FBFV" + String.valueOf(batchId) + getCurrentDateAsString();
	}

	private String generateFormIdentifier() {
		StringBuilder formIdentifer = new StringBuilder();

        //TODO: looks like this is hardcoded for now?
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
	 * Converts the refp start date String (dd MMM yyyy) into period in 'yyyyMM' prettyPrint
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
            //TODO: might need to throw exception here? otherwise this will be a silent failure - add a test
			System.out.println("Date parser error :"+ pe.getMessage());
		}
		
		return period;
	}
	
	public static String getCurrentDateAsString(){
		LocalDate date = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");
		return date.format(formatter);
	}
}
