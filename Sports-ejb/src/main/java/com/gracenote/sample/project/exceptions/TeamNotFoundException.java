/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.exceptions;

/**
 *
 * @author Gbenga
 */
public class TeamNotFoundException extends ApplicationDefinedExceptions {

    public TeamNotFoundException() {
        super("Language not currently supported or cannot be found");
    }

    public TeamNotFoundException(String message) {
        super(message);
    }
    
    public TeamNotFoundException(String message, Throwable ex) {
        super(message, ex);
    }

}
