/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.services.impl;

import com.gracenote.sample.project.exceptions.GameSeasonNotFoundException;
import com.gracenote.sample.project.entities.GameSeason;
import com.gracenote.sample.project.qualifiers.Logged;
import com.gracenote.sample.project.services.AbstractFacade;
import com.gracenote.sample.project.services.GameSeasonFacadeLocal;
import com.gracenote.sample.project.utility.Util;
import com.gracenote.sample.project.utility.Logger;
import com.gracenote.sample.project.validators.PagingValidator;
import com.jcabi.aspects.RetryOnFailure;
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
public class GameSeasonFacade extends AbstractFacade<GameSeason> implements GameSeasonFacadeLocal {

    @PersistenceContext(unitName = "SportsReport-ejb1.0PU")
    private EntityManager em;

    private static final Logger LOGGER = new Logger(GameSeasonFacade.class.getName());

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public GameSeasonFacade() {
        super(GameSeason.class);
    }

    @Override
    public Map<Long, List<GameSeason>> findAllSeasonsPaginated(
            @Valid @NotNull(message = "Page number must not be null")
            @PagingValidator(message = "Page number must be greater than 0") Integer pageNumber,
            @Valid @NotNull(message = "Page size must not be null")
            @PagingValidator(message = "Page size must be greater than 0") Integer pageSize) {

        try {
            int offset = (pageNumber - 1) * pageSize;
            List<GameSeason> seasonsList = super.findWithNamedQuery("GameSeason.findAll", offset, pageSize);

            Map<Long, List<GameSeason>> mapCountRecord = null;

            if (seasonsList.isEmpty()) {
                LOGGER.error("No Game Seasons record found");
                mapCountRecord = Collections.emptyMap();
            } else {
                Long seasonsListSize = super.countWithNamedQuery("GameSeason.findAll", Collections.emptyMap());
                mapCountRecord = new HashMap<>();
                mapCountRecord.put(seasonsListSize, seasonsList);
            }
            LOGGER.info("{0} Game Season(s) found!", seasonsList);

            return Collections.unmodifiableMap(mapCountRecord);
        } catch (RuntimeException ex) {
            throw new ContextedRuntimeException("Exception occured while retrieving Game Seasons List", ex)
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
    public Map<Long, List<GameSeason>> findAllSeasons() {

        try {
            List<GameSeason> seasonsList = super.findWithNamedQuery("GameSeason.findAll", Collections.emptyMap());
            Map<Long, List<GameSeason>> mapCountRecord = null;

            if (seasonsList.isEmpty()) {
                LOGGER.error("No Game Season record found");
            }
            Long seasonsListSize = new Long(seasonsList.size());

            mapCountRecord = new HashMap<>();
            mapCountRecord.put(seasonsListSize, seasonsList);

            LOGGER.info("{0} Game Season(s) found!", seasonsListSize);

            return Collections.unmodifiableMap(mapCountRecord);
        } catch (RuntimeException ex) {
            throw new ContextedRuntimeException("Exception occured while retrieving Game Season List", ex)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }

    /**
     *
     * @param seasonId
     * @return
     * @throws
     * com.gracenote.sample.project.exceptions.GameSeasonNotFoundException
     */
    @Override
    public GameSeason findBySeasonId(Long seasonId) throws GameSeasonNotFoundException {

        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("seasonId", seasonId);
            List<GameSeason> seasonList = super.findWithNamedQuery("GameSeason.findBySeasonId", parameters);

            if (seasonList == null || seasonList.isEmpty()) {
                GameSeasonNotFoundException obj
                        = new GameSeasonNotFoundException("Game Season not found by id");
                obj.addContextValue("findBySeasonIdError", "Game Season not found by id: ")
                        .addContextValue("Game Season id", seasonId)
                        .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
                throw obj;
            }
            return seasonList.get(0);
        } catch (RuntimeException ex) {
            throw new ContextedRuntimeException("Unexpected error occured while retrieving Games Season", ex)
                    .addContextValue("Game Season id", seasonId)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }

    /**
     *
     * @param newSeason
     * @return
     */
    @Override
    public GameSeason createGameSeason(
            @Valid @NotNull(message = "Game Season passed as parameter cannot be null") GameSeason newSeason) {

        try {
            super.create(newSeason);

            LOGGER.info("{0} created successfully.", newSeason.getSeasonId());

            return newSeason;
        } catch (RuntimeException exp) {
            throw new ContextedRuntimeException("An unexpected error occured while creating new Game Season", exp)
                    .addContextValue("Game Season: ", newSeason)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }

    /**
     *
     * @param updatedSeason
     * @return
     * @throws
     * com.gracenote.sample.project.exceptions.GameSeasonNotFoundException
     */
    @Override
    @RetryOnFailure(attempts = 3, delay = 10, unit = TimeUnit.MILLISECONDS, types = OptimisticLockException.class)
    public GameSeason editGameSeason(
            @Valid @NotNull(message = "GameSeason passed as parameter cannot be null") GameSeason updatedSeason)
            throws GameSeasonNotFoundException {

        try {
            GameSeason managedSeason = findBySeasonId(updatedSeason.getSeasonId());
            if (managedSeason == null) {
                GameSeasonNotFoundException obj = new GameSeasonNotFoundException("GameSeason not found by id");
                obj.addContextValue("editGameSeasonError", "GameSeason not found by id: ")
                        .addContextValue("Game Season: ", updatedSeason)
                        .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
                throw obj;
            }

            doGameSeasonDataTransfer(managedSeason, updatedSeason);
            super.edit(managedSeason);

            LOGGER.info("Game Season with id {0} updated successfully.", managedSeason.getSeasonId());

            return managedSeason;
        } catch (OptimisticLockException ex) {
            throw ex;

        } catch (RuntimeException exp) {
            throw new ContextedRuntimeException("An unexpected error occured while updating GameSeason", exp)
                    .addContextValue("Game Season: ", updatedSeason)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }

    }

    private void doGameSeasonDataTransfer(GameSeason updatableGameSeason, GameSeason updatedGameSeason) {
        updatableGameSeason.setSeasonId(updatedGameSeason.getSeasonId());
        updatableGameSeason.setSeason(updatedGameSeason.getSeason());
    }

    /**
     *
     * @param seasonId
     * @throws
     * com.gracenote.sample.project.exceptions.GameSeasonNotFoundException
     */
    @Override
    public void removeGameSeason(
            @Valid @NotNull(message = "Game Season id must not be null") Long seasonId)
            throws GameSeasonNotFoundException {

        try {
            GameSeason managedGameSeason = findBySeasonId(seasonId);
            super.remove(managedGameSeason);

            LOGGER.info("GameSeason with id {0} has been removed successfully.",
                    managedGameSeason.getSeasonId());
        } catch (RuntimeException exp) {
            throw new ContextedRuntimeException("An unexpected error occured while removing GameSeason", exp)
                    .addContextValue("GameSeason id", seasonId)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }

}
