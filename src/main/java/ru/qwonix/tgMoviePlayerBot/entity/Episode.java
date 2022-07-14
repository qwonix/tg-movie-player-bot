package ru.qwonix.tgMoviePlayerBot.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Duration;
import java.time.LocalDate;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder(toBuilder = true)
@Data
public class Episode {
    @EqualsAndHashCode.Include
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
