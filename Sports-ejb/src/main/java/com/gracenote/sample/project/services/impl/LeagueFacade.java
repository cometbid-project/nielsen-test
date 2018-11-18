/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.services.impl;

import com.gracenote.sample.project.entities.League;
import com.gracenote.sample.project.entities.Team;
import com.gracenote.sample.project.exceptions.LeagueNotFoundException;
import com.gracenote.sample.project.qualifiers.Logged;
import com.gracenote.sample.project.services.AbstractFacade;
import com.gracenote.sample.project.services.LeagueFacadeLocal;
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
public class LeagueFacade extends AbstractFacade<League> implements LeagueFacadeLocal {

    @PersistenceContext(unitName = "SportsReport-ejb1.0PU")
    private EntityManager em;

    private static final Logger LOGGER = new Logger(LeagueFacade.class.getName());

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public LeagueFacade() {
        super(League.class);
    }

    @Override
    public Map<Long, List<League>> findAllLeaguesPaginated(
            @Valid @NotNull(message = "Page number must not be null")
            @PagingValidator(message = "Page number must be greater than 0") Integer pageNumber,
            @Valid @NotNull(message = "Page size must not be null")
            @PagingValidator(message = "Page size must be greater than 0") Integer pageSize) {

        try {
            int offset = (pageNumber - 1) * pageSize;
            List<League> leaguesList = super.findWithNamedQuery("Leagues.findAll", offset, pageSize);

            Map<Long, List<League>> mapCountRecord = null;
            if (leaguesList.isEmpty()) {
                LOGGER.error("No Leagues record found");
                mapCountRecord = new HashMap<>();
            } else {
                Long leaguesListSize = super.countWithNamedQuery("Leagues.findAll", Collections.emptyMap());
                mapCountRecord = new HashMap<>();
                mapCountRecord.put(leaguesListSize, leaguesList);
            }
            LOGGER.info("{0} League(s) found!", leaguesList);

            return Collections.unmodifiableMap(mapCountRecord);
        } catch (RuntimeException ex) {
            throw new ContextedRuntimeException("Exception occured while retrieving Leagues List", ex)
                    .addContextValue("Page Number", pageNumber)
                    .addContextValue("Page Size", pageSize)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }

    /**
     *
     * @return
     */
    @Override
    public Map<Long, List<League>> findAllLeagues() {

        try {
            List<League> leaguesList = super.findWithNamedQuery("Leagues.findAll", Collections.emptyMap());

            if (leaguesList.isEmpty()) {
                LOGGER.error("No League record found");
            }

            Long leaguesListSize = new Long(leaguesList.size());

            Map<Long, List<League>> mapCountRecord = new HashMap<>();
            mapCountRecord.put(leaguesListSize, leaguesList);

            LOGGER.info("{0} League(s) found!", leaguesListSize);

            return Collections.unmodifiableMap(mapCountRecord);
        } catch (RuntimeException ex) {
            throw new ContextedRuntimeException("Exception occured while retrieving League List", ex)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }

    /**
     *
     * @param leagueId
     * @return
     * @throws com.gracenote.sample.project.exceptions.LeagueNotFoundException
     */
    @Override
    public League findByLeagueId(Long leagueId) throws LeagueNotFoundException {

        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("leagueId", leagueId);
            List<League> leagueList = super.findWithNamedQuery("League.findByLeagueId", parameters);

            if (leagueList == null || leagueList.isEmpty()) {
                LeagueNotFoundException obj
                        = new LeagueNotFoundException("League not found by id");
                obj.addContextValue("findByLeagueIdError", "League not found by id: ")
                        .addContextValue("League id", leagueId)
                        .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
                throw obj;
            }
            return leagueList.get(0);
        } catch (RuntimeException ex) {
            throw new ContextedRuntimeException("Unexpected error occured while retrieving League", ex)
                    .addContextValue("League id", leagueId)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }

    /**
     *
     * @param leagueId
     * @return
     * @throws com.gracenote.sample.project.exceptions.LeagueNotFoundException
     */
    @Override
    public Map<Long, Collection<Team>> findTeamsByLeagueId(Long leagueId) throws LeagueNotFoundException {

        try {
            League managedLeague = findByLeagueId(leagueId);
            if (managedLeague == null) {
                LeagueNotFoundException obj = new LeagueNotFoundException("League not found by id");
                obj.addContextValue("editLeagueError", "League not found by id: ")
                        .addContextValue("League id: ", leagueId)
                        .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
                throw obj;
            }

            Map<Long, Collection<Team>> mapCountRecord = new HashMap<>();
            Collection<Team> teamList = managedLeague.getLeagueTeams();
            mapCountRecord.put(new Long(teamList.size()), teamList);
            
            return mapCountRecord;
        } catch (RuntimeException ex) {
            throw new ContextedRuntimeException("Unexpected error occured while retrieving Games", ex)
                    .addContextValue("League id", leagueId)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }

    /**
     *
     * @param newLeague
     * @return
     */
    @Override
    public League createLeague(
            @Valid @NotNull(message = "League passed as parameter cannot be null") League newLeague) {

        try {
            super.create(newLeague);

            LOGGER.info("{0} created successfully.", newLeague.getLeagueId());

            return newLeague;
        } catch (RuntimeException exp) {
            throw new ContextedRuntimeException("An unexpected error occured while creating new League", exp)
                    .addContextValue("League: ", newLeague)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }

    /**
     *
     * @param updatedLeague
     * @return
     * @throws com.gracenote.sample.project.exceptions.LeagueNotFoundException
     */
    @Override
    @RetryOnFailure(attempts = 3, delay = 10, unit = TimeUnit.MILLISECONDS, types = OptimisticLockException.class)
    public League editLeague(
            @Valid @NotNull(message = "League passed as parameter cannot be null") League updatedLeague)
            throws LeagueNotFoundException {

        try {
            League managedLeague = findByLeagueId(updatedLeague.getLeagueId());
            if (managedLeague == null) {
                LeagueNotFoundException obj = new LeagueNotFoundException("Leagues not found by id");
                obj.addContextValue("editLeaguesError", "League not found by id: ")
                        .addContextValue("League: ", updatedLeague)
                        .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
                throw obj;
            }

            doLeaguesDataTransfer(managedLeague, updatedLeague);
            super.edit(managedLeague);

            LOGGER.info("League with id {0} updated successfully.", managedLeague.getLeagueId());

            return managedLeague;
        } catch (OptimisticLockException ex) {
            throw ex;

        } catch (RuntimeException exp) {
            throw new ContextedRuntimeException("An unexpected error occured while updating League", exp)
                    .addContextValue("League: ", updatedLeague)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }

    }

    private void doLeaguesDataTransfer(League updatableLeagues, League updatedLeagues) {
        updatableLeagues.setLeagueId(updatedLeagues.getLeagueId());
        updatableLeagues.setLeagueName(updatedLeagues.getLeagueName());
        updatableLeagues.setLeagueTeams(updatedLeagues.getLeagueTeams());
    }

    /**
     *
     * @param leagueId
     * @throws com.gracenote.sample.project.exceptions.LeagueNotFoundException
     */
    @Override
    public void removeLeague(
            @Valid @NotNull(message = "League id must not be null") Long leagueId)
            throws LeagueNotFoundException {

        try {
            League managedLeague = findByLeagueId(leagueId);
            super.remove(managedLeague);

            LOGGER.info("Leagues with league id {0} has been removed successfully.",
                    managedLeague.getLeagueId());
        } catch (RuntimeException exp) {
            throw new ContextedRuntimeException("An unexpected error occured while removing Leagues", exp)
                    .addContextValue("League id", leagueId)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }

}
