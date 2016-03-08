package com.github.onsdigital.perkin.pck.derivator;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class DerivatorFactory {

	private Map<String, Derivator> derivators;

	public DerivatorFactory() {
		derivators = new HashMap<>();
	}

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
