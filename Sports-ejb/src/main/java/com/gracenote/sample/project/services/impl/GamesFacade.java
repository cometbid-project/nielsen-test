/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.services.impl;

import com.gracenote.sample.project.entities.Games;
import com.gracenote.sample.project.entities.Team;
import com.gracenote.sample.project.exceptions.GameNotFoundException;
import com.gracenote.sample.project.qualifiers.Logged;
import com.gracenote.sample.project.services.AbstractFacade;
import com.gracenote.sample.project.services.GamesFacadeLocal;
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
public class GamesFacade extends AbstractFacade<Games> implements GamesFacadeLocal {

    @PersistenceContext(unitName = "SportsReport-ejb1.0PU")
    private EntityManager em;

    private static final Logger LOGGER = new Logger(GamesFacade.class.getName());

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public GamesFacade() {
        super(Games.class);
    }

    @Override
    public Map<Long, List<Games>> findAllGamesByLeagueAndSeasonPaginated(
            @Valid @NotNull(message = "Page number must not be null")
            @PagingValidator(message = "Page number must be greater than 0") Integer pageNumber,
            @Valid @NotNull(message = "Page size must not be null")
            @PagingValidator(message = "Page size must be greater than 0") Integer pageSize,
            @Valid @NotNull(message = "League id must not be null") Long leagueId,
            @Valid @NotNull(message = "Season id must not be null") Long seasonId) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("seasonId", seasonId);
        parameters.put("leagueId", leagueId);

        try {
            int offset = (pageNumber - 1) * pageSize;
            List<Games> gamesList = super.findWithNamedQuery("Games.findAllByLeagueAndSeason", parameters, offset, pageSize);

            Map<Long, List<Games>> mapCountRecord = null;

            if (gamesList.isEmpty()) {
                LOGGER.error("No Game Games record found");
                mapCountRecord = Collections.emptyMap();
            } else {
                Long gamesListSize = super.countWithNamedQuery("Games.findAllByLeagueAndSeason", parameters);

                mapCountRecord = new HashMap<>();
                mapCountRecord.put(gamesListSize, gamesList);
            }

            LOGGER.info("{0} Game(s) found!", gamesList);

            return Collections.unmodifiableMap(mapCountRecord);
        } catch (RuntimeException ex) {
            throw new ContextedRuntimeException("Exception occured while retrieving Games List", ex)
                    .addContextValue("Page Number", pageNumber)
                    .addContextValue("Page Size", pageSize)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }

    /**
     *
     * @param leagueId
     * @param seasonId
     * @return
     */
    @Override
    public Map<Long, List<Games>> findAllGamesByLeagueAndSeason(
            @Valid @NotNull(message = "League id must not be null") Long leagueId,
            @Valid @NotNull(message = "Season id must not be null") Long seasonId) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("leagueId", leagueId);
        parameters.put("seasonId", seasonId);

        try {
            List<Games> gamesList = super.findWithNamedQuery("Games.findAllByLeagueAndSeason", parameters);

            if (gamesList.isEmpty()) {
                LOGGER.error("No Games record found");
            }

            Long gamesListSize = new Long(gamesList.size());

            Map<Long, List<Games>> mapCountRecord = new HashMap<>();
            mapCountRecord.put(gamesListSize, gamesList);

            LOGGER.info("{0} Game(s) found!", gamesListSize);

            return Collections.unmodifiableMap(mapCountRecord);
        } catch (RuntimeException ex) {
            throw new ContextedRuntimeException("Exception occured while retrieving Games List", ex)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }

    /**
     *
     * @param gameId
     * @return
     * @throws com.gracenote.sample.project.exceptions.GameNotFoundException
     */
    @Override
    public Games findByGameId(Long gameId) throws GameNotFoundException {

        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("gameId", gameId);
            List<Games> playerList = super.findWithNamedQuery("Games.findByGameId", parameters);

            if (playerList == null || playerList.isEmpty()) {
                GameNotFoundException obj
                        = new GameNotFoundException("Game not found by id");
                obj.addContextValue("findByGameIdError", "Game not found by id: ")
                        .addContextValue("Game id", gameId)
                        .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
                throw obj;
            }
            return playerList.get(0);
        } catch (RuntimeException ex) {
            throw new ContextedRuntimeException("Unexpected error occured while retrieving Games", ex)
                    .addContextValue("Game id", gameId)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }

