package com.github.onsdigital.perkin.transform;

import java.io.IOException;

public class TransformException extends IOException {

    private static final long serialVersionUID = 1L;

    public TransformException(String message, Throwable cause) {
        super(message, cause);
    }
}
