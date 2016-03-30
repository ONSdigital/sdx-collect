package com.github.onsdigital.perkin.json;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class SurveyTemplate {

    @SerializedName("survey_id")
    private String id;
    @SerializedName("form_type")
    private String formType;
    @Singular("question")
	private List<QuestionTemplate> questions;

}
