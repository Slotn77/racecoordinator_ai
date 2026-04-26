package com.antigravity.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

public class AudioConfig {

  private final String type;
  private final String url;
  private final String text;

  @BsonCreator
  public AudioConfig(
      @BsonProperty("type") @JsonProperty("type") String type,
      @BsonProperty("url") @JsonProperty("url") String url,
      @BsonProperty("text") @JsonProperty("text") String text) {
    this.type = type != null ? type : "preset";
    this.url = url;
    this.text = text;
  }

  public AudioConfig() {
    this("preset", null, null);
  }

  public String getType() {
    return type;
  }

  public String getUrl() {
    return url;
  }

  public String getText() {
    return text;
  }
}
