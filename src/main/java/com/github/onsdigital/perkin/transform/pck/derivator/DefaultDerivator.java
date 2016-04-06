package com.github.onsdigital.perkin.transform.pck.derivator;

public class DefaultDerivator implements Derivator {

	@Override
	public String deriveValue(String answer) {
		// Following the derivator pattern. In this case just use the answer passed through.
		if (answer == null) {
			return answer;
		}

		return answer.trim();
	}

}
