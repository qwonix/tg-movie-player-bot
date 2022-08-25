package ru.qwonix.tgMoviePlayerBot.entity;


import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;

@ToString
@Builder(toBuilder = true)
@Data
public class Season {
    private int id;
    private int number;
    private String description;
    private LocalDate premiereReleaseDate;
    private LocalDate finalReleaseDate;
    private int totalEpisodesCount;
    private Series series;
    private String previewFileId;
}
