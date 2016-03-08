package com.github.onsdigital.perkin.pck.survey;

import com.github.onsdigital.perkin.pck.questions.PCKQuestionTemplate;

import java.util.List;


public class SurveyTemplate {
	
	
	private String id;
	private String name;
	private List<PCKQuestionTemplate> pckQuestionTemplates;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
	public List<PCKQuestionTemplate> getPckQuestionTemplates() {
		return pckQuestionTemplates;
	}
	public void setPckQuestionTemplates(List<PCKQuestionTemplate> pckQuestionTemplates) {
		this.pckQuestionTemplates = pckQuestionTemplates;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("This is a SurveyTemplate Class :");
		sb.append("Name :" +name);
		sb.append("Conatins a List with PCK question mappings for a given survey");
		return sb.toString();
	}

}
