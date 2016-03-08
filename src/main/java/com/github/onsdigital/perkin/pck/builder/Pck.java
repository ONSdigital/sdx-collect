package com.github.onsdigital.perkin.pck.builder;

import com.github.onsdigital.perkin.pck.questions.PCKQuestion;

import java.util.List;

public class Pck {
	
	private String header;
	private String formLead;
	private String formIdentifier;
	private List <PCKQuestion> questions;

    private String filename;
	
	
	public String getHeader() {
		return header;
	}
	public void setHeader(String header) {
		this.header = header;
	}
	public String getFormLead() {
		return formLead;
	}
	public void setFormLead(String formLead) {
		this.formLead = formLead;
	}
	public String getFormIdentifier() {
		return formIdentifier;
	}
	public void setFormIdentifier(String formIdentifier) {
		this.formIdentifier = formIdentifier;
	}
	public List<PCKQuestion> getQuestions() {
		return questions;
	}
	public void setQuestions(List<PCKQuestion> questions) {
		this.questions = questions;
	}

    public String getFilename() {
        return filename;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
	public String toString(){
		
		final String LINE_BREAK = System.lineSeparator();
		final String SPACE = " ";
		StringBuilder sb = new StringBuilder();
		
		sb.append(header)
		.append(LINE_BREAK)
		.append(formLead)
		.append(LINE_BREAK)
		.append(formIdentifier)
		.append(LINE_BREAK);
		
		for (PCKQuestion question : questions) {
			sb.append(question.toString())
			.append(LINE_BREAK);
		}
		
		return sb.toString();
	}

}
