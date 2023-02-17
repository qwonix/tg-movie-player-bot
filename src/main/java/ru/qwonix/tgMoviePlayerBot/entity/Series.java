package ru.qwonix.tgMoviePlayerBot.entity;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;


@ToString
@Builder(toBuilder = true)
@Data
public class Series {
    private int id;
    private String title;
    private String description;
    private String country;
    private String previewTgFileId;
    private Show show;

    private LocalDate premiereReleaseDate;

    public String getPremiereReleaseYearOrTBA() {
        String seriesPremiereReleaseDate;

        if (this.getPremiereReleaseDate() == null) {
            seriesPremiereReleaseDate = "TBA";
        } else {
            seriesPremiereReleaseDate = String.valueOf(this.getPremiereReleaseDate().getYear());
        }
        return seriesPremiereReleaseDate;
    }
}
