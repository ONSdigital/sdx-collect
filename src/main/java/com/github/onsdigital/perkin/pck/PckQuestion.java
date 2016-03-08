package com.github.onsdigital.perkin.pck;

import org.apache.commons.lang3.StringUtils;

public class PckQuestion {

    private static final char PAD_CHARACTER = '0';

    private String questionNumber;
	private String answer;
	
	private static final int LENGTH_QUESTION = 4;
	private static final int LENGTH_ANSWER = 11;
	
	public PckQuestion(String questionNumber, String answer) {
		setQuestionNumber(questionNumber);
        setAnswer(answer);
	}
	
	public String getQuestionNumber() {
		return questionNumber;
	}

	public void setQuestionNumber(String questionNumber) {
		this.questionNumber = leftPad(questionNumber, LENGTH_QUESTION);
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
		return questionNumber + " " + answer;
	}
}
