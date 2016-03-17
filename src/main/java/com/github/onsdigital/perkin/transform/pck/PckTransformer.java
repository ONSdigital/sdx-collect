package com.github.onsdigital.perkin.transform.pck;

import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.transform.*;
import com.github.onsdigital.perkin.transform.pck.derivator.DerivatorFactory;
import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Convert a survey into PCK prettyPrint.
 */
@Slf4j
public class PckTransformer implements Transformer {

	private static final String HEADER_SEPARATOR = ":";
	private static final String FORM_LEAD = "FV";

	private static final int LENGTH_BATCH = 6;
    private static final int LENGTH_QUESTION = 4;

	private DerivatorFactory derivatorFactory = new DerivatorFactory();

    private static Map<String, String> lookup;

    static {
        lookup = new HashMap<>();
        lookup.put("0203", "RSI7B");
        lookup.put("0205", "RSI9B");
        lookup.put("0213", "RSI8B");
        lookup.put("0215", "RSI10B");
    }

    @Override
    public List<DataFile> transform(final Survey survey, final TransformContext context) throws TransformException {
        log.debug("TRANSFORM|PCK|transforming into pck from survey: {}", survey);

        //we only have the MCI survey template for now
        Pck pck = new Pck();
        pck.setHeader(generateHeader(context.getBatch(), survey.getDate()));
        pck.setFormIdentifier(generateFormIdentifier(survey));
        pck.setQuestions(derivatorFactory.deriveAllAnswers(survey, context.getSurveyTemplate()));
        pck.setFormLead(FORM_LEAD);

        //TODO: made up a filename structure for now
        pck.setFilename(context.getBatch() + "_" + survey.getMetadata().getRuRef() + ".pck");

        log.info("TRANSFORM|PCK|created pck: " + pck);

        return Arrays.asList(pck);
    }

    private String generateHeader(final long batch, final Date date) {
		return "FBFV" + TransformerHelper.leftPadZeroes(String.valueOf(batch), LENGTH_BATCH) + formatDate(date);
	}

    private String generateFormIdentifier(Survey survey) {
		StringBuilder formIdentifer = new StringBuilder();

        String idbrFormReference = survey.getCollection().getInstrumentId();

        //form:idbrrefcheckletter:periodfrom

        //form type is 5 chars
        //colon
        //idbr reference is 11 digits, 1 char check letter
        //colon
        //period is 4 chars
        String commonSoftwareFormReference = lookupCommonSoftwareFormReference(idbrFormReference);
        //e.g. idbr form reference 0203, RSI7B for common software
        //0203 = RSI7B
        //0205 = RSI9B
        //0213 = RSI8B
        //0215 = RSI10B
        String idbrReference = survey.getMetadata().getRuRef();

		formIdentifer.append(commonSoftwareFormReference);
		formIdentifer.append(HEADER_SEPARATOR);
		formIdentifer.append(idbrReference);
		formIdentifer.append(HEADER_SEPARATOR);

        //TODO get period date from survey
		String refpStartDate ="01 Oct 2014";
		formIdentifer.append(getFormattedPeriod(refpStartDate));

		return formIdentifer.toString();
	}

    private String lookupCommonSoftwareFormReference(String idbrFormReference) {
        String ref = lookup.get(idbrFormReference);
        log.debug("TRANSFORM|PCK|lookup idbr form ref: {} got: {}", idbrFormReference, ref);

        return ref;
    }

    private String getFormattedPeriod(String date) {
		DateFormat inputFormatter = new SimpleDateFormat("dd MMMM yyyy");
		DateFormat outputFormatter = new SimpleDateFormat("yyyyMM");
		String period = null;
		try {
			Date startDate = inputFormatter.parse(date);
			period = outputFormatter.format(startDate);
		} catch (ParseException e) {
            //TODO: might need to throw exception here? otherwise this will be a silent failure - add a test
			log.warn("TRANSFORM|PCK|error parsing date: " + date, e);
		}
		
		return period;
	}
	
	public static String formatDate(Date date){
		return new SimpleDateFormat("dd/MM/yy").format(date);
	}
}
