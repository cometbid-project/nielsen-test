/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 *
 * @author Gbenga
 */
public class PagingParameterValidator implements ConstraintValidator<PagingValidator, Integer> {

    @Override
    public void initialize(PagingValidator constraint) {
        // nothing to do
    }

    @Override
    public boolean isValid(Integer parameter, ConstraintValidatorContext context) {  
        return !(parameter < 1);      
    }
}
