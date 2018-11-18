/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.services.impl;

import com.gracenote.sample.project.entities.Players;
import com.gracenote.sample.project.exceptions.PlayerNotFoundException;
import com.gracenote.sample.project.qualifiers.Logged;
import com.gracenote.sample.project.services.AbstractFacade;
import com.gracenote.sample.project.services.PlayersFacadeLocal;
import com.gracenote.sample.project.utility.Logger;
import com.gracenote.sample.project.utility.Util;
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
public class PlayersFacade extends AbstractFacade<Players> implements PlayersFacadeLocal {

    @PersistenceContext(unitName = "SportsReport-ejb1.0PU")
    private EntityManager em;

    private static final Logger LOGGER = new Logger(PlayersFacade.class.getName());

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public PlayersFacade() {
        super(Players.class);
    }

    @Override
    public Map<Long, List<Players>> findAllPlayersByTeamPaginated(
            @Valid @NotNull(message = "Page number must not be null")
            @PagingValidator(message = "Page number must be greater than 0") Integer pageNumber,
            @Valid @NotNull(message = "Page size must not be null")
            @PagingValidator(message = "Page size must be greater than 0") Integer pageSize,
            @Valid @NotNull(message = "Team id must not be null") Long teamId) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("teamId", teamId);        

        try {
            int offset = (pageNumber - 1) * pageSize;
            List<Players> playersList = super.findWithNamedQuery("Players.findAllByTeam", parameters, offset, pageSize);
            
            Map<Long, List<Players>> mapCountRecord = null;
            if (playersList.isEmpty()) {
                LOGGER.error("No Game Players record found");
                mapCountRecord = new HashMap<>();
            } else {
                Long playersListSize = super.countWithNamedQuery("Players.findAllByTeam", parameters);
                mapCountRecord = new HashMap<>();
                mapCountRecord.put(playersListSize, playersList);
            }
            LOGGER.info("{0} Game Player(s) found!", playersList);

            return Collections.unmodifiableMap(mapCountRecord);
        } catch (RuntimeException ex) {
            throw new ContextedRuntimeException("Exception occured while retrieving Game Players List", ex)
                    .addContextValue("Page Number", pageNumber)
                    .addContextValue("Page Size", pageSize)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }

    /**
     *
     * @param teamId
     * @return
     */
    @Override
    public Map<Long, List<Players>> findAllPlayersByTeam(
            @Valid @NotNull(message = "Team id must not be null") Long teamId) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("teamId", teamId);

        try {
            List<Players> playersList = super.findWithNamedQuery("Players.findAllByTeam", parameters);

            if (playersList.isEmpty()) {
                LOGGER.error("No Game Player record found");
            }

            Long playersListSize = new Long(playersList.size());

            Map<Long, List<Players>> mapCountRecord = new HashMap<>();
            mapCountRecord.put(playersListSize, playersList);

            LOGGER.info("{0} Game Player(s) found!", playersListSize);

            return Collections.unmodifiableMap(mapCountRecord);
        } catch (RuntimeException ex) {
            throw new ContextedRuntimeException("Exception occured while retrieving Game Player List", ex)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }

    /**
     *
     * @param playerId
     * @return
     * @throws com.gracenote.sample.project.exceptions.PlayerNotFoundException
     */
    @Override
    public Players findByPlayerId(Long playerId) throws PlayerNotFoundException {

        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("playerId", playerId);
            List<Players> playerList = super.findWithNamedQuery("Players.findByPlayerId", parameters);

            if (playerList == null || playerList.isEmpty()) {
                PlayerNotFoundException obj
                        = new PlayerNotFoundException("Game Player not found by id");
                obj.addContextValue("findByPlayerIdError", "Game Player not found by id: ")
                        .addContextValue("Game Player id", playerId)
                        .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
                throw obj;
            }
            return playerList.get(0);
        } catch (RuntimeException ex) {
            throw new ContextedRuntimeException("Unexpected error occured while retrieving Games Player", ex)
                    .addContextValue("Game Player id", playerId)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }

    /**
     *
     * @param newPlayer
     * @return
     */
    @Override
    public Players createPlayer(
            @Valid @NotNull(message = "Game Player passed as parameter cannot be null") Players newPlayer) {

        try {
            super.create(newPlayer);

            LOGGER.info("{0} created successfully.", newPlayer.getPlayerId());

            return newPlayer;
        } catch (RuntimeException exp) {
            throw new ContextedRuntimeException("An unexpected error occured while creating new Game Player", exp)
                    .addContextValue("Game Player: ", newPlayer)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }

    /**
     *
     * @param updatedPlayer
     * @return
     * @throws com.gracenote.sample.project.exceptions.PlayerNotFoundException
     */
    @Override
    @RetryOnFailure(attempts = 3, delay = 10, unit = TimeUnit.MILLISECONDS, types = OptimisticLockException.class)
    public Players editPlayer(
            @Valid @NotNull(message = "Player passed as parameter cannot be null") Players updatedPlayer)
            throws PlayerNotFoundException {

        try {
            Players managedPlayer = findByPlayerId(updatedPlayer.getPlayerId());
            if (managedPlayer == null) {
                PlayerNotFoundException obj = new PlayerNotFoundException("Players not found by id");
                obj.addContextValue("editPlayersError", "Player not found by id: ")
                        .addContextValue("Game Player: ", updatedPlayer)
                        .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
                throw obj;
            }

            doPlayersDataTransfer(managedPlayer, updatedPlayer);
            super.edit(managedPlayer);

            LOGGER.info("Game Player with id {0} updated successfully.", managedPlayer.getPlayerId());

            return managedPlayer;
        } catch (OptimisticLockException ex) {
            throw ex;

        } catch (RuntimeException exp) {
            throw new ContextedRuntimeException("An unexpected error occured while updating Players", exp)
                    .addContextValue("Game Player: ", updatedPlayer)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }

    }

    private void doPlayersDataTransfer(Players updatablePlayers, Players updatedPlayers) {
        updatablePlayers.setFirstName(updatedPlayers.getFirstName());
        updatablePlayers.setLastName(updatedPlayers.getLastName());
        updatablePlayers.setTeam(updatedPlayers.getTeam());
        updatablePlayers.setPlayerId(updatedPlayers.getPlayerId());
        updatablePlayers.setBirthDate(updatedPlayers.getBirthDate());
    }

    /**
     *
     * @param playerId
     * @throws com.gracenote.sample.project.exceptions.PlayerNotFoundException
     */
    @Override
    public void removePlayer(
            @Valid @NotNull(message = "Game Player id must not be null") Long playerId)
            throws PlayerNotFoundException {

        try {
            Players managedPlayers = findByPlayerId(playerId);
            super.remove(managedPlayers);

            LOGGER.info("Players with player id {0} has been removed successfully.",
                    managedPlayers.getPlayerId());
        } catch (RuntimeException exp) {
            throw new ContextedRuntimeException("An unexpected error occured while removing Players", exp)
                    .addContextValue("Players id", playerId)
                    .addContextValue("Time of Error: ", Util.dateFormat.format(new Date()));
        }
    }

}
