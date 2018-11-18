package com.gracenote.sample.project.exceptions;

import javax.ejb.ApplicationException;
import org.apache.commons.lang3.exception.ContextedRuntimeException;

@ApplicationException(rollback = true)
public class MediaTypeNotSpecifiedException extends ContextedRuntimeException {

    private static final long serialVersionUID = 1L;

    public MediaTypeNotSpecifiedException() {
        super("Media type not specified");
    }

    public MediaTypeNotSpecifiedException(String message) {
        super(message);
    }

}
