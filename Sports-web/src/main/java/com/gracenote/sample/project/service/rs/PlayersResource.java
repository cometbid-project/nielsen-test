/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.service.rs;

import com.gracenote.sample.project.ut.monitor.HitCounterInterceptor;
import com.gracenote.sample.project.ut.monitor.TimeInMethodInterceptor;
import com.gracenote.sample.project.utility.ThreadManagerService;
import static com.gracenote.sample.project.utility.ThreadManagerService.REFERER;
import com.gracenote.sample.project.entities.Players;
import com.gracenote.sample.project.exceptions.PlayerNotFoundException;
import com.gracenote.sample.project.ut.vo.JsonEntityBuilder;
import com.gracenote.sample.project.mappers.PlayerNotFoundMapper;
import com.gracenote.sample.project.services.PlayersFacadeLocal;
import com.gracenote.sample.project.utility.Logger;
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
@Path("players")
@Produces({MediaType.APPLICATION_JSON})
@Interceptors({HitCounterInterceptor.class, TimeInMethodInterceptor.class})
public class PlayersResource {

    private static final Logger LOGGER = new Logger(PlayersResource.class.getName());

    @Inject
    JsonEntityBuilder playersVo;
    @Inject
    ThreadManagerService utService;
    @Inject
    PlayerNotFoundMapper playerErrorMapper;

    @Inject
    private PlayersFacadeLocal playerFacade;
  
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
     * For example: GET /players/teamId/123?pgNo=1&pgSize=10
     *
     * @param pageNumber
     * @param pageSize
     * @param teamId
     * @param asyncResponse
     */
    @GET
    @Path("teamId/{teamId}")
    public void getPlayersPaginated(@DefaultValue("1")
            @QueryParam("pgNo")
            @Valid
            @NotNull(message = "Page number must not be null")
            @PagingValidator(message = "Page number must be greater than 0") Integer pageNumber,
            @DefaultValue("10")
            @QueryParam("pgSize")
            @Valid
            @NotNull(message = "Page size must not be null")
            @PagingValidator(message = "Page size must be greater than 0") Integer pageSize,
            @PathParam("teamId") Long teamId,
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {
                Map<Long, List<Players>> playerMapList = playerFacade.findAllPlayersByTeamPaginated(
                        Util.getPageNumber(pageNumber), Util.getPageSize(pageSize), teamId );

                long key = (Integer) playerMapList.keySet().toArray()[0];
                if (playerMapList.isEmpty() || key <= 0) {
                    Response response = Response.status(Response.Status.NO_CONTENT).build();
                    asyncResponse.resume(response);
                }

                if (key > 0) {
                    Collection<Players> playerList = playerMapList.get(key);
                    JsonArray jsonArray = playerList.stream().map(this::buildPlayerJson)
                            .collect(JsonCollectors.toJsonArray());

                    String jsonStr = buildObjectFromArrayJson(jsonArray, key).toString();
                    Response response = Response.ok(jsonStr).build();
                    asyncResponse.resume(response);
                }
            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while retrieving Player: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

   

    /**
     * For example: GET /players/123
     *
     * @param playerId
     * @param asyncResponse
     */
    @GET
    @Path("{id}")
    public void getPlayer(@PathParam("id")
            @Valid @NotNull(message = "Player id must not be null") Long playerId,
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {
                Players player = playerFacade.findByPlayerId(playerId);

                String jsonStr = buildPlayerJson(player).toString();
                Response response = Response.ok(jsonStr).build();
                asyncResponse.resume(response);
            } catch (PlayerNotFoundException ex) {
                LOGGER.error("Error occured while retrieving Player by Id: {0}", ex.getMessage());
                Response response = playerErrorMapper.toResponse(ex);
                asyncResponse.resume(response);

            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while retrieving Player by Id: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    /**
     * For example: POST /players
     *
     * @param newPlayer
     * @param asyncResponse
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    public void createPlayerService(
            @Valid @NotNull(message = "Player passed in request cannot be null") Players newPlayer,
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {
                Players player = playerFacade.createPlayer(newPlayer);

                Response response = Response.created(getPlayerLocation(player)).build();
                asyncResponse.resume(response);
            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while creating Player: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    /**
     * For example: PUT /players
     *
     * @param updatedPlayer
     * @param asyncResponse
     */
    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    public void updatePlayerService(
            @Valid @NotNull(message = "Player passed as parameter cannot be null") Players updatedPlayer,
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {
                Players player = playerFacade.editPlayer(updatedPlayer);

                String jsonStr = buildPlayerJson(player).toString();
                Response response = Response.ok().build();
                asyncResponse.resume(response);
            } catch (PlayerNotFoundException ex) {
                LOGGER.error("Error occured while updating Player: {0}", ex.getMessage());
                Response response = playerErrorMapper.toResponse(ex);
                asyncResponse.resume(response);
            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while updating Player: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    /**
     * For example: DELETE /players/123
     *
     * @param playerId
     * @param asyncResponse
     */
    @DELETE
    @Path("{id}")
    public void removePlayerService(
            @Valid @NotNull(message = "Player id must not be null") @PathParam("id") Long playerId,
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {
                playerFacade.removePlayer(playerId);

                Response response = Response.ok().build();
                asyncResponse.resume(response);
            } catch (PlayerNotFoundException ex) {
                LOGGER.error("Error occured while removing Player: {0}", ex.getMessage());
                Response response = playerErrorMapper.toResponse(ex);
                asyncResponse.resume(response);

            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while removing Player: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    private JsonObject buildObjectFromArrayJson(JsonArray playerArray, Long recordCount) {
        return Json.createObjectBuilder()
                .add("totalRecords", recordCount)
                .add("player", Json.createArrayBuilder(playerArray)
                        .build())
                .build();
    }

    private URI getPlayerLocation(Players player) {
        return uriInfo.getBaseUriBuilder()
                .path(PlayersResource.class)
                .path(PlayersResource.class, "getPlayer")
                .build(player.getPlayerId());
    }

    private JsonObject buildPlayerJson(Players player) {
        URI selfUri = getPlayerLocation(((Players) player));

        return playersVo.buildForPlayers(player, selfUri);
    }
}
