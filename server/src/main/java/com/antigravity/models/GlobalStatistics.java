package com.antigravity.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

public class GlobalStatistics {

  @BsonId
  @JsonProperty("_id")
  private ObjectId id;

  @BsonProperty("race_entity_id")
  @JsonProperty("race_entity_id")
  private String raceEntityId;

  @BsonProperty("total_races")
  @JsonProperty("total_races")
  private int totalRaces;

  @BsonProperty("total_laps")
  @JsonProperty("total_laps")
  private int totalLaps;

  @BsonProperty("total_race_time_ms")
  @JsonProperty("total_race_time_ms")
  private long totalRaceTimeMs;

  @BsonProperty("fastest_lap_time")
  @JsonProperty("fastest_lap_time")
  private double fastestLapTime;

  @BsonProperty("fastest_lap_driver_name")
  @JsonProperty("fastest_lap_driver_name")
  private String fastestLapDriverName;

  @BsonProperty("fastest_lap_track_name")
  @JsonProperty("fastest_lap_track_name")
  private String fastestLapTrackName;

  @BsonProperty("fastest_lap_driver_nickname")
  @JsonProperty("fastest_lap_driver_nickname")
  private String fastestLapDriverNickname;

  @BsonProperty("fastest_lap_date")
  @JsonProperty("fastest_lap_date")
  private long fastestLapDate;

  @BsonProperty("highest_lap_count")
  @JsonProperty("highest_lap_count")
  private double highestLapCount;

  @BsonProperty("highest_lap_count_holder_name")
  @JsonProperty("highest_lap_count_holder_name")
  private String highestLapCountHolderName;

  @BsonProperty("highest_lap_count_track_name")
  @JsonProperty("highest_lap_count_track_name")
  private String highestLapCountTrackName;

  @BsonProperty("highest_lap_count_holder_nickname")
  @JsonProperty("highest_lap_count_holder_nickname")
  private String highestLapCountHolderNickname;

  @BsonProperty("highest_lap_count_date")
  @JsonProperty("highest_lap_count_date")
  private long highestLapCountDate;

  public GlobalStatistics() {
    this.fastestLapTime = Double.MAX_VALUE;
  }

  public GlobalStatistics(String raceEntityId) {
    this.raceEntityId = raceEntityId;
    this.fastestLapTime = Double.MAX_VALUE;
  }

  public GlobalStatistics(
      @BsonId @JsonProperty("_id") ObjectId id,
      @BsonProperty("race_entity_id") @JsonProperty("race_entity_id") String raceEntityId,
      @BsonProperty("total_races") @JsonProperty("total_races") int totalRaces,
      @BsonProperty("total_laps") @JsonProperty("total_laps") int totalLaps,
      @BsonProperty("total_race_time_ms") @JsonProperty("total_race_time_ms") long totalRaceTimeMs,
      @BsonProperty("fastest_lap_time") @JsonProperty("fastest_lap_time") double fastestLapTime,
      @BsonProperty("fastest_lap_driver_name") @JsonProperty("fastest_lap_driver_name")
          String fastestLapDriverName,
      @BsonProperty("fastest_lap_driver_nickname") @JsonProperty("fastest_lap_driver_nickname")
          String fastestLapDriverNickname,
      @BsonProperty("fastest_lap_track_name") @JsonProperty("fastest_lap_track_name")
          String fastestLapTrackName,
      @BsonProperty("fastest_lap_date") @JsonProperty("fastest_lap_date") long fastestLapDate,
      @BsonProperty("highest_lap_count") @JsonProperty("highest_lap_count") double highestLapCount,
      @BsonProperty("highest_lap_count_holder_name") @JsonProperty("highest_lap_count_holder_name")
          String highestLapCountHolderName,
      @BsonProperty("highest_lap_count_holder_nickname")
          @JsonProperty("highest_lap_count_holder_nickname")
          String highestLapCountHolderNickname,
      @BsonProperty("highest_lap_count_track_name") @JsonProperty("highest_lap_count_track_name")
          String highestLapCountTrackName,
      @BsonProperty("highest_lap_count_date") @JsonProperty("highest_lap_count_date")
          long highestLapCountDate) {
    this.id = id;
    this.raceEntityId = raceEntityId;
    this.totalRaces = totalRaces;
    this.totalLaps = totalLaps;
    this.totalRaceTimeMs = totalRaceTimeMs;
    this.fastestLapTime = fastestLapTime;
    this.fastestLapDriverName = fastestLapDriverName;
    this.fastestLapDriverNickname = fastestLapDriverNickname;
    this.fastestLapTrackName = fastestLapTrackName;
    this.fastestLapDate = fastestLapDate;
    this.highestLapCount = highestLapCount;
    this.highestLapCountHolderName = highestLapCountHolderName;
    this.highestLapCountHolderNickname = highestLapCountHolderNickname;
    this.highestLapCountTrackName = highestLapCountTrackName;
    this.highestLapCountDate = highestLapCountDate;
  }

  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public String getRaceEntityId() {
    return raceEntityId;
  }

