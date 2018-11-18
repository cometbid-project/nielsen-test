/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.mappers;

import com.gracenote.sample.project.exceptions.GameSeasonNotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Gbenga
 */
@Provider
public class GameSeasonNotFoundMapper implements ExceptionMapper<GameSeasonNotFoundException> {

    /**
     *
     * @param ex
     * @return
     */
    @Override
    public Response toResponse(GameSeasonNotFoundException ex) {
        return Response.status(416).
                entity(ex.getMessage()).
                type(MediaType.APPLICATION_JSON).
                build();
    }
}
