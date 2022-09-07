package ru.qwonix.tgMoviePlayerBot.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;
import java.time.LocalDate;

@ToString
@Builder(toBuilder = true)
@Getter
public class Episode {
    private int id;
    private int number;
    private int productionCode;
    private String title;
    private String description;
    private LocalDate releaseDate;
    private String language;
    private String country;
    private Duration duration;
    private Season season;
    private String videoFileId;
    private String previewFileId;
}
