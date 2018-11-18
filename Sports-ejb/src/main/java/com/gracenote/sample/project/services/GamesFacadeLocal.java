/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.services;

import com.gracenote.sample.project.entities.Games;
import com.gracenote.sample.project.entities.Team;
import com.gracenote.sample.project.exceptions.GameNotFoundException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;

/**
 *
 * @author Gbenga
 */
@Local
public interface GamesFacadeLocal {

    Games createGame(Games newGame);

    Games editGame(Games updatedGame) throws GameNotFoundException;

    void removeGame(Long gameId) throws GameNotFoundException;

    Games findByGameId(Long gameId) throws GameNotFoundException;
    
    Collection<Team> findTeamsInvolvedByGameId(Long gameId) throws GameNotFoundException;

    Map<Long, List<Games>> findAllGamesByLeagueAndSeasonPaginated(
            Integer pageNumber,
            Integer pageSize,
            Long leagueId,
            Long seasonId);

    Map<Long, List<Games>> findAllGamesByLeagueAndSeason(
            Long leagueId,
            Long seasonId);

}
