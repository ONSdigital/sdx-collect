package com.github.onsdigital.perkin.transform.pck;

import com.github.davidcarboni.httpino.Serialiser;
import com.github.onsdigital.perkin.helper.FileHelper;
import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.transform.DataFile;
import com.github.onsdigital.perkin.transform.Transformer;
import com.github.onsdigital.perkin.transform.pck.derivator.DerivatorFactory;
import com.github.onsdigital.perkin.json.SurveyTemplate;
import com.github.onsdigital.perkin.transform.TemplateNotFoundException;
import com.github.onsdigital.perkin.transform.TransformException;
import org.apache.commons.lang3.StringUtils;


import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Convert a survey into PCK format.
 */
public class PckTransformer implements Transformer {

	private static final String HEADER_SEPARATOR = ":";
	private static final String FORM_LEAD = "FV";
	private static final int QUESTION_PAD_LENGTH = 4;

	private DerivatorFactory derivatorFactory = new DerivatorFactory();

    @Override
    public DataFile transform(final Survey survey, final long batchId) throws TransformException {
        System.out.println("pck building from survey: " + survey);

        //we only have the MCI survey template for now
        Pck pck = new Pck();
        pck.setHeader(generateHeader(batchId));
        pck.setFormIdentifier(generateFormIdentifier());
        pck.setQuestions(derivatorFactory.deriveAllAnswers(survey, getTemplate(survey)));
        pck.setFormLead(FORM_LEAD);

        //TODO: made up a filename structure for now
        pck.setFilename(batchId + "_" + survey.getRespondentId() + ".pck");

        System.out.println("pck built: " + pck);

        return pck;
    }

    private SurveyTemplate getTemplate(Survey survey) throws TemplateNotFoundException {

        try {
            //we only have the MCI survey template for now
            String json = new String(FileHelper.loadFileAsBytes("surveys/template.023.json"));
            return Serialiser.deserialise(json, SurveyTemplate.class);
        } catch (IOException e) {
            throw new TemplateNotFoundException("surveys/template.023.json", e);
        }
    }

    private String generateHeader(long batchId) {

        //TODO: think the date should be a date from the survey
	
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
