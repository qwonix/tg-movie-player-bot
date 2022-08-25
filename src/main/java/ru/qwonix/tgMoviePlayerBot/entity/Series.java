package ru.qwonix.tgMoviePlayerBot.entity;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;


@ToString
@Builder(toBuilder = true)
@Data
public class Series {
    private int id;
    private String name;
    private String description;
    private String country;
    private String previewFileId;
}
