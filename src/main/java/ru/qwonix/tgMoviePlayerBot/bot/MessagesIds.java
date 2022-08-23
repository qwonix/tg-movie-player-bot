package ru.qwonix.tgMoviePlayerBot.bot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONObject;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MessagesIds {
    private Integer episodeMessageId;
    private Integer seasonMessageId;
    private Integer seriesMessageId;
    private Integer videoMessageId;

    public static MessagesIds fromJson(String json) {
        JSONObject jsonObject = new JSONObject(json);

        MessagesIds MessagesIds = new MessagesIds();

        Integer episodeMessageId = jsonObject.optInt("episodeMessageId");
        if (episodeMessageId == 0) episodeMessageId = null;
        MessagesIds.setEpisodeMessageId(episodeMessageId);

        Integer seasonMessageId = jsonObject.optInt("seasonMessageId");
        if (seasonMessageId == 0) seasonMessageId = null;
        MessagesIds.setSeasonMessageId(seasonMessageId);

        Integer seriesMessageId = jsonObject.optInt("seriesMessageId");
        if (seriesMessageId == 0) seriesMessageId = null;
        MessagesIds.setSeriesMessageId(seriesMessageId);

        Integer videoMessageId = jsonObject.optInt("videoMessageId");
        if (videoMessageId == 0) videoMessageId = null;
        MessagesIds.setVideoMessageId(videoMessageId);

        return MessagesIds;
    }

    public JSONObject toJson() {
        JSONObject jsonData = new JSONObject();
        jsonData.put("videoMessageId", videoMessageId);
        jsonData.put("episodeMessageId", episodeMessageId);
        jsonData.put("seasonMessageId", seasonMessageId);
        jsonData.put("seriesMessageId", seriesMessageId);

        return jsonData;
    }

    public boolean hasEpisodeMessageId() {
        return episodeMessageId != null;
    }

    public boolean hasSeasonMessageId() {
        return seasonMessageId != null;
    }

    public boolean hasSeriesMessageId() {
        return seriesMessageId != null;
    }

    public boolean hasVideoMessageId() {
        return videoMessageId != null;
    }

    public void reset() {
        this.setVideoMessageId(null);
        this.setEpisodeMessageId(null);
        this.setSeasonMessageId(null);
        this.setSeriesMessageId(null);
    }
}
