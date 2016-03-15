package com.github.onsdigital.perkin.json;

import com.github.onsdigital.perkin.transform.TransformException;

public class SurveyParserException extends TransformException {

	private static final long serialVersionUID = 1L;

    private String json;

    public SurveyParserException(String message) {
        super(message);
    }

	public SurveyParserException(final String message, final String json, final Throwable cause) {
		super(message, cause);
        this.json = json;
	}

    public String getJson() {
        return json;
    }
}
