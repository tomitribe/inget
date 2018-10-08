/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,


 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.superbiz.video.dao;

import javax.annotation.Generated;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(builderClassName = "Read", toBuilder = true)
@Generated("org.tomitribe.model.ModelClassGenerator")
public class Movie {

    private String id;

    private String title;

    private String director;

    private String genre;

    private int year;

    private int rating;

    private Credential credential;

    public CreateMovie.Create toCreate() {
        return CreateMovie.builder().title(this.title).director(this.director).genre(this.genre).year(this.year)
                .rating(this.rating);
    }

    public static CreateMovie.Create create() {
        return CreateMovie.builder();
    }

    public UpdateMovie.Update toUpdate() {
        return UpdateMovie.builder().title(this.title).director(this.director).genre(this.genre).year(this.year)
                .rating(this.rating);
    }

    public static UpdateMovie.Update update() {
        return UpdateMovie.builder();
    }

    public String toDelete() {
        return this.id;
    }
}
