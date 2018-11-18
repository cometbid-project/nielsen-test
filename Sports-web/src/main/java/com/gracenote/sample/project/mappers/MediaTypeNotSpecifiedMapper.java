/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.mappers;

import com.gracenote.sample.project.exceptions.MediaTypeNotSpecifiedException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Gbenga
 */
@Provider
public class MediaTypeNotSpecifiedMapper implements ExceptionMapper<MediaTypeNotSpecifiedException> {

    /**
     *
     * @param ex
     * @return
     */
    @Override
    public Response toResponse(MediaTypeNotSpecifiedException ex) {
        return Response.status(430).
                entity(ex.getMessage()).
                type(MediaType.APPLICATION_JSON).
                build();
    }
}
