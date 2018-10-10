package io.superbiz.video.rest.client.interfaces;

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
    public io.superbiz.video.model.Movie find(
            @PathParam("id") Long id);

    @GET
    public List<io.superbiz.video.model.Movie> getMovies();

    @POST
    @Consumes("application/json")
    public io.superbiz.video.model.Movie addMovie(
            io.superbiz.video.model.Movie movie);

    @DELETE
    @Path("{id}")
    public void deleteMovie(
            @PathParam("id") long id);

    @PUT
    @Path("{id}")
    @Consumes("application/json")
    public io.superbiz.video.model.Movie updateMovie(
            @PathParam("id") long id,

            io.superbiz.video.model.Movie movie);

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public int count(
            @QueryParam("field") String field,

            @QueryParam("searchTerm") String searchTerm);
}
