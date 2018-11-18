/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.services.impl;

import com.gracenote.sample.project.entities.Players;
import com.gracenote.sample.project.entities.Team;
import com.gracenote.sample.project.exceptions.TeamNotFoundException;
import com.gracenote.sample.project.qualifiers.Logged;
import com.gracenote.sample.project.services.AbstractFacade;
import com.gracenote.sample.project.services.TeamFacadeLocal;
import com.gracenote.sample.project.utility.Logger;
import com.gracenote.sample.project.utility.Util;
import com.gracenote.sample.project.validators.PagingValidator;
import com.jcabi.aspects.RetryOnFailure;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.exception.ContextedRuntimeException;

/**
 *
 * @author Gbenga
 */
@Logged
@Stateless
public class TeamFacade extends AbstractFacade<Team> implements TeamFacadeLocal {

    @PersistenceContext(unitName = "SportsReport-ejb1.0PU")
    private EntityManager em;
    
    private static final Logger LOGGER = new Logger(TeamFacade.class.getName());

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public TeamFacade() {
        super(Team.class);
    }

    @Override
    public Map<Long, List<Team>> findAllTeamsInLeaguePaginated(
            @Valid @NotNull(message = "Page number must not be null")
            @PagingValidator(message = "Page number must be greater than 0") Integer pageNumber,
            @Valid @NotNull(message = "Page size must not be null")
            @PagingValidator(message = "Page size must be greater than 0") Integer pageSize,
            @Valid @NotNull(message = "League id must not be null") Long leagueId) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("leagueId", leagueId);        

        try {
            int offset = (pageNumber - 1) * pageSize;
            List<Team> teamsList = super.findWithNamedQuery("Team.findAllByLeague", parameters, offset, pageSize);
            
            Map<Long, List<Team>> mapCountRecord = null;
            if (teamsList.isEmpty()) {
                LOGGER.error("No Teams record found");
                mapCountRecord = new HashMap<>();
            } else {
                Long teamsListSize = super.countWithNamedQuery("Team.findAllByLeague", parameters);
                mapCountRecord = new HashMap<>();
                mapCountRecord.put(teamsListSize, teamsList);
            }
            LOGGER.info("{0} Team(s) found!", teamsList);

            return Collections.unmodifiableMap(mapCountRecord);
        } catch (RuntimeException ex) {
            throw new ContextedRuntimeException("Exception occured while retrieving Teams List", ex)
                    .addContextValue("Page Number", pageNumber)
                    .addContextValue("Page Size", pageSize)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }

    
    /**
     *
     * @param teamId
     * @return
     * @throws com.gracenote.sample.project.exceptions.TeamNotFoundException
     */
    @Override
    public Team findByTeamId(Long teamId) throws TeamNotFoundException {

        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("teamId", teamId);
            List<Team> teamList = super.findWithNamedQuery("Team.findByTeamId", parameters);

            if (teamList == null || teamList.isEmpty()) {
                TeamNotFoundException obj
                        = new TeamNotFoundException("Team not found by id");
                obj.addContextValue("findByTeamIdError", "Team not found by id: ")
                        .addContextValue("Team id", teamId)
                        .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
                throw obj;
            }
            return teamList.get(0);
        } catch (RuntimeException ex) {
            throw new ContextedRuntimeException("Unexpected error occured while retrieving Team", ex)
                    .addContextValue("Team id", teamId)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }
    
    /**
     *
     * @param teamId
     * @return
     * @throws com.gracenote.sample.project.exceptions.TeamNotFoundException
     */
    @Override
    public Collection<Players> findPlayersByTeamId(Long teamId) throws TeamNotFoundException {

        try {
            Team managedTeam = findByTeamId(teamId);
            if (managedTeam == null) {
                TeamNotFoundException obj = new TeamNotFoundException("Team not found by id");
                obj.addContextValue("editTeamError", "Team not found by id: ")
                        .addContextValue("Team id: ", teamId)
                        .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
                throw obj;
            }

            return managedTeam.getPlayers();
        } catch (RuntimeException ex) {
            throw new ContextedRuntimeException("Unexpected error occured while retrieving Games", ex)
                    .addContextValue("Team id", teamId)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }

    /**
     *
     * @param newTeam
     * @return
     */
    @Override
    public Team createTeam(
            @Valid @NotNull(message = "Team passed as parameter cannot be null") Team newTeam) {

        try {
            super.create(newTeam);

            LOGGER.info("{0} created successfully.", newTeam.getTeamId());

            return newTeam;
        } catch (RuntimeException exp) {
            throw new ContextedRuntimeException("An unexpected error occured while creating new Team", exp)
                    .addContextValue("Team: ", newTeam)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }

    /**
     *
     * @param updatedTeam
     * @return
     * @throws com.gracenote.sample.project.exceptions.TeamNotFoundException
     */
    @Override
    @RetryOnFailure(attempts = 3, delay = 10, unit = TimeUnit.MILLISECONDS, types = OptimisticLockException.class)
    public Team editTeam(
            @Valid @NotNull(message = "Team passed as parameter cannot be null") Team updatedTeam)
            throws TeamNotFoundException {

        try {
            Team managedTeam = findByTeamId(updatedTeam.getTeamId());
            if (managedTeam == null) {
                TeamNotFoundException obj = new TeamNotFoundException("Teams not found by id");
                obj.addContextValue("editTeamsError", "Team not found by id: ")
                        .addContextValue("Team: ", updatedTeam)
                        .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
                throw obj;
            }

            doTeamsDataTransfer(managedTeam, updatedTeam);
            super.edit(managedTeam);

            LOGGER.info("Team with id {0} updated successfully.", managedTeam.getTeamId());

            return managedTeam;
        } catch (OptimisticLockException ex) {
            throw ex;

        } catch (RuntimeException exp) {
            throw new ContextedRuntimeException("An unexpected error occured while updating Team", exp)
                    .addContextValue("Team: ", updatedTeam)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }

    }

    private void doTeamsDataTransfer(Team updatableTeams, Team updatedTeams) {
        updatableTeams.setLeague(updatedTeams.getLeague());
        updatableTeams.setName(updatedTeams.getName());
        updatableTeams.setTeamId(updatedTeams.getTeamId());
        updatableTeams.setPlayers(updatedTeams.getPlayers());
    }

    /**
     *
     * @param teamId
     * @throws com.gracenote.sample.project.exceptions.TeamNotFoundException
     */
    @Override
    public void removeTeam(
            @Valid @NotNull(message = "Team id must not be null") Long teamId)
            throws TeamNotFoundException {

        try {
            Team managedTeam = findByTeamId(teamId);
            super.remove(managedTeam);

            LOGGER.info("Teams with team id {0} has been removed successfully.",
                    managedTeam.getTeamId());
        } catch (RuntimeException exp) {
            throw new ContextedRuntimeException("An unexpected error occured while removing Teams", exp)
                    .addContextValue("Team id", teamId)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }
    
}
