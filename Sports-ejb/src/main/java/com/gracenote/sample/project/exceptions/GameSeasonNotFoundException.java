/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.exceptions;

import javax.ejb.ApplicationException;

/**
 *
 * @author Gbenga
 */
@ApplicationException(rollback = true)
public class GameSeasonNotFoundException extends ApplicationDefinedExceptions {

    public GameSeasonNotFoundException() {
        super("Subscription type not found");
    }

    public GameSeasonNotFoundException(String message) {
        super(message);
    }

    public GameSeasonNotFoundException(String message, Throwable ex) {
        super(message, ex);
    }

}
