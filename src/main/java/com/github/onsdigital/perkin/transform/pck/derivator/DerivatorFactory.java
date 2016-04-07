package com.github.onsdigital.perkin.transform.pck.derivator;

import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.transform.pck.Question;
import com.github.onsdigital.perkin.json.QuestionTemplate;
import com.github.onsdigital.perkin.json.SurveyTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


import java.util.*;

@Slf4j
public class DerivatorFactory {

	private Map<String, Derivator> derivators;

	public DerivatorFactory() {
		derivators = new HashMap<>();
	}

    //TODO make private? i.e. just use deriveAnswer(...)
	public Derivator getDerivator(String name) throws DerivatorNotFoundException {

        if (name == null) name = "default";

		if (derivators.containsKey(name)) {
            log.debug("TRANSFORM|PCK|found derivator: " + name);
			return derivators.get(name);
		} else {
			Derivator derivator = loadDerivator(name);
            derivators.put(name, derivator);
            return derivator;
		}
	}

    public List<Question> deriveAllAnswers(Survey survey, SurveyTemplate surveyTemplate) throws DerivatorNotFoundException {

        List<Question> result = new ArrayList<>();

        for (QuestionTemplate questionTemplate : surveyTemplate.getQuestions()) {

			String answer = survey.getAnswer(questionTemplate.getQuestionNumber());
			Derivator derivator = getDerivator(questionTemplate.getType());
			String derivedAnswer = derivator.deriveValue(answer);
			if (derivedAnswer == null){
				log.info("TRANSFORM|PCK|derived: Question number "+ questionTemplate.getQuestionNumber() +" from question template: "+ questionTemplate + " not submitted, not added to PCK");
			}else{
				Question question = new Question(questionTemplate.getQuestionNumber(), derivedAnswer);
				result.add(question);
				log.debug("TRANSFORM|PCK|derived: " + question + " from question template: " + questionTemplate + " answer: " + answer);
			}

        }

        return result;
    }

	private Derivator loadDerivator(String name) throws DerivatorNotFoundException {
		try {
			if (StringUtils.isBlank(name)) {
				throw new DerivatorNotFoundException(name);
			}

			String className = "com.github.onsdigital.perkin.transform.pck.derivator." + StringUtils.capitalize(name.trim().toLowerCase()) + "Derivator";
			log.trace("TRANSFORM|PCK|loading derivator: " + className);

			Derivator derivator = (Derivator) Class.forName(className).newInstance();
			log.debug("TRANSFORM|PCK|loaded derivator: " + derivator);
			return derivator;

		} catch (InstantiationException | IllegalAccessException| ClassNotFoundException e) {
			throw new DerivatorNotFoundException(name, e);
		}
	}

}
