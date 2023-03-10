package ru.qwonix.tgMoviePlayerBot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.json.JSONObject;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class MessagesIds {
    private Integer seriesMessageId;
    private Integer seasonMessageId;
    private Integer episodeMessageId;
    private Integer videoMessageId;

    public static MessagesIds fromJson(String json) {
        JSONObject jsonObject = new JSONObject(json);
        MessagesIds MessagesIds = new MessagesIds();

        Integer seriesMessageId = jsonObject.optInt("seriesMessageId");
        if (seriesMessageId == 0) seriesMessageId = null;
        MessagesIds.setSeriesMessageId(seriesMessageId);

        Integer seasonMessageId = jsonObject.optInt("seasonMessageId");
        if (seasonMessageId == 0) seasonMessageId = null;
        MessagesIds.setSeasonMessageId(seasonMessageId);

        Integer episodeMessageId = jsonObject.optInt("episodeMessageId");
        if (episodeMessageId == 0) episodeMessageId = null;
        MessagesIds.setEpisodeMessageId(episodeMessageId);

        Integer videoMessageId = jsonObject.optInt("videoMessageId");
        if (videoMessageId == 0) videoMessageId = null;
        MessagesIds.setVideoMessageId(videoMessageId);

        return MessagesIds;
    }

    public JSONObject toJson() {
        JSONObject jsonData = new JSONObject();
        jsonData.put("seriesMessageId", seriesMessageId);
        jsonData.put("seasonMessageId", seasonMessageId);
        jsonData.put("episodeMessageId", episodeMessageId);
        jsonData.put("videoMessageId", videoMessageId);

        return jsonData;
    }

    public boolean hasSeriesMessageId() {
        return seriesMessageId != null;
    }

    public boolean hasSeasonMessageId() {
        return seasonMessageId != null;
    }

    public boolean hasEpisodeMessageId() {
        return episodeMessageId != null;
    }

    public boolean hasVideoMessageId() {
        return videoMessageId != null;
    }

    public void reset() {
        this.setSeriesMessageId(null);
        this.setSeasonMessageId(null);
        this.setEpisodeMessageId(null);
        this.setVideoMessageId(null);
    }
}
