/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.service.rs;

import com.gracenote.sample.project.entities.Games;
import com.gracenote.sample.project.exceptions.GameNotFoundException;
import com.gracenote.sample.project.exceptions.InvalidParameterException;
import com.gracenote.sample.project.ut.vo.JsonEntityBuilder;
import com.gracenote.sample.project.mappers.GameNotFoundMapper;
import com.gracenote.sample.project.services.GamesFacadeLocal;
import com.gracenote.sample.project.ut.monitor.HitCounterInterceptor;
import com.gracenote.sample.project.ut.monitor.TimeInMethodInterceptor;
import com.gracenote.sample.project.utility.Logger;
import com.gracenote.sample.project.utility.ThreadManagerService;
import static com.gracenote.sample.project.utility.ThreadManagerService.REFERER;
import com.gracenote.sample.project.utility.ThreadNameTrackingRunnable;
import com.gracenote.sample.project.validators.PagingValidator;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.stream.JsonCollectors;
import javax.servlet.http.HttpServletRequest;
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
@Path("games")
@Produces({MediaType.APPLICATION_JSON})
@Interceptors({HitCounterInterceptor.class, TimeInMethodInterceptor.class})
public class GameResource {

    private static final Logger LOGGER = new Logger(GameResource.class.getName());

    @Inject
    private GamesFacadeLocal gamesFacade;
    
    @Inject
    JsonEntityBuilder gameVo;

    @Inject
    ThreadManagerService utService;
    @Inject
    GameNotFoundMapper gameErrorMapper;

    @Context
    HttpServletRequest request;
    @Context
    HttpHeaders httpHeaders;
    @Context
    private UriInfo uriInfo;

    private String actionName;

    @PostConstruct
    public void initialise() {
        if(httpHeaders == null){
           throw new RuntimeException(String.format("At least one Header field must be specified: {0}", 
                   "Mandatory: REFERER"));
        }
        String referer = httpHeaders.getRequestHeader(REFERER).get(0);
        actionName = utService.createName(uriInfo.getRequestUri().toString(), referer);
    }

