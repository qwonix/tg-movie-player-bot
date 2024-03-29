package ru.qwonix.tgMoviePlayerBot.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@ToString
@Builder(toBuilder = true)
@Getter
@Setter
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
    private String previewTgFileId;
    private Season season;

    private List<Video> videos;
}
