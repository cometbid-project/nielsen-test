/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.services;

import com.gracenote.sample.project.entities.Players;
import com.gracenote.sample.project.exceptions.PlayerNotFoundException;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;

/**
 *
 * @author Gbenga
 */
@Local
public interface PlayersFacadeLocal {

    Players createPlayer(Players newPlayer);

    Players editPlayer(Players updatedPlayer) throws PlayerNotFoundException;

    void removePlayer(Long playerId) throws PlayerNotFoundException;

    Players findByPlayerId(Long seasonId) throws PlayerNotFoundException;

    Map<Long, List<Players>> findAllPlayersByTeamPaginated(Integer pageNumber,
            Integer pageSize, Long teamId);

    Map<Long, List<Players>> findAllPlayersByTeam(Long teamId);

}
