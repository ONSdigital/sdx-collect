package com.github.onsdigital.perkin.json;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuestionTemplate {

    @SerializedName("q")
    private String questionNumber;
	private String type;
}
