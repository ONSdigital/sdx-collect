package com.github.onsdigital.perkin.pck.questions;

import org.apache.commons.lang3.StringUtils;

public class PCKQuestion {

	String questionNumber;
	String answer;
	
	private static final int QUESTION_PAD_LENGTH = 4;
	private static final int ANSWER_PAD_LENGTH = 11;
	
	public PCKQuestion(String questionNumber, String answer) {
		this.questionNumber = leftPadQuestionNumber(questionNumber);
		this.answer = leftPadAnswer(answer);
	}
	
	public String getQuestionNumber() {
		return questionNumber;
	}
	public void setQuestionNumber(String questionNumber) {
		this.questionNumber = leftPadQuestionNumber(questionNumber);
	}
	public String getValue() {
		return answer;
	}
	public void setValue(String answer) {
		this.answer = leftPadAnswer(answer);
	}
	
	private String leftPadQuestionNumber(String questionNumber) {
		return StringUtils.leftPad(questionNumber, QUESTION_PAD_LENGTH, "0");
	}
	
	private String leftPadAnswer(String answer){
		return StringUtils.leftPad(answer, ANSWER_PAD_LENGTH, "0");
	}
	
	@Override
	public String toString(){
		
		return questionNumber + " " + answer;
	}
	
	
}
