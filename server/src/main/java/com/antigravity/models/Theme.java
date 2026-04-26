package com.antigravity.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

/**
 * A theme groups together visual and audio asset assignments into logical "slots." Each slot maps a
 * purpose (e.g., "flag.green", "lamp.red.on") to an asset entity ID in the assets collection.
 *
 * <p>The "RaceCoordinator AI (default)" default theme is created on first run and cannot be
 * deleted. Users can create, copy, edit, and delete custom themes.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Theme extends Model {

  public static final String DEFAULT_THEME_ID = "default_classic_rc_ai";

  private final String name;
  private final boolean isDefault;
  private final Map<String, String> slots;
  private final Map<String, AudioConfig> audioSlots;

  @BsonCreator
  public Theme(
      @BsonProperty("name") @JsonProperty("name") String name,
      @BsonProperty("is_default") @JsonProperty("is_default") boolean isDefault,
      @BsonProperty("slots") @JsonProperty("slots") Map<String, String> slots,
      @BsonProperty("audio_slots") @JsonProperty("audio_slots") Map<String, AudioConfig> audioSlots,
      @BsonProperty("entity_id") @JsonProperty("entity_id") String entityId,
      @BsonId @BsonProperty("_id") @JsonProperty("_id") ObjectId id) {
    super(id, entityId);
    this.name = name;
    this.isDefault = isDefault;
    this.slots = slots != null ? slots : new HashMap<>();
    this.audioSlots = audioSlots != null ? audioSlots : new HashMap<>();
  }

  public String getName() {
    return name;
  }

  @BsonProperty("is_default")
  @JsonProperty("is_default")
  public boolean isDefault() {
    return isDefault;
  }

  public Map<String, String> getSlots() {
    return slots;
  }

  @BsonProperty("audio_slots")
  @JsonProperty("audio_slots")
  public Map<String, AudioConfig> getAudioSlots() {
    return audioSlots;
  }
}
