package com.github.onsdigital.perkin.transform.pck.derivator;

import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.transform.pck.Question;
import com.github.onsdigital.perkin.transform.pck.QuestionTemplate;
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

    public List<Question> deriveAllAnswers(Survey survey, SurveyTemplate surveyTemplate) throws DerivatorNotFoundException {

        List<Question> result = new ArrayList<>();

        for (QuestionTemplate questionTemplate : surveyTemplate.getQuestionTemplates()) {

            String answer = survey.getAnswer(questionTemplate.getQuestionNumber());

            Derivator derivator = getDerivator(questionTemplate.getDerivator());
            String derivedAnswer = derivator.deriveValue(answer);

            Question question = new Question(questionTemplate.getQuestionNumber(), derivedAnswer);
            result.add(question);
            System.out.println("derived: " + question + " from question template: " + questionTemplate + " answer: " + answer);
        }

        return result;
    }

	private Derivator loadDerivator(String name) throws DerivatorNotFoundException {
		try {
			if (StringUtils.isBlank(name)) {
				throw new DerivatorNotFoundException(name);
			}

			String className = "com.github.onsdigital.perkin.transform.pck.derivator." + StringUtils.capitalize(name.trim().toLowerCase()) + "Derivator";
			System.out.println("loading derivator: " + className);

			Derivator derivator = (Derivator) Class.forName(className).newInstance();
			System.out.println("loaded derivator: " + derivator);
			return derivator;

		} catch (InstantiationException | IllegalAccessException| ClassNotFoundException e) {
			throw new DerivatorNotFoundException(name, e);
		}
	}
}
