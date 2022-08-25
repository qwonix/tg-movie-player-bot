package ru.qwonix.tgMoviePlayerBot.entity;


import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@ToString
@Builder(toBuilder = true)
@Data
public class Season {
    private int id;
    private int number;
    private String description;
    private LocalDate premiereReleaseDate;

    public String getFormattedPremiereReleaseDate() {
        String seriesPremiereReleaseDate;

        if (this.getPremiereReleaseDate() == null) {
            seriesPremiereReleaseDate = "TBA";
        } else {
            seriesPremiereReleaseDate = this.getPremiereReleaseDate()
                    .format(DateTimeFormatter.ofPattern("d MMMM y", Locale.forLanguageTag("ru")));
        }
        return seriesPremiereReleaseDate;
    }

    public String getFormattedFinalReleaseDate() {
        String seriesFinalReleaseDate;
        if (this.getFinalReleaseDate() == null) {
            seriesFinalReleaseDate = "TBA";
        } else {
            seriesFinalReleaseDate = this.getFinalReleaseDate()
                    .format(DateTimeFormatter.ofPattern("d MMMM y", Locale.forLanguageTag("ru")));
        }
        return seriesFinalReleaseDate;
    }

    private LocalDate finalReleaseDate;
    private int totalEpisodesCount;
    private Series series;
    private String previewFileId;
}
