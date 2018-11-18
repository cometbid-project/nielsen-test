/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gracenote.sample.project.service.rs;

import com.gracenote.sample.project.entities.League;
import com.gracenote.sample.project.exceptions.LeagueNotFoundException;
import com.gracenote.sample.project.ut.vo.JsonEntityBuilder;
import com.gracenote.sample.project.mappers.LeagueNotFoundMapper;
import com.gracenote.sample.project.services.LeagueFacadeLocal;
import com.gracenote.sample.project.ut.monitor.HitCounterInterceptor;
import com.gracenote.sample.project.ut.monitor.TimeInMethodInterceptor;
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
@Path("leagues")
@Produces({MediaType.APPLICATION_JSON})
@Interceptors({HitCounterInterceptor.class, TimeInMethodInterceptor.class})
public class LeagueResource {

    private static final Logger LOGGER = new Logger(LeagueResource.class.getName());

    @Inject
    private LeagueFacadeLocal leagueFacade;

    @Inject
    JsonEntityBuilder leagueVo;

    @Inject
    ThreadManagerService utService;
    @Inject
    LeagueNotFoundMapper leagueErrorMapper;

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
     * For example: GET /leagues?pgNo=1&pgSize=10
     *
     * @param pageNumber
     * @param pageSize
     * @param asyncResponse
     */
    @GET
    public void getLeaguesPaginated(@DefaultValue("1")
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
                Map<Long, List<League>> leagueMapList = leagueFacade.findAllLeaguesPaginated(
                        Util.getPageNumber(pageNumber), Util.getPageSize(pageSize));

                long key = (Integer) leagueMapList.keySet().toArray()[0];
                if (leagueMapList.isEmpty() || key <= 0) {
                    Response response = Response.status(Response.Status.NO_CONTENT).build();
                    asyncResponse.resume(response);
                }

                if (key > 0) {
                    Collection<League> leagueList = leagueMapList.get(key);
                    JsonArray jsonArray = leagueList.stream().map(this::buildLeagueJson)
                            .collect(JsonCollectors.toJsonArray());

                    String jsonStr = buildObjectFromArrayJson(jsonArray, key).toString();
                    Response response = Response.ok(jsonStr).build();
                    asyncResponse.resume(response);
                }
            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while retrieving League: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    /**
     * For example: GET /leagues
     *
     * @param asyncResponse
     */
    @GET
    public void allLeagues(@Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {

                Map<Long, List<League>> leagueMapList = leagueFacade.findAllLeagues();

                long key = (Integer) leagueMapList.keySet().toArray()[0];
                if (leagueMapList.isEmpty() || key <= 0) {
                    Response response = Response.status(Response.Status.NO_CONTENT).build();
                    asyncResponse.resume(response);
                }

                if (key > 0) {
                    Collection<League> leagueList = leagueMapList.get(key);
                    JsonArray jsonArray = leagueList.stream().map(this::buildLeagueJson)
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
     * For example: GET /leagues/123
     *
     * @param leagueId
     * @param asyncResponse
     */
    @GET
    @Path("{id}")
    public void getLeague(@PathParam("id")
            @Valid @NotNull(message = "League id must not be null") Long leagueId,
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {
                League league = leagueFacade.findByLeagueId(leagueId);

                String jsonStr = buildLeagueJson(league).toString();
                Response response = Response.ok(jsonStr).build();
                asyncResponse.resume(response);
            } catch (LeagueNotFoundException ex) {
                LOGGER.error("Error occured while retrieving League by Id: {0}", ex.getMessage());
                Response response = leagueErrorMapper.toResponse(ex);
                asyncResponse.resume(response);

            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while retrieving League by Id: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    /**
     * For example: POST /leagues
     *
     * @param newLeague
     * @param asyncResponse
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    public void createLeagueService(
            @Valid @NotNull(message = "League passed in request cannot be null") League newLeague,
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {
                League league = leagueFacade.createLeague(newLeague);

                Response response = Response.created(getLeagueLocation(league)).build();
                asyncResponse.resume(response);
            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while creating League: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    /**
     * For example: PUT /leagues
     *
     * @param updatedLeague
     * @param asyncResponse
     */
    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    public void updateLeagueService(
            @Valid @NotNull(message = "League passed as parameter cannot be null") League updatedLeague,
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {
                League league = leagueFacade.editLeague(updatedLeague);

                String jsonStr = buildLeagueJson(league).toString();
                Response response = Response.ok().build();
                asyncResponse.resume(response);
            } catch (LeagueNotFoundException ex) {
                LOGGER.error("Error occured while updating League: {0}", ex.getMessage());
                Response response = leagueErrorMapper.toResponse(ex);
                asyncResponse.resume(response);
            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while updating League: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    /**
     * For example: DELETE /leagues/123
     *
     * @param leagueId
     * @param asyncResponse
     */
    @DELETE
    @Path("{id}")
    public void removeLeagueService(
            @Valid @NotNull(message = "League id must not be null") @PathParam("id") Long leagueId,
            @Suspended final AsyncResponse asyncResponse) {

        utService.configureTimeout(asyncResponse);

        utService.getManagedExecutorService().execute(new ThreadNameTrackingRunnable(() -> {
            try {
                leagueFacade.removeLeague(leagueId);

                Response response = Response.ok().build();
                asyncResponse.resume(response);
            } catch (LeagueNotFoundException ex) {
                LOGGER.error("Error occured while removing League: {0}", ex.getMessage());
                Response response = leagueErrorMapper.toResponse(ex);
                asyncResponse.resume(response);

            } catch (RuntimeException ex) {
                LOGGER.error("Unexpected error occured while removing League: {0}", ex.getMessage());
                Response response = Response.serverError().build();
                asyncResponse.resume(response);
            }
        }, actionName));
    }

    private JsonObject buildObjectFromArrayJson(JsonArray leagueArray, Long recordCount) {
        return Json.createObjectBuilder()
                .add("totalRecords", recordCount)
                .add("League", Json.createArrayBuilder(leagueArray)
                        .build())
                .build();
    }

    private URI getLeagueLocation(League league) {
        return uriInfo.getBaseUriBuilder()
                .path(LeagueResource.class)
                .path(LeagueResource.class, "getLeague")
                .build(league.getLeagueId());
    }

    private JsonObject buildLeagueJson(League league) {
        URI selfUri = getLeagueLocation(((League) league));

        return leagueVo.buildForLeague(league, selfUri);
    }

}
