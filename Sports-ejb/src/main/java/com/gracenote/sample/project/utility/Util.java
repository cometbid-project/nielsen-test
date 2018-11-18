/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.utility;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 *
 * @author Gbenga
 */
public class Util {

    private static Validator validator;
    public static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    private Util() {
    }

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Util.validator = factory.getValidator();
    }

    public static Validator getValidator() {
        return validator;
    }

    public static int getPageNumber(int pageNumber) {
        if (pageNumber < 1) {
            pageNumber = 1;
        }
        return pageNumber;
    }

    public static int getPageSize(int pageSize) {
        if (pageSize < 1) {
            pageSize = 10;
        }
        return pageSize;
    }
}
