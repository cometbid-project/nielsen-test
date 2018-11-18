/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.mappers;

import com.gracenote.sample.project.exceptions.TeamNotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Gbenga
 */
@Provider
public class TeamNotFoundMapper implements ExceptionMapper<TeamNotFoundException> {

    /**
     *
     * @param ex
     * @return
     */
    @Override
    public Response toResponse(TeamNotFoundException ex) {
        return Response.status(422).
                entity(ex.getMessage()).
                type(MediaType.APPLICATION_JSON).
                build();
    }
}