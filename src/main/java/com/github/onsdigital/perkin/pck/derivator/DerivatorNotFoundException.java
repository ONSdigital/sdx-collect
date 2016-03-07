package com.github.onsdigital.perkin.pck.derivator;

public class DerivatorNotFoundException extends Exception {

	private static final long serialVersionUID = 2610098058834428466L;

	public DerivatorNotFoundException(final String name) {
		super("Derivator not found: " + name);
	}
	
	public DerivatorNotFoundException(final String name, final Throwable t) {
		super("Derivator not found: " + name, t);
	}
}
