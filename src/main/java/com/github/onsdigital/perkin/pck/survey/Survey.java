package com.github.onsdigital.perkin.pck.survey;

import java.util.Map;


public class Survey {


	private String id;
	private String name;
	private Map<String,String> answers;
	
	
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
	public Map<String, String> getAnswers() {
		return answers;
	}
	public void setAnswers(Map<String, String> answers) {
		this.answers = answers;
	}
	
	public String getAnswer(String questionNumber){
		return this.getAnswers().get(questionNumber);
	}
	
		
	
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("This is a Survey Class :");
		sb.append("Name :" +name);
		sb.append("Id :"+ id);
		return sb.toString();
	}
	
}
