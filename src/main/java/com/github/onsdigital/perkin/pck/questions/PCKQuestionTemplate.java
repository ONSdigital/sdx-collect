package com.github.onsdigital.perkin.pck.questions;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PCKQuestionTemplate {

	private String questionNumber;
	private String derivator;
    private boolean optional;
}
