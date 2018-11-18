/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.services;

import com.gracenote.sample.project.entities.League;
import com.gracenote.sample.project.entities.Players;
import com.gracenote.sample.project.entities.League;
import com.gracenote.sample.project.entities.League;
import com.gracenote.sample.project.entities.Team;
import com.gracenote.sample.project.exceptions.LeagueNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;

/**
 *
 * @author Gbenga
 */
@Local
public interface LeagueFacadeLocal {

    League createLeague(League newLeague);

    League editLeague(League updatedLeague) throws LeagueNotFoundException;

    void removeLeague(Long leagueId) throws LeagueNotFoundException;

    League findByLeagueId(Long leagueId) throws LeagueNotFoundException;

    Map<Long, List<League>> findAllLeaguesPaginated(Integer pageNumber, Integer pageSize);

    Map<Long, List<League>> findAllLeagues();
    
    Map<Long, Collection<Team>> findTeamsByLeagueId(Long leagueId) throws LeagueNotFoundException;

}
