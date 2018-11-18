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
public class LeagueNotFoundException extends ApplicationDefinedExceptions {

    public LeagueNotFoundException() {
        super("State/Province not found");
    }

    public LeagueNotFoundException(String message) {
        super(message);
    }

    public LeagueNotFoundException(String message, Throwable ex) {
        super(message, ex);
    }

}
