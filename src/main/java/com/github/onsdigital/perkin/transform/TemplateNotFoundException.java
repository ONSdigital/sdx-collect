package com.github.onsdigital.perkin.transform;

public class TemplateNotFoundException extends TransformException {

    private static final long serialVersionUID = 1L;

    public TemplateNotFoundException(String message) {
        super("Template not found: " + message);
    }

    public TemplateNotFoundException(String message, Throwable cause) {
        super("Template not found: " + message, cause);
    }
}
