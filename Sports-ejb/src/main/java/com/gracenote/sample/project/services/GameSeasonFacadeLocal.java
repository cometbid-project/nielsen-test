/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.services;

import com.gracenote.sample.project.entities.GameSeason;
import com.gracenote.sample.project.exceptions.GameSeasonNotFoundException;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;

/**
 *
 * @author Gbenga
 */
@Local
public interface GameSeasonFacadeLocal {

    GameSeason createGameSeason(GameSeason newSeason);

    GameSeason editGameSeason(GameSeason updatedSeason) throws GameSeasonNotFoundException;

    void removeGameSeason(Long seasonId) throws GameSeasonNotFoundException;

    GameSeason findBySeasonId(Long seasonId) throws GameSeasonNotFoundException;

    Map<Long, List<GameSeason>> findAllSeasonsPaginated(
            Integer pageNumber,
            Integer pageSize);

    Map<Long, List<GameSeason>> findAllSeasons();

    //int countSeasons();
}
