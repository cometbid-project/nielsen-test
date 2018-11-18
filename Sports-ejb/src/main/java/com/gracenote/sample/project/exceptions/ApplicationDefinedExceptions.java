/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.exceptions;

import org.apache.commons.lang3.exception.ContextedException;

/**
 *
 * @author Gbenga
 */
public class ApplicationDefinedExceptions extends ContextedException {

    /**
     * Creates a new instance of <code>ApplicationDefinedExceptions</code>
     * without detail message.
     */
    public ApplicationDefinedExceptions() {
    }

    /**
     * Constructs an instance of <code>ApplicationDefinedExceptions</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public ApplicationDefinedExceptions(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>ApplicationDefinedExceptions</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     * @param ex
     */
    public ApplicationDefinedExceptions(String msg, Throwable ex) {
        super(msg, ex);
    }
}
