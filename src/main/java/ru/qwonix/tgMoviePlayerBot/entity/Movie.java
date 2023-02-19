package ru.qwonix.tgMoviePlayerBot.entity;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@ToString
@Builder(toBuilder = true)
@Data
public class Movie {
    private int id;
    private String title;
    private String description;
    private String previewTgFileId;
    private Video video;
    private Show show;
}
