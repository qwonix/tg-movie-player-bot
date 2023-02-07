package ru.qwonix.tgMoviePlayerBot.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Builder(toBuilder = true)
@Getter
@Setter
public class Video {
    private int id;
    private Integer resolution;
    private String audioLanguage;
    private String subtitlesLanguage;
    private String videoFileId;
    private int priority;

    private boolean hasSubtitles = subtitlesLanguage == null;
}
