/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.service.rs;

import com.gracenote.sample.project.entities.Team;
import com.gracenote.sample.project.exceptions.GameNotFoundException;
import com.gracenote.sample.project.exceptions.LeagueNotFoundException;
import com.gracenote.sample.project.exceptions.TeamNotFoundException;
import com.gracenote.sample.project.mappers.GameNotFoundMapper;
import com.gracenote.sample.project.mappers.LeagueNotFoundMapper;
import com.gracenote.sample.project.mappers.TeamNotFoundMapper;
import com.gracenote.sample.project.services.GamesFacadeLocal;
import com.gracenote.sample.project.services.LeagueFacadeLocal;
import com.gracenote.sample.project.services.TeamFacadeLocal;
import com.gracenote.sample.project.ut.monitor.HitCounterInterceptor;
import com.gracenote.sample.project.ut.monitor.TimeInMethodInterceptor;
import com.gracenote.sample.project.ut.vo.JsonEntityBuilder;
import com.gracenote.sample.project.utility.Logger;
import com.gracenote.sample.project.utility.ThreadManagerService;
import static com.gracenote.sample.project.utility.ThreadManagerService.REFERER;
import com.gracenote.sample.project.utility.ThreadNameTrackingRunnable;
import com.gracenote.sample.project.utility.Util;
import com.gracenote.sample.project.validators.PagingValidator;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.stream.JsonCollectors;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Gbenga
 */
@RequestScoped
@Path("teams")
@Produces({MediaType.APPLICATION_XML})
@Interceptors({HitCounterInterceptor.class, TimeInMethodInterceptor.class})
public class TeamResource {

    private static final Logger LOGGER = new Logger(TeamResource.class.getName());

    @Inject
    ThreadManagerService utService;
    @Inject
    TeamNotFoundMapper teamErrorMapper;
    @Inject
    GameNotFoundMapper gameErrorMapper;
    @Inject
    LeagueNotFoundMapper leagueErrorMapper;
    
    @EJB
    private LeagueFacadeLocal leagueFacade;

    @Inject
    JsonEntityBuilder teamVo;

    @EJB
    private TeamFacadeLocal teamFacade;
    @EJB
    private GamesFacadeLocal gamesFacade;

    @Context
    HttpHeaders httpHeaders;
    @Context
    private UriInfo uriInfo;

    private String actionName;

    @PostConstruct
    public void initialise() {
        String referer = httpHeaders.getRequestHeader(REFERER).get(0);
        actionName = utService.createName(uriInfo.getRequestUri().toString(), referer);
    }

