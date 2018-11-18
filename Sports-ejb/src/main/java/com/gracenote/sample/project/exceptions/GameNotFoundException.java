package com.gracenote.sample.project.exceptions;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class GameNotFoundException extends ApplicationDefinedExceptions {

    public GameNotFoundException() {
        super("Region not found");
    }

    public GameNotFoundException(String message) {
        super(message);
    }

    public GameNotFoundException(String message, Throwable ex) {
        super(message, ex);
    }
}
