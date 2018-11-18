/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.services;

import com.gracenote.sample.project.entities.Players;
import com.gracenote.sample.project.entities.Team;
import com.gracenote.sample.project.exceptions.TeamNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Gbenga
 */
@Local
public interface TeamFacadeLocal {

    Team createTeam(Team newTeam);

    Team editTeam(Team updatedTeam) throws TeamNotFoundException;

    void removeTeam(Long teamId) throws TeamNotFoundException;

    Team findByTeamId(Long teamId) throws TeamNotFoundException;

    Collection<Players> findPlayersByTeamId(Long teamId) throws TeamNotFoundException;

    Map<Long, List<Team>> findAllTeamsInLeaguePaginated(Integer pageNumber,
            Integer pageSize, Long leagueId);

}
