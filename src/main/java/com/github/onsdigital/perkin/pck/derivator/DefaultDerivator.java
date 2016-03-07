package com.github.onsdigital.perkin.pck.derivator;

public class DefaultDerivator implements Derivator {
	

	@Override
	public String deriveValue(String surveyAnswer) {
		// Following the derivator pattern. In this case just use the answer passed through.
		return surveyAnswer;
	}

}
