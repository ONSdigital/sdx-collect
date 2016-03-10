package com.github.onsdigital.perkin.json;

import com.github.onsdigital.perkin.transform.pck.QuestionTemplate;
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
	private List<QuestionTemplate> questionTemplates;
}
