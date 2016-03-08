package com.github.onsdigital.perkin.pck;

import java.util.List;

public class Pck {

    private static final String NEW_LINE = System.lineSeparator();

	private String header;
	private String formLead;
	private String formIdentifier;
	private List <PckQuestion> questions;

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

	public List<PckQuestion> getQuestions() {
		return questions;
	}

	public void setQuestions(List<PckQuestion> questions) {
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

		StringBuilder sb = new StringBuilder()
                .append(header).append(NEW_LINE)
		        .append(formLead).append(NEW_LINE)
		        .append(formIdentifier).append(NEW_LINE);
		
		for (PckQuestion question : questions) {
			sb.append(question.toString()).append(NEW_LINE);
		}
		
		return sb.toString();
	}
}
