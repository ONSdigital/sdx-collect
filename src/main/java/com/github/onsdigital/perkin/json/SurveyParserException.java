package com.github.onsdigital.perkin.json;

import com.github.onsdigital.perkin.transform.TransformException;

public class SurveyParserException extends TransformException {

	private static final long serialVersionUID = 1L;

    public SurveyParserException(String message) {
        super(message);
    }

	public SurveyParserException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