    /**
     * For example: GET /teams/leagueId/123?pgNo=1&pgSize=10
     *
     * @param pageNumber
     * @param pageSize
     * @param leagueId
     * @param asyncResponse
     */
    @GET
    @Path("leagueId/{leagueId}")
    public void teamPaginated(@DefaultValue("1")
            @QueryParam("pgNo")
            @Valid
            @NotNull(message = "Page number must not be null")
            @PagingValidator(message = "Page number must be greater than 0") Integer pageNumber,
            @DefaultValue("10")
            @QueryParam("pgSize")
            @Valid
            @NotNull(message = "Page size must not be null")
            @PagingValidator(message = "Page size must be greater than 0") Integer pageSize,
            @PathParam("leagueId")Long leagueId, 
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {
                Map<Long, List<Team>> teamMapList = teamFacade.findAllTeamsInLeaguePaginated(
                        Util.getPageNumber(pageNumber), Util.getPageSize(pageSize), leagueId);

                long key = (Long) teamMapList.keySet().toArray()[0];
                if (teamMapList.isEmpty() || key <= 0) {
                    Response response = Response.status(Response.Status.NO_CONTENT).build();
                    asyncResponse.resume(response);
                }

                if (key > 0) {
                    Collection<Team> teamList = teamMapList.get(key);
                    JsonArray jsonArray = teamList.stream().map(this::buildTeamJson)
                            .collect(JsonCollectors.toJsonArray());

                    String jsonStr = buildObjectFromArrayJson(jsonArray, key).toString();
                    Response response = Response.ok(jsonStr).build();
                    asyncResponse.resume(response);
                }
            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while retrieving Currencies: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    /**
     * For example: GET /teams/leagueId/123?pgNo=1&pgSize=10
     *
     * @param pageSize
     * @param asyncResponse
     * @param leagueId
     */
    @GET
    @Path("leagueId/{leagueId}")
    public void allTeamsInLeague(Integer pageSize,
          @PathParam("leagueId")Long leagueId, @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {

                Map<Long, Collection<Team>> teamMapList = leagueFacade.findTeamsByLeagueId(leagueId);

                long key = (Integer) teamMapList.keySet().toArray()[0];
                if (teamMapList.isEmpty() || key <= 0) {
                    Response response = Response.status(Response.Status.NO_CONTENT).build();
                    asyncResponse.resume(response);
                }

                if (key > 0) {
                    Collection<Team> teamList = teamMapList.get(key);
                    JsonArray jsonArray = teamList.stream().map(this::buildTeamJson)
                            .collect(JsonCollectors.toJsonArray());

                    String jsonStr = buildObjectFromArrayJson(jsonArray, key).toString();
                    Response response = Response.ok(jsonStr).build();
                    asyncResponse.resume(response);
                }

            } catch (LeagueNotFoundException ex) {
                LOGGER.error("Error occured while retrieving League by Id: {0}", ex.getMessage());
                Response response = leagueErrorMapper.toResponse(ex);
                asyncResponse.resume(response);

            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while retrieving Team: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    /**
     * For example: GET /teams/123
     *
     * @param teamId
     * @param asyncResponse
     */
    @GET
    @Path("{id}")
    public void getTeam(@PathParam("id")
            @Valid @NotNull(message = "Team id must not be null") Long teamId,
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {
                Team team = teamFacade.findByTeamId(teamId);

                String jsonStr = buildTeamJson(team).toString();
                Response response = Response.ok(jsonStr).build();
                asyncResponse.resume(response);
            } catch (TeamNotFoundException ex) {
                LOGGER.error("Error occured while retrieving Team by Id: {0}", ex.getMessage());
                Response response = teamErrorMapper.toResponse(ex);
                asyncResponse.resume(response);
            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while retrieving Team by Id: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    /**
     * For example: GET /teams/gameId/123
     *
     * @param gameId
     * @param asyncResponse
     */
    @GET
    @Path("gameId/{gameId}")
    public void getTeamsByGame(@PathParam("gameId")
            @Valid @NotNull(message = "Game id must not be null") Long gameId,
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {
                Collection<Team> teamsList = gamesFacade.findTeamsInvolvedByGameId(gameId);

                JsonArray jsonArray = teamsList.stream().map(this::buildTeamJson)
                        .collect(JsonCollectors.toJsonArray());

                String jsonStr = buildObjectFromArrayJson(jsonArray, new Long(teamsList.size())).toString();
                Response response = Response.ok(jsonStr).build();
                asyncResponse.resume(response);
            } catch (GameNotFoundException ex) {
                LOGGER.error("Error occured while retrieving Team by Id: {0}", ex.getMessage());
                Response response = gameErrorMapper.toResponse(ex);
                asyncResponse.resume(response);
            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while retrieving Team by Id: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    /**
     * For example: POST /teams
     *
     * @param newTeam
     * @param asyncResponse
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    public void createTeamService(
            @Valid @NotNull(message = "Team passed in request cannot be null") Team newTeam,
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {
                Team currency = teamFacade.createTeam(newTeam);

                Response response = Response.created(getTeamLocation(currency)).build();
                asyncResponse.resume(response);
            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while creating Team: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    /**
     * For example: PUT /teams
     *
     * @param updatedTeam
     * @param asyncResponse
     */
    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    public void updateTeamService(
            @Valid @NotNull(message = "Team passed as parameter cannot be null") Team updatedTeam,
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {

            try {
                Team currency = teamFacade.editTeam(updatedTeam);

                String jsonStr = buildTeamJson(currency).toString();
                Response response = Response.ok(jsonStr).build();
                asyncResponse.resume(response);
            } catch (TeamNotFoundException ex) {
                LOGGER.error("Error occured while updating Team: {0}", ex.getMessage());
                Response response = teamErrorMapper.toResponse(ex);
                asyncResponse.resume(response);
            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while updating Team: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    /**
     * For example: DELETE /teams/123
     *
     * @param gameId
     * @param asyncResponse
     */
    @DELETE
    @Path("{id}")
    public void removeTeamService(@PathParam("id")
            @Valid @NotNull(message = "Team id must not be null") Long gameId,
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {
                teamFacade.removeTeam(gameId);

                Response response = Response.ok().build();
                asyncResponse.resume(response);
            } catch (TeamNotFoundException ex) {
                LOGGER.error("Error occured while removing Team: {0}", ex.getMessage());
                Response response = teamErrorMapper.toResponse(ex);
                asyncResponse.resume(response);
            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while removing Team: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    private JsonObject buildObjectFromArrayJson(JsonArray teamArray, Long recordCount) {
        return Json.createObjectBuilder()
                .add("totalRecords", recordCount)
                .add("Team", Json.createArrayBuilder(teamArray)
                        .build())
                .build();
    }

    private URI getTeamLocation(Team team) {
        return uriInfo.getBaseUriBuilder()
                .path(TeamResource.class)
                .path(TeamResource.class, "getTeam")
                .build(team.getTeamId());
    }

    private JsonObject buildTeamJson(Team team) {
        URI selfUri = getTeamLocation(((Team) team));

        return teamVo.buildForTeam(team, selfUri);
    }
}