    /**
     * For example: GET /games/page/leagueId/123/seasonId/342?pgNo=1&pgSize=10
     *
     * @param pageNumber
     * @param pageSize
     * @param leagueId
     * @param seasonId
     * @param asyncResponse
     */
    @GET
    @Path("/page/leagueId/{leagueId}/seasonId/{seasonId}")
    public void gamesPaginatedResource(
            @DefaultValue("1")
            @QueryParam("pgNo")
            @Valid
            @PagingValidator(message = "Page number must be greater than 0") Integer pageNumber,
            @DefaultValue("10")
            @QueryParam("pgSize")
            @Valid
            @PagingValidator(message = "Page size must be greater than 0") Integer pageSize,
            @PathParam("leagueId") @NotNull(message = "League id must be specified")Long leagueId, 
            @PathParam("seasonId") @NotNull(message = "Season id must be specified") Long seasonId, 
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {
                Map<Long, List<Games>> gamesMapList = gamesFacade.findAllGamesByLeagueAndSeasonPaginated(                       
                        pageNumber, pageSize, leagueId, seasonId);

                Long key = (Long) gamesMapList.keySet().toArray()[0];
                if (gamesMapList.isEmpty() || key <= 0) {
                    Response response = Response.status(Response.Status.NO_CONTENT).build();
                    asyncResponse.resume(response);
                }

                if (key > 0) {
                    Collection<Games> gamesList = gamesMapList.get(key);
                    JsonArray jsonArray = gamesList.stream().map(this::buildGamesJson)
                            .collect(JsonCollectors.toJsonArray());

                    String jsonStr = buildObjectFromArrayJson(jsonArray, key).toString();
                    Response response = Response.ok(jsonStr).build();
                    asyncResponse.resume(response);
                }
            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while retrieving Games: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }
    
    /**
     * For example: GET /games
     *
     * @param asyncResponse
     * @param seasonId
     * @param leagueId
     */
    @GET
    @Path("leagueId/{leagueId}/seasonId/{seasonId}")
    public void allGamesResource(
            @PathParam("leagueId") @NotNull(message = "League id must be specified")Long leagueId, 
            @PathParam("seasonId") @NotNull(message = "Season id must be specified") Long seasonId, 
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {

                Map<Long, List<Games>> gamesMapList = gamesFacade
                        .findAllGamesByLeagueAndSeason(leagueId, seasonId);

                Long key = (Long) gamesMapList.keySet().toArray()[0];
                if (gamesMapList.isEmpty() || key <= 0) {
                    Response response = Response.status(Response.Status.NO_CONTENT).build();
                    asyncResponse.resume(response);
                }

                if (key > 0) {
                    Collection<Games> gameList = gamesMapList.get(key);
                    JsonArray jsonArray = gameList.stream().map(this::buildGamesJson)
                            .collect(JsonCollectors.toJsonArray());

                    String jsonStr = buildObjectFromArrayJson(jsonArray, key).toString();
                    Response response = Response.ok(jsonStr).build();
                    asyncResponse.resume(response);
                }
            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while retrieving Games: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }


    /**
     * For example: GET /games/123
     *
     * @param gameId
     * @param asyncResponse
     */
    @GET
    @Path("{id}")
    public void getGamesResource(
            @PathParam("id")
            @Valid @NotNull(message = "Game id must be specified") Long gameId,
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {
                Games games = gamesFacade.findByGameId(gameId);

                String jsonStr = buildGamesJson(games).toString();
                Response response = Response.ok(jsonStr).build();
                asyncResponse.resume(response);
            } catch (GameNotFoundException ex) {
                LOGGER.error("Error occured while retrieving Games by Id: {0}", ex.getMessage());
                Response response = gameErrorMapper.toResponse(ex);
                asyncResponse.resume(response);
            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while retrieving Games by Id: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    /**
     * For example: POST /games
     *
     * @param newGames
     * @param asyncResponse
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    public void createGameResource(
            @Valid @NotNull(message = "Game must be specified in the body of request") Games newGames,
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {
                Games games = gamesFacade.createGame(newGames);

                Response response = Response.created(getGamesLocation(games)).build();
                asyncResponse.resume(response);
            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while creating Games: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    /**
     * For example: PUT /games
     *
     * @param updatedGames
     * @param asyncResponse
     */
    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    public void updateGameResource(
            @Valid @NotNull(message = "Game must be specified in the body of request") Games updatedGames,
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {

            try {
                Games games = gamesFacade.editGame(updatedGames);

                String jsonStr = buildGamesJson(games).toString();
                Response response = Response.ok(jsonStr).build();
                asyncResponse.resume(response);
            } catch (GameNotFoundException ex) {
                LOGGER.error("Error occured while updating Games: {0}", ex.getMessage());
                Response response = gameErrorMapper.toResponse(ex);
                asyncResponse.resume(response);
            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while updating Games: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    /**
     * For example: DELETE /games/123
     *
     * @param gameId
     * @param asyncResponse
     */
    @DELETE
    @Path("{id}")
    public void removeGameResource(
            @PathParam("id")
            @Valid @NotNull(message = "Game id must be specified") Long gameId,
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {
                gamesFacade.removeGame(gameId);

                Response response = Response.ok().build();
                asyncResponse.resume(response);
            } catch (GameNotFoundException ex) {
                LOGGER.error("Error occured while removing Games: {0}", ex.getMessage());
                Response response = gameErrorMapper.toResponse(ex);
                asyncResponse.resume(response);
            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while removing Games: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    private JsonObject buildObjectFromArrayJson(JsonArray gamesArray, Long recordCount) {
        return Json.createObjectBuilder()
                .add("totalRecords", recordCount)
                .add("Games", Json.createArrayBuilder(gamesArray)
                        .build())
                .build();
    }

    private URI getGamesLocation(Games games) {
        return uriInfo.getBaseUriBuilder()
                .path(GameResource.class)
                .path(GameResource.class, "getGames")
                .build(games.getGameId());
    }

    private JsonObject buildGamesJson(Games games) {
        URI selfUri = getGamesLocation(((Games) games));

        return gameVo.buildForGames(games, selfUri);
    }

}
