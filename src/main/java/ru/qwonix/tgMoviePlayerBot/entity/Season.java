package ru.qwonix.tgMoviePlayerBot.entity;


import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder(toBuilder = true)
@Data
public class Season {
    @EqualsAndHashCode.Include
    private int id;
    private int number;
    private String description;
    private LocalDate premiereReleaseDate;
    private LocalDate finalReleaseDate;

    private Series series;
}
