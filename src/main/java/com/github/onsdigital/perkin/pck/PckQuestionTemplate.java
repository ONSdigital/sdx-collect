package com.github.onsdigital.perkin.pck;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PckQuestionTemplate {

	private String questionNumber;
	private String derivator;
    private boolean optional;
}
