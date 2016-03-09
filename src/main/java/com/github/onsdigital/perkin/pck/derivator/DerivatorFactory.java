package com.github.onsdigital.perkin.pck.derivator;

import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.pck.PckQuestion;
import com.github.onsdigital.perkin.pck.PckQuestionTemplate;
import com.github.onsdigital.perkin.json.SurveyTemplate;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DerivatorFactory {

	private Map<String, Derivator> derivators;

	public DerivatorFactory() {
		derivators = new HashMap<>();
	}

    //TODO make private? i.e. just use deriveAnswer(...)
	public Derivator getDerivator(String name) throws DerivatorNotFoundException {

		if (derivators.containsKey(name)) {
            System.out.println("found derivator: " + name);
			return derivators.get(name);
		} else {
			Derivator derivator = loadDerivator(name);
            derivators.put(name, derivator);
            return derivator;
		}
	}

    public List<PckQuestion> deriveAllAnswers(Survey survey, SurveyTemplate surveyTemplate) throws DerivatorNotFoundException {

        List<PckQuestion> result = new ArrayList<>();

        for (PckQuestionTemplate question : surveyTemplate.getPckQuestionTemplates()) {

            String answer = survey.getAnswer(question.getQuestionNumber());

            Derivator derivator = getDerivator(question.getDerivator());
            String derivedAnswer = derivator.deriveValue(answer);

            PckQuestion pckQuestion = new PckQuestion(question.getQuestionNumber(), derivedAnswer);
            result.add(pckQuestion);
            System.out.println("derived: " + pckQuestion + " from question: " + question + " answer: " + answer);
        }

        return result;
    }

	private Derivator loadDerivator(String name) throws DerivatorNotFoundException {
		try {
			if (StringUtils.isBlank(name)) {
				throw new DerivatorNotFoundException(name);
			}

			String className = "com.github.onsdigital.perkin.pck.derivator." + StringUtils.capitalize(name.trim().toLowerCase()) + "Derivator";
			System.out.println("loading derivator: " + className);

			Derivator derivator = (Derivator) Class.forName(className).newInstance();
			System.out.println("loaded derivator: " + derivator);
			return derivator;

		} catch (InstantiationException | IllegalAccessException| ClassNotFoundException e) {
			throw new DerivatorNotFoundException(name, e);
		}
	}
}
