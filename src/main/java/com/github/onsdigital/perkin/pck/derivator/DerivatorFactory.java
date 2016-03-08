package com.github.onsdigital.perkin.pck.derivator;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
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

    public String deriveAnswer(String derivatorName, String answer) throws DerivatorNotFoundException {
        Derivator derivator = getDerivator(derivatorName);
        String derivedAnswer = derivator.deriveValue(answer);
        System.out.println("derived answer: " + derivedAnswer + " from: " + answer + " using: " + derivatorName);
        return derivedAnswer;
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
