package com.github.onsdigital.perkin.transform.pck;

import com.github.onsdigital.perkin.transform.DataFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class Pck implements DataFile {

    private static final String NEW_LINE = System.lineSeparator();

	private String header;
	private String formLead;
	private String formIdentifier;
	private List<Question> questions;

    private String filename;
    private String path;

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

	public List<Question> getQuestions() {
		return questions;
	}

	public void setQuestions(List<Question> questions) {
		this.questions = questions;
	}

    @Override
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public byte[] getBytes() {
        StringBuilder sb = new StringBuilder()
                .append(header).append(NEW_LINE)
                .append(formLead).append(NEW_LINE)
                .append(formIdentifier).append(NEW_LINE);

        for (Question question : questions) {
            sb.append(question.toString()).append(NEW_LINE);
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public long getSize() {
        return getBytes().length;
    }

    //TODO: remove?
    @Override
	public String toString(){

		StringBuilder sb = new StringBuilder()
                .append(header).append(NEW_LINE)
		        .append(formLead).append(NEW_LINE)
		        .append(formIdentifier).append(NEW_LINE);
		
		for (Question question : questions) {
			sb.append(question.toString()).append(NEW_LINE);
		}
		
		return sb.toString();
	}
}
