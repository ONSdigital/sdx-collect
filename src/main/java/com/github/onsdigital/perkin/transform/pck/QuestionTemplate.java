package com.github.onsdigital.perkin.transform.pck;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuestionTemplate {

	private String questionNumber;
	private String derivator;
    private boolean optional;
}
