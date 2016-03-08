package com.github.onsdigital.perkin.pck.questions;


/**Class that holds the json representation stored on the file system and is used to create a list of PCKQuestions for use in the transform
 * 
 * 
 * @author howela3
 *
 */
public class PCKQuestionTemplate {

	private String questionNumber;
	private String derivator;
	
	
	
	public PCKQuestionTemplate(String questionKey, String derivator, boolean optional) {
		this.questionNumber = questionKey;
		this.derivator = derivator;

				
	}
	
	
	public String getQuestionNumber() {
		return questionNumber;
	}



	public void setQuestionKey(String questionKey) {
		this.questionNumber = questionKey;
	}


	public String getDerivator() {
		return derivator;
	}


	public void setDerivator(String derivator) {
		this.derivator = derivator;
	}



	@Override
	public String toString() {
		return "PCKQuestionFile [questionNumber=" + questionNumber + ", derivator="
				+ derivator+ "]";
	}
	
	
	
	
}
