/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.service.rs;

import com.gracenote.sample.project.entities.GameSeason;
import com.gracenote.sample.project.exceptions.GameSeasonNotFoundException;
import com.gracenote.sample.project.ut.vo.JsonEntityBuilder;
import com.gracenote.sample.project.validators.PagingValidator;
import com.gracenote.sample.project.mappers.GameSeasonNotFoundMapper;
import com.gracenote.sample.project.services.GameSeasonFacadeLocal;
import com.gracenote.sample.project.ut.monitor.HitCounterInterceptor;
import com.gracenote.sample.project.ut.monitor.TimeInMethodInterceptor;
import com.gracenote.sample.project.utility.Logger;
import com.gracenote.sample.project.utility.ThreadManagerService;
import static com.gracenote.sample.project.utility.ThreadManagerService.REFERER;
import com.gracenote.sample.project.utility.ThreadNameTrackingRunnable;
import com.gracenote.sample.project.utility.Util;
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
import javax.servlet.http.HttpServletRequest;
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
import javax.json.stream.JsonCollectors;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Gbenga
 */
@RequestScoped
@Path("seasons")
@Produces({MediaType.APPLICATION_JSON})
@Interceptors({HitCounterInterceptor.class, TimeInMethodInterceptor.class})
public class GameSeasonReource {

    private static final Logger LOGGER = new Logger(GameSeasonReource.class.getName());

    @Inject
    ThreadManagerService utService;
    @Inject
    GameSeasonNotFoundMapper seasonErrorMapper;

    @Inject
    JsonEntityBuilder seasonsVo;

    @Inject
    private GameSeasonFacadeLocal seasonsFacade;

