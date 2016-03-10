package com.github.onsdigital.perkin.transform.pck.derivator;

public class BooleanDerivator implements Derivator {

    public static final String TRUE = "1";
	public static final String FALSE = "2";

	@Override
	public String deriveValue(String answer) {

		if (answer == null) {
			return FALSE;
		}

		if(answer.trim().equalsIgnoreCase("y")) {
			return TRUE;
		} else {
			return FALSE;
		}
	}
}
