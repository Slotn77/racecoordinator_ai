package com.antigravity.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
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

  @BsonProperty("fastest_lap_team_name")
  @JsonProperty("fastest_lap_team_name")
  private String fastestLapTeamName;

  @BsonProperty("highest_score")
  @JsonProperty("highest_score")
  private double highestScore;

  @BsonProperty("highest_score_holder_name")
  @JsonProperty("highest_score_holder_name")
  private String highestScoreHolderName;

  @BsonProperty("highest_score_track_name")
  @JsonProperty("highest_score_track_name")
  private String highestScoreTrackName;

  @BsonProperty("highest_score_holder_nickname")
  @JsonProperty("highest_score_holder_nickname")
  private String highestScoreHolderNickname;

  @BsonProperty("highest_score_date")
  @JsonProperty("highest_score_date")
  private long highestScoreDate;

  @BsonProperty("highest_score_team_name")
  @JsonProperty("highest_score_team_name")
  private String highestScoreTeamName;

  @BsonProperty("lane_fastest_lap_times")
  @JsonProperty("lane_fastest_lap_times")
  private List<Double> laneFastestLapTimes;

  @BsonProperty("lane_fastest_lap_driver_names")
  @JsonProperty("lane_fastest_lap_driver_names")
  private List<String> laneFastestLapDriverNames;

  @BsonProperty("lane_fastest_lap_driver_nicknames")
  @JsonProperty("lane_fastest_lap_driver_nicknames")
  private List<String> laneFastestLapDriverNicknames;

  @BsonProperty("lane_fastest_lap_dates")
  @JsonProperty("lane_fastest_lap_dates")
  private List<Long> laneFastestLapDates;

  @BsonProperty("lane_fastest_lap_team_names")
  @JsonProperty("lane_fastest_lap_team_names")
  private List<String> laneFastestLapTeamNames;

  @BsonProperty("lane_highest_scores")
  @JsonProperty("lane_highest_scores")
  private List<Double> laneHighestScores;

  @BsonProperty("lane_highest_score_holder_names")
  @JsonProperty("lane_highest_score_holder_names")
  private List<String> laneHighestScoreHolderNames;

  @BsonProperty("lane_highest_score_holder_nicknames")
  @JsonProperty("lane_highest_score_holder_nicknames")
  private List<String> laneHighestScoreHolderNicknames;

  @BsonProperty("lane_highest_score_dates")
  @JsonProperty("lane_highest_score_dates")
  private List<Long> laneHighestScoreDates;

  @BsonProperty("lane_highest_score_team_names")
  @JsonProperty("lane_highest_score_team_names")
  private List<String> laneHighestScoreTeamNames;

  public GlobalStatistics() {
    this.fastestLapTime = Double.MAX_VALUE;
    initLaneLists();
  }

  public GlobalStatistics(String raceEntityId) {
    this.raceEntityId = raceEntityId;
    this.fastestLapTime = Double.MAX_VALUE;
    initLaneLists();
  }

  private void initLaneLists() {
    this.laneFastestLapTimes = new ArrayList<>();
    this.laneFastestLapDriverNames = new ArrayList<>();
    this.laneFastestLapDriverNicknames = new ArrayList<>();
    this.laneFastestLapDates = new ArrayList<>();
    this.laneHighestScores = new ArrayList<>();
    this.laneHighestScoreHolderNames = new ArrayList<>();
    this.laneHighestScoreHolderNicknames = new ArrayList<>();
    this.laneHighestScoreDates = new ArrayList<>();
    this.laneFastestLapTeamNames = new ArrayList<>();
    this.laneHighestScoreTeamNames = new ArrayList<>();
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
      @BsonProperty("highest_score") @JsonProperty("highest_score") double highestScore,
      @BsonProperty("highest_score_holder_name") @JsonProperty("highest_score_holder_name")
          String highestScoreHolderName,
      @BsonProperty("highest_score_holder_nickname") @JsonProperty("highest_score_holder_nickname")
          String highestScoreHolderNickname,
      @BsonProperty("highest_score_track_name") @JsonProperty("highest_score_track_name")
          String highestScoreTrackName,
      @BsonProperty("highest_score_date") @JsonProperty("highest_score_date") long highestScoreDate,
      @BsonProperty("lane_fastest_lap_times") @JsonProperty("lane_fastest_lap_times")
          List<Double> laneFastestLapTimes,
      @BsonProperty("lane_fastest_lap_driver_names") @JsonProperty("lane_fastest_lap_driver_names")
          List<String> laneFastestLapDriverNames,
      @BsonProperty("lane_fastest_lap_driver_nicknames")
          @JsonProperty("lane_fastest_lap_driver_nicknames")
          List<String> laneFastestLapDriverNicknames,
      @BsonProperty("lane_fastest_lap_dates") @JsonProperty("lane_fastest_lap_dates")
          List<Long> laneFastestLapDates,
      @BsonProperty("lane_highest_scores") @JsonProperty("lane_highest_scores")
          List<Double> laneHighestScores,
      @BsonProperty("lane_highest_score_holder_names")
          @JsonProperty("lane_highest_score_holder_names")
          List<String> laneHighestScoreHolderNames,
      @BsonProperty("lane_highest_score_holder_nicknames")
          @JsonProperty("lane_highest_score_holder_nicknames")
          List<String> laneHighestScoreHolderNicknames,
      @BsonProperty("lane_highest_score_dates") @JsonProperty("lane_highest_score_dates")
          List<Long> laneHighestScoreDates,
      @BsonProperty("fastest_lap_team_name") @JsonProperty("fastest_lap_team_name")
          String fastestLapTeamName,
      @BsonProperty("highest_score_team_name") @JsonProperty("highest_score_team_name")
          String highestScoreTeamName,
      @BsonProperty("lane_fastest_lap_team_names") @JsonProperty("lane_fastest_lap_team_names")
          List<String> laneFastestLapTeamNames,
      @BsonProperty("lane_highest_score_team_names") @JsonProperty("lane_highest_score_team_names")
          List<String> laneHighestScoreTeamNames) {
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
    this.highestScore = highestScore;
    this.highestScoreHolderName = highestScoreHolderName;
    this.highestScoreHolderNickname = highestScoreHolderNickname;
    this.highestScoreTrackName = highestScoreTrackName;
    this.highestScoreDate = highestScoreDate;
    this.laneFastestLapTimes =
        laneFastestLapTimes != null ? laneFastestLapTimes : new ArrayList<>();
    this.laneFastestLapDriverNames =
        laneFastestLapDriverNames != null ? laneFastestLapDriverNames : new ArrayList<>();
    this.laneFastestLapDriverNicknames =
        laneFastestLapDriverNicknames != null ? laneFastestLapDriverNicknames : new ArrayList<>();
    this.laneFastestLapDates =
        laneFastestLapDates != null ? laneFastestLapDates : new ArrayList<>();
    this.laneHighestScores = laneHighestScores != null ? laneHighestScores : new ArrayList<>();
    this.laneHighestScoreHolderNames =
        laneHighestScoreHolderNames != null ? laneHighestScoreHolderNames : new ArrayList<>();
    this.laneHighestScoreHolderNicknames =
        laneHighestScoreHolderNicknames != null
            ? laneHighestScoreHolderNicknames
            : new ArrayList<>();
    this.laneHighestScoreDates =
        laneHighestScoreDates != null ? laneHighestScoreDates : new ArrayList<>();
    this.fastestLapTeamName = fastestLapTeamName;
    this.highestScoreTeamName = highestScoreTeamName;
    this.laneFastestLapTeamNames =
        laneFastestLapTeamNames != null ? laneFastestLapTeamNames : new ArrayList<>();
    this.laneHighestScoreTeamNames =
        laneHighestScoreTeamNames != null ? laneHighestScoreTeamNames : new ArrayList<>();
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

  public String getFastestLapTeamName() {
    return fastestLapTeamName;
  }

  public void setFastestLapTeamName(String fastestLapTeamName) {
    this.fastestLapTeamName = fastestLapTeamName;
  }

  public double getHighestScore() {
    return highestScore;
  }

  public void setHighestScore(double highestScore) {
    this.highestScore = highestScore;
  }

  public String getHighestScoreHolderName() {
    return highestScoreHolderName;
  }

  public void setHighestScoreHolderName(String highestScoreHolderName) {
    this.highestScoreHolderName = highestScoreHolderName;
  }

  public String getHighestScoreTrackName() {
    return highestScoreTrackName;
  }

  public void setHighestScoreTrackName(String highestScoreTrackName) {
    this.highestScoreTrackName = highestScoreTrackName;
  }

  public long getHighestScoreDate() {
    return highestScoreDate;
  }

  public void setHighestScoreDate(long highestScoreDate) {
    this.highestScoreDate = highestScoreDate;
  }

  public String getHighestScoreTeamName() {
    return highestScoreTeamName;
  }

  public void setHighestScoreTeamName(String highestScoreTeamName) {
    this.highestScoreTeamName = highestScoreTeamName;
  }

  public String getFastestLapDriverNickname() {
    return fastestLapDriverNickname;
  }

  public void setFastestLapDriverNickname(String fastestLapDriverNickname) {
    this.fastestLapDriverNickname = fastestLapDriverNickname;
  }

  public String getHighestScoreHolderNickname() {
    return highestScoreHolderNickname;
  }

  public void setHighestScoreHolderNickname(String highestScoreHolderNickname) {
    this.highestScoreHolderNickname = highestScoreHolderNickname;
  }

  public List<Double> getLaneFastestLapTimes() {
    return laneFastestLapTimes;
  }

  public void setLaneFastestLapTimes(List<Double> laneFastestLapTimes) {
    this.laneFastestLapTimes = laneFastestLapTimes;
  }

  public List<String> getLaneFastestLapDriverNames() {
    return laneFastestLapDriverNames;
  }

  public void setLaneFastestLapDriverNames(List<String> laneFastestLapDriverNames) {
    this.laneFastestLapDriverNames = laneFastestLapDriverNames;
  }

  public List<String> getLaneFastestLapDriverNicknames() {
    return laneFastestLapDriverNicknames;
  }

  public void setLaneFastestLapDriverNicknames(List<String> laneFastestLapDriverNicknames) {
    this.laneFastestLapDriverNicknames = laneFastestLapDriverNicknames;
  }

  public List<Long> getLaneFastestLapDates() {
    return laneFastestLapDates;
  }

  public void setLaneFastestLapDates(List<Long> laneFastestLapDates) {
    this.laneFastestLapDates = laneFastestLapDates;
  }

  public List<String> getLaneFastestLapTeamNames() {
    return laneFastestLapTeamNames;
  }

  public void setLaneFastestLapTeamNames(List<String> laneFastestLapTeamNames) {
    this.laneFastestLapTeamNames = laneFastestLapTeamNames;
  }

  public List<Double> getLaneHighestScores() {
    return laneHighestScores;
  }

  public void setLaneHighestScores(List<Double> laneHighestScores) {
    this.laneHighestScores = laneHighestScores;
  }

  public List<String> getLaneHighestScoreHolderNames() {
    return laneHighestScoreHolderNames;
  }

  public void setLaneHighestScoreHolderNames(List<String> laneHighestScoreHolderNames) {
    this.laneHighestScoreHolderNames = laneHighestScoreHolderNames;
  }

  public List<String> getLaneHighestScoreHolderNicknames() {
    return laneHighestScoreHolderNicknames;
  }

  public void setLaneHighestScoreHolderNicknames(List<String> laneHighestScoreHolderNicknames) {
    this.laneHighestScoreHolderNicknames = laneHighestScoreHolderNicknames;
  }

  public List<Long> getLaneHighestScoreDates() {
    return laneHighestScoreDates;
  }

  public void setLaneHighestScoreDates(List<Long> laneHighestScoreDates) {
    this.laneHighestScoreDates = laneHighestScoreDates;
  }

  public List<String> getLaneHighestScoreTeamNames() {
    return laneHighestScoreTeamNames;
  }

  public void setLaneHighestScoreTeamNames(List<String> laneHighestScoreTeamNames) {
    this.laneHighestScoreTeamNames = laneHighestScoreTeamNames;
  }
}
