package com.github.onsdigital.perkin.pck.survey;

import com.github.onsdigital.perkin.pck.questions.PCKQuestionTemplate;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class SurveyTemplate {

	private String id;
	private String name;
    @Singular("question")
	private List<PCKQuestionTemplate> pckQuestionTemplates;
}
