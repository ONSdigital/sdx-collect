package com.github.onsdigital.perkin.pck.derivator;

public class BooleanDerivator implements Derivator {

	public static final String TRUE = "1";
	public static final String FALSE = "2";

	@Override
	public String deriveValue(String surveyAnswer) {

		if (surveyAnswer == null) {
			return FALSE;
		}

		if(surveyAnswer.trim().equalsIgnoreCase("y")) {
			return TRUE;
		} else {
			return FALSE;
		}
	}

}
