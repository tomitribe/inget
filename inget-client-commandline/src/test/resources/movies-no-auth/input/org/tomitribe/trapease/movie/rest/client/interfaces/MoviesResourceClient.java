package org.tomitribe.trapease.movie.rest.client.interfaces;

import javax.annotation.Generated;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("movies")
@Produces({
        "application/json" })
@Generated("org.tomitribe.client.ClientGenerator")
public interface MoviesResourceClient {

    @GET
    @Path("{id}")
    public org.tomitribe.trapease.movie.model.Movie find(
            @PathParam("id") Long id);

    @GET
    public List<org.tomitribe.trapease.movie.model.Movie> getMovies();

    @POST
    @Consumes("application/json")
    public org.tomitribe.trapease.movie.model.Movie addMovie(
            org.tomitribe.trapease.movie.model.Movie movie);

    @DELETE
    @Path("{id}")
    public void deleteMovie(
            @PathParam("id") long id);

    @PUT
    @Path("{id}")
    @Consumes("application/json")
    public org.tomitribe.trapease.movie.model.Movie updateMovie(
            @PathParam("id") long id,

            org.tomitribe.trapease.movie.model.Movie movie);

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public int count(
            @QueryParam("field") String field,

            @QueryParam("searchTerm") String searchTerm);
}
