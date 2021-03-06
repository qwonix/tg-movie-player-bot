package ru.qwonix.tgMoviePlayerBot.entity;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;


@Builder(toBuilder = true)
@Data
public class Episode {

    private int id;
    private int number;
    private String name;
    private String description;
    private LocalDate releaseDate;

    private String language;
    private String country;

    private Duration duration;

    private Season season;

    private String fileId;
}
