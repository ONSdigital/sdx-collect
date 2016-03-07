package com.github.onsdigital.perkin.pck.derivator;

import org.apache.commons.lang3.StringUtils;

public class ContainsDerivator implements Derivator {

	private final String TRUE =  "1";
	private final String FALSE = "2";
	@Override
	public String deriveValue(String surveyAnswer) {
		
		if(StringUtils.isBlank(surveyAnswer)){
			return FALSE;
		}
		else {
			return TRUE;
		}
	}

}
