package io.superbiz.video.model;

import javax.annotation.Generated;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(builderClassName = "Read", toBuilder = true)
@Generated("org.tomitribe.inget.model.ModelClassGenerator")
public class Movie {

    private String id;

    private String title;

    private String director;

    private String genre;

    private int year;

    private int rating;

    public CreateMovie.Create toCreate() {
        return CreateMovie.builder().director(this.director).genre(this.genre).rating(this.rating);
    }

    public static CreateMovie.Create create() {
        return CreateMovie.builder();
    }

    public UpdateMovie.Update toUpdate() {
        return UpdateMovie.builder().director(this.director).genre(this.genre).rating(this.rating);
    }

    public static UpdateMovie.Update update() {
        return UpdateMovie.builder();
    }

    public String toDelete() {
        return this.id;
    }
}
