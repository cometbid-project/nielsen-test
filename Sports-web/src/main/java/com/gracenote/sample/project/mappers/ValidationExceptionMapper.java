/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.mappers;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Gbenga
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    /**
     *
     * @param exception
     * @return
     */
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
        exception.getConstraintViolations()
                .forEach(v -> {
                    builder.header("Error-Description",
                            String.format("Validation Failed: invalid value {0}, message: {1}",
                                    v.getInvalidValue(), v.getMessage()));
                });
        return builder.build();
    }
}
