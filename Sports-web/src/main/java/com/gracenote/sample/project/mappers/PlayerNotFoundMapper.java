/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.mappers;

import com.gracenote.sample.project.exceptions.PlayerNotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**s
 *
 * @author Gbenga
 */
@Provider
public class PlayerNotFoundMapper implements ExceptionMapper<PlayerNotFoundException> {

    /**
     *
     * @param ex
     * @return
     */
    @Override
    public Response toResponse(PlayerNotFoundException ex) {
        return Response.status(421).
                entity(ex.getMessage()).
                type(MediaType.APPLICATION_JSON).
                build();
    }
}