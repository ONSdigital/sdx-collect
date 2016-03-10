package com.github.onsdigital.perkin.transform.pck;

import org.apache.commons.lang3.StringUtils;

public class Question {

    private static final char PAD_CHARACTER = '0';

    private String number;
	private String answer;
	
	private static final int LENGTH_QUESTION = 4;
	private static final int LENGTH_ANSWER = 11;
	
	public Question(String number, String answer) {
		setNumber(number);
        setAnswer(answer);
	}
	
	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = leftPad(number, LENGTH_QUESTION);
	}
	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = leftPad(answer, LENGTH_ANSWER);
	}

    private String leftPad(String value, int length) {
        return StringUtils.leftPad(value, length, PAD_CHARACTER);
    }
	
	@Override
	public String toString(){
		return number + " " + answer;
	}
}