  public void setRaceEntityId(String raceEntityId) {
    this.raceEntityId = raceEntityId;
  }

  public int getTotalRaces() {
    return totalRaces;
  }

  public void setTotalRaces(int totalRaces) {
    this.totalRaces = totalRaces;
  }

  public void addRaceCount() {
    this.totalRaces++;
  }

  public int getTotalLaps() {
    return totalLaps;
  }

  public void setTotalLaps(int totalLaps) {
    this.totalLaps = totalLaps;
  }

  public void addLaps(int laps) {
    this.totalLaps += laps;
  }

  public long getTotalRaceTimeMs() {
    return totalRaceTimeMs;
  }

  public void setTotalRaceTimeMs(long totalRaceTimeMs) {
    this.totalRaceTimeMs = totalRaceTimeMs;
  }

  public void addRaceTimeMs(long ms) {
    this.totalRaceTimeMs += ms;
  }

  public double getFastestLapTime() {
    return fastestLapTime;
  }

  public void setFastestLapTime(double fastestLapTime) {
    this.fastestLapTime = fastestLapTime;
  }

  public String getFastestLapDriverName() {
    return fastestLapDriverName;
  }

  public void setFastestLapDriverName(String fastestLapDriverName) {
    this.fastestLapDriverName = fastestLapDriverName;
  }

  public String getFastestLapTrackName() {
    return fastestLapTrackName;
  }

  public void setFastestLapTrackName(String fastestLapTrackName) {
    this.fastestLapTrackName = fastestLapTrackName;
  }

  public long getFastestLapDate() {
    return fastestLapDate;
  }

  public void setFastestLapDate(long fastestLapDate) {
    this.fastestLapDate = fastestLapDate;
  }

  public double getHighestLapCount() {
    return highestLapCount;
  }

  public void setHighestLapCount(double highestLapCount) {
    this.highestLapCount = highestLapCount;
  }

  public String getHighestLapCountHolderName() {
    return highestLapCountHolderName;
  }

  public void setHighestLapCountHolderName(String highestLapCountHolderName) {
    this.highestLapCountHolderName = highestLapCountHolderName;
  }

  public String getHighestLapCountTrackName() {
    return highestLapCountTrackName;
  }

  public void setHighestLapCountTrackName(String highestLapCountTrackName) {
    this.highestLapCountTrackName = highestLapCountTrackName;
  }

  public long getHighestLapCountDate() {
    return highestLapCountDate;
  }

  public void setHighestLapCountDate(long highestLapCountDate) {
    this.highestLapCountDate = highestLapCountDate;
  }

  public String getFastestLapDriverNickname() {
    return fastestLapDriverNickname;
  }

  public void setFastestLapDriverNickname(String fastestLapDriverNickname) {
    this.fastestLapDriverNickname = fastestLapDriverNickname;
  }

  public String getHighestLapCountHolderNickname() {
    return highestLapCountHolderNickname;
  }

  public void setHighestLapCountHolderNickname(String highestLapCountHolderNickname) {
    this.highestLapCountHolderNickname = highestLapCountHolderNickname;
  }
}
