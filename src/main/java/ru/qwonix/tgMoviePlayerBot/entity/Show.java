package ru.qwonix.tgMoviePlayerBot.entity;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@ToString
@Builder(toBuilder = true)
@Data
public class Show {
    private int id;
    private String description;
    private String title;
    private String previewTgFileId;
}