    /**
     *
     * @param gameId
     * @return
     * @throws com.gracenote.sample.project.exceptions.GameNotFoundException
     */
    @Override
    public Collection<Team> findTeamsInvolvedByGameId(Long gameId) throws GameNotFoundException {

        try {
            Games managedGame = findByGameId(gameId);
            if (managedGame == null) {
                GameNotFoundException obj = new GameNotFoundException("Games not found by id");
                obj.addContextValue("editGamesError", "Game not found by id: ")
                        .addContextValue("Game id: ", gameId)
                        .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
                throw obj;
            }

            return managedGame.getTeams();
        } catch (RuntimeException ex) {
            throw new ContextedRuntimeException("Unexpected error occured while retrieving Games", ex)
                    .addContextValue("Game id", gameId)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }

    /**
     *
     * @param newGame
     * @return
     */
    @Override
    public Games createGame(
            @Valid @NotNull(message = "Game passed as parameter cannot be null") Games newGame) {

        try {
            super.create(newGame);

            LOGGER.info("{0} created successfully.", newGame.getGameId());

            return newGame;
        } catch (RuntimeException exp) {
            throw new ContextedRuntimeException("An unexpected error occured while creating new Game", exp)
                    .addContextValue("Game: ", newGame)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }

    /**
     *
     * @param updatedGame
     * @return
     * @throws com.gracenote.sample.project.exceptions.GameNotFoundException
     */
    @Override
    @RetryOnFailure(attempts = 3, delay = 10, unit = TimeUnit.MILLISECONDS, types = OptimisticLockException.class)
    public Games editGame(
            @Valid @NotNull(message = "Game passed as parameter cannot be null") Games updatedGame)
            throws GameNotFoundException {

        try {
            Games managedGame = findByGameId(updatedGame.getGameId());
            if (managedGame == null) {
                GameNotFoundException obj = new GameNotFoundException("Games not found by id");
                obj.addContextValue("editGamesError", "Game not found by id: ")
                        .addContextValue("Game Game: ", updatedGame)
                        .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
                throw obj;
            }

            doGamesDataTransfer(managedGame, updatedGame);
            super.edit(managedGame);

            LOGGER.info("Game with id {0} updated successfully.", managedGame.getGameId());

            return managedGame;
        } catch (OptimisticLockException ex) {
            throw ex;

        } catch (RuntimeException exp) {
            throw new ContextedRuntimeException("An unexpected error occured while updating Games", exp)
                    .addContextValue("Game Game: ", updatedGame)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }

    }

    private void doGamesDataTransfer(Games updatableGames, Games updatedGames) {
        updatableGames.setGameId(updatedGames.getGameId());
        updatableGames.setFinalResult(updatedGames.getFinalResult());
        updatableGames.setGameDate(updatedGames.getGameDate());
        updatableGames.setGoals(updatedGames.getGoals());
        updatableGames.setLeague(updatedGames.getLeague());
        updatableGames.setPenalty(updatedGames.getPenalty());
        updatableGames.setSeason(updatedGames.getSeason());
        updatableGames.setTeams(updatedGames.getTeams());
    }

    /**
     *
     * @param gameId
     * @throws com.gracenote.sample.project.exceptions.GameNotFoundException
     */
    @Override
    public void removeGame(
            @Valid @NotNull(message = "Game id must not be null") Long gameId)
            throws GameNotFoundException {

        try {
            Games managedGames = findByGameId(gameId);
            super.remove(managedGames);

            LOGGER.info("Games with game id {0} has been removed successfully.",
                    managedGames.getGameId());
        } catch (RuntimeException exp) {
            throw new ContextedRuntimeException("An unexpected error occured while removing Games", exp)
                    .addContextValue("Games id", gameId)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }

}
