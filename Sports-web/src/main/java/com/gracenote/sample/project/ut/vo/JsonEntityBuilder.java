/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.ut.vo;

import com.gracenote.sample.project.entities.GameSeason;
import com.gracenote.sample.project.entities.Games;
import com.gracenote.sample.project.entities.League;
import com.gracenote.sample.project.entities.Players;
import com.gracenote.sample.project.entities.Team;
import java.net.URI;
import javax.json.Json;
import javax.json.JsonObject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Gbenga
 */
public class JsonEntityBuilder {

    /* @Inject
    GameSeasonReource seasonsResource;
     */
    public JsonObject buildForGameSeasons(@Valid @NotNull GameSeason seasonObj, URI selfUri) {

        return Json.createObjectBuilder()
                .add("id", seasonObj.getSeasonId())
                .add("name", seasonObj.getSeason())
                .add("_links", Json.createObjectBuilder()
                        .add("self", selfUri.toString()))
                .build();

    }

    public JsonObject buildForGames(@Valid @NotNull Games gamesObj, URI selfUri) {

        return Json.createObjectBuilder()
                .add("gameId", gamesObj.getGameId())
                .add("goals", gamesObj.getGoals())
                .add("penalty", gamesObj.getPenalty())
                .add("date", gamesObj.getGameDate().toString())
                .add("result", gamesObj.getFinalResult())
                .add("_links", Json.createObjectBuilder()
                        .add("self", selfUri.toString()))
                .add("League", gamesObj.getLeague().getLeagueId())
                .add("Season", gamesObj.getSeason().getSeasonId())
                .build();

    }

    public JsonObject buildForLeague(@Valid @NotNull League leagueObj, URI selfUri) {

        return Json.createObjectBuilder()
                .add("leagueId", leagueObj.getLeagueId())
                .add("name", leagueObj.getLeagueName())
                .add("_links", Json.createObjectBuilder()
                        .add("self", selfUri.toString()))
                .build();

    }

    public JsonObject buildForPlayers(@Valid @NotNull Players playerObj, URI selfUri) {

        return Json.createObjectBuilder()
                .add("playerId", playerObj.getPlayerId())
                .add("firstName", playerObj.getFirstName())
                .add("lastName", playerObj.getLastName())
                .add("birthdate", playerObj.getBirthDate().toString())
                .add("_links", Json.createObjectBuilder()
                        .add("self", selfUri.toString()))
                        .add("Team", playerObj.getTeam().getTeamId())
                .build();

    }

    public JsonObject buildForTeam(@Valid @NotNull Team teamObj, URI selfUri) {

        return Json.createObjectBuilder()
                .add("teamId", teamObj.getTeamId())
                .add("name", teamObj.getName())
                .add("_links", Json.createObjectBuilder()
                        .add("self", selfUri.toString())
                        .add("League", teamObj.getLeague().getLeagueId()))
                .build();

    }
}
