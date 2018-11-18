/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.rest.application.config;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author Gbenga
 */
@javax.ws.rs.ApplicationPath("service")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(com.gracenote.sample.project.mappers.GameNotFoundMapper.class);
        resources.add(com.gracenote.sample.project.mappers.GameSeasonNotFoundMapper.class);
        resources.add(com.gracenote.sample.project.mappers.InvalidParameterMapper.class);
        resources.add(com.gracenote.sample.project.mappers.LeagueNotFoundMapper.class);
        resources.add(com.gracenote.sample.project.mappers.MediaTypeNotSpecifiedMapper.class);
        resources.add(com.gracenote.sample.project.mappers.PlayerNotFoundMapper.class);
        resources.add(com.gracenote.sample.project.mappers.TeamNotFoundMapper.class);
        resources.add(com.gracenote.sample.project.mappers.ValidationExceptionMapper.class);
        resources.add(com.gracenote.sample.project.service.rs.GameResource.class);
        resources.add(com.gracenote.sample.project.service.rs.GameSeasonReource.class);
        resources.add(com.gracenote.sample.project.service.rs.LeagueResource.class);
        resources.add(com.gracenote.sample.project.service.rs.PlayersResource.class);
        resources.add(com.gracenote.sample.project.service.rs.TeamResource.class);
        resources.add(com.gracenote.sample.project.utility.JSONRequestFilter.class);
    }
    
}
