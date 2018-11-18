/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.utility;

import com.gracenote.sample.project.exceptions.MediaTypeNotSpecifiedException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author babatunde.adeyemi
 */
@Provider
public class JSONRequestFilter implements ContainerRequestFilter {

    @Context
    private UriInfo uriInfo;
    private static final Logger LOGGER = new Logger(JSONRequestFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext crc) throws MediaTypeNotSpecifiedException, IOException {
        //logger.log(Level.INFO, "Url invoked is {0}", uriInfo.getPath());

        if (crc.getMediaType() == null) {
            throw new MediaTypeNotSpecifiedException("Content Type not specified in the Header field");
        }
        
        if (!crc.hasEntity()) {//if no data was supplied with the request
            //logger.warning("***     The request contains no data     ***");

            //Insert an empty oject in the stream since it is empty
            //to avoid the error "java.io.EOFException: No content to map to Object due to end of input"
            //thrown by jersey when an empty JSON request is made
            //logger.info("###     Intercepted request with empty data     ###");
            //get the content-type           
            MediaType mt = crc.getMediaType();
            String contentType = mt.getType() + "/" + mt.getSubtype();
            //logger.info("###   Content-type detected: "+contentType+"   ###");

            InputStream is = crc.getEntityStream();
            if (contentType.equals("application/json")) {
                String replacementData = "{}";
                byte[] requestEntity = replacementData.getBytes();
                crc.setEntityStream(new ByteArrayInputStream(requestEntity));

                //logger.info("###     Replaced the empty data sent by an empty json object     ###");
            } else if (contentType.equals("application/xml")) {
                String replacementData = "<request></request>";
                byte[] requestEntity = replacementData.getBytes();
                crc.setEntityStream(new ByteArrayInputStream(requestEntity));

                //logger.info("###     Replaced the empty data sent by an empty xml data     ###");
            }
        }
    }
}
