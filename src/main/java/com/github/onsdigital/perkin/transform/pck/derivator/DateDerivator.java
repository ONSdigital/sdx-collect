package com.github.onsdigital.perkin.transform.pck.derivator;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateDerivator implements Derivator {

	private static final String INPUT_DATE_FORMAT = "d/M/yyyy";
	private static final String OUTPUT_DATE_FORMAT = "ddMMyy";

	@Override
	public String deriveValue(String answer) {

		if (answer == null) {
			return null;
		}

		if (StringUtils.isBlank(answer)) {
			return "";
		} else {
			try {
				Date input = new SimpleDateFormat(INPUT_DATE_FORMAT).parse(answer);
				String output = new SimpleDateFormat(OUTPUT_DATE_FORMAT).format(input);
				return output;
			} catch (ParseException e) {
				return "";
			}
		}
	}
}