    @Context
    HttpServletRequest request;
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
     * For example: GET /seasons?pgNo=1&pgSize=10
     *
     * @param pageNumber
     * @param pageSize
     * @param asyncResponse
     */
    @GET
    public void gameSeasonsPaginated(@DefaultValue("1")
            @QueryParam("pgNo")
            @Valid
            @NotNull(message = "Page number must not be null")
            @PagingValidator(message = "Page number must be greater than 0") Integer pageNumber,
            @DefaultValue("10")
            @QueryParam("pgSize")
            @Valid
            @NotNull(message = "Page size must not be null")
            @PagingValidator(message = "Page size must be greater than 0") Integer pageSize,
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {

                Map<Long, List<GameSeason>> seasonsMapList = seasonsFacade
                        .findAllSeasonsPaginated(Util.getPageNumber(pageNumber), Util.getPageSize(pageSize));

                long key = (Long) seasonsMapList.keySet().toArray()[0];
                if (seasonsMapList.isEmpty() || key <= 0) {
                    Response response = Response.status(Response.Status.NO_CONTENT).build();
                    asyncResponse.resume(response);
                }

                if (key > 0) {
                    Collection<GameSeason> seasonList = seasonsMapList.get(key);
                    JsonArray jsonArray = seasonList.stream().map(this::buildGameSeasonJson)
                            .collect(JsonCollectors.toJsonArray());

                    String jsonStr = buildObjectFromArrayJson(jsonArray, key).toString();
                    Response response = Response.ok(jsonStr).build();
                    asyncResponse.resume(response);
                }
            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while retrieving Seasons: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    /**
     * For example: GET /seasons
     *
     * @param asyncResponse
     */
    @GET
    public void allGameSeasons(@Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {

                Map<Long, List<GameSeason>> seasonsMapList = seasonsFacade.findAllSeasons();

                long key = (Integer) seasonsMapList.keySet().toArray()[0];
                if (seasonsMapList.isEmpty() || key <= 0) {
                    Response response = Response.status(Response.Status.NO_CONTENT).build();
                    asyncResponse.resume(response);
                }

                if (key > 0) {
                    Collection<GameSeason> seasonList = seasonsMapList.get(key);
                    JsonArray jsonArray = seasonList.stream().map(this::buildGameSeasonJson)
                            .collect(JsonCollectors.toJsonArray());

                    String jsonStr = buildObjectFromArrayJson(jsonArray, key).toString();
                    Response response = Response.ok(jsonStr).build();
                    asyncResponse.resume(response);
                }
            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while retrieving Seasons: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    /**
     * For example: GET /seasons/123
     *
     * @param seasonId
     * @param asyncResponse
     */
    @GET
    @Path("{id}")
    public void getGameSeason(@PathParam("id")
            @Valid @NotNull(message = "GameSeason id must not be null") Long seasonId,
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {
                GameSeason season = seasonsFacade.findBySeasonId(seasonId);

                String jsonStr = buildGameSeasonJson(season).toString();
                Response response = Response.ok(jsonStr).build();
                asyncResponse.resume(response);
            } catch (GameSeasonNotFoundException ex) {
                LOGGER.error("Error occured while retrieving GameSeason by Id: {0}", ex.getMessage());

                Response response = seasonErrorMapper.toResponse(ex);
                asyncResponse.resume(response);
            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while retrieving GameSeason by Id: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    /**
     * For example: POST /seasons
     *
     * @param newSeason
     * @param asyncResponse
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    public void createGameSeason(
            @Valid @NotNull(message = "GameSeason passed in request cannot be null") GameSeason newSeason,
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {
                GameSeason gameSeason = seasonsFacade.createGameSeason(newSeason);

                Response response = Response.created(getGameSeasonLocation(gameSeason)).build();
                asyncResponse.resume(response);
            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while creating GameSeason: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    /**
     * For example: PUT /seasons
     *
     * @param updatedSeason
     * @param asyncResponse
     */
    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    public void updateCountryService(
            @Valid @NotNull(message = "GameSeason passed as parameter cannot be null") GameSeason updatedSeason,
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        try {
            GameSeason country = seasonsFacade.editGameSeason(updatedSeason);

            String jsonStr = buildGameSeasonJson(country).toString();
            Response response = Response.ok(jsonStr).build();
            asyncResponse.resume(response);
        } catch (GameSeasonNotFoundException ex) {
            LOGGER.error("Error occured while updating GameSeason: {0}", ex.getMessage());
            Response response = seasonErrorMapper.toResponse(ex);
            asyncResponse.resume(response);
        } catch (RuntimeException ex) {
            LOGGER.error("Unexpected error occured while updating GameSeason: {0}", ex.getMessage());
            Response response = Response.serverError().build();
            asyncResponse.resume(response);
        }
    }

    /**
     * For example: DELETE /seasons/123
     *
     * @param seasonId
     * @param asyncResponse
     */
    @DELETE
    @Path("{id}")
    public void removeCountryService(@PathParam("id")
            @Valid @NotNull(message = "GameSeason id must not be null") Long seasonId,
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {
                seasonsFacade.removeGameSeason(seasonId);

                Response response = Response.ok().build();
                asyncResponse.resume(response);
            } catch (GameSeasonNotFoundException ex) {
                LOGGER.error("Error occured while removing GameSeason: {0}", ex.getMessage());
                Response response = seasonErrorMapper.toResponse(ex);
                asyncResponse.resume(response);
            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while removing GameSeason: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    private JsonObject buildObjectFromArrayJson(JsonArray seasonsArray, Long recordCount) {
        return Json.createObjectBuilder()
                .add("totalRecords", recordCount)
                .add("GameSeasons", Json.createArrayBuilder(seasonsArray)
                        .build())
                .build();
    }

    private URI getGameSeasonLocation(GameSeason season) {
        return uriInfo.getBaseUriBuilder()
                .path(GameSeasonReource.class)
                .path(GameSeasonReource.class, "getGameSeason")
                .build(season.getSeasonId());
    }

    private JsonObject buildGameSeasonJson(GameSeason season) {
        URI selfUri = getGameSeasonLocation(((GameSeason) season));

        return seasonsVo.buildForGameSeasons(season, selfUri);
    }
}
