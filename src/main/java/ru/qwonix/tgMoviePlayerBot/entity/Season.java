package ru.qwonix.tgMoviePlayerBot.entity;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;


@Builder(toBuilder = true)
@Data
public class Season {
    private int id;
    private int number;
    private String description;
    private LocalDate premiereReleaseDate;
    private LocalDate finalReleaseDate;
    private Series series;
}
