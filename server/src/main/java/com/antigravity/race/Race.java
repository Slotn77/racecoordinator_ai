package com.antigravity.race;

import com.antigravity.context.DatabaseContext;
import com.antigravity.converters.HeatConverter;
import com.antigravity.converters.RaceConverter;
import com.antigravity.converters.RaceParticipantConverter;
import com.antigravity.models.AnalogFuelOptions;
import com.antigravity.models.Driver;
import com.antigravity.models.FuelOptions;
import com.antigravity.models.GlobalStatistics;
import com.antigravity.models.Track;
import com.antigravity.proto.CallbuttonEvent;
import com.antigravity.proto.InterfaceEvent;
import com.antigravity.proto.InterfaceStatus;
import com.antigravity.proto.InterfaceStatusEvent;
import com.antigravity.proto.OverallStandingsUpdate;
import com.antigravity.proto.RaceData;
import com.antigravity.proto.RaceFlag;
import com.antigravity.proto.RaceState;
import com.antigravity.proto.RaceTime;
import com.antigravity.proto.RecordData;
import com.antigravity.proto.RecordEntry;
import com.antigravity.protocols.CarData;
import com.antigravity.protocols.ProtocolDelegate;
import com.antigravity.protocols.ProtocolListener;
import com.antigravity.protocols.arduino.ArduinoConfig;
import com.antigravity.protocols.arduino.ArduinoProtocol;
import com.antigravity.protocols.demo.Demo;
import com.antigravity.race.states.HeatOver;
import com.antigravity.race.states.IRaceState;
import com.antigravity.race.states.NotStarted;
import com.antigravity.race.states.Paused;
import com.antigravity.race.states.RaceOver;
import com.antigravity.race.states.Racing;
import com.antigravity.race.states.Starting;
import com.antigravity.service.DatabaseService;
import com.google.protobuf.GeneratedMessageV3;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Race implements ProtocolListener {

  // Data based on the race model configuration
  private final com.antigravity.models.Race model;
  private final Track track;
  private final List<RaceParticipant> drivers;
  private List<Heat> heats;
  private Heat currentHeat;
  private final OverallStandings overallStandings;

  public List<RaceParticipant> getDrivers() {
    return drivers;
  }

  private ProtocolDelegate protocols;
  private boolean isDemoMode;
  private DatabaseContext databaseContext;

  // Dynamic race data
  private IRaceState state;
  private float accumulatedRaceTime = 0.0f;
  private boolean hasRacedInCurrentHeat = false;
  private boolean autoStartFired = false;
  private boolean autoAdvanceFired = false;
  private double autoStartRemaining = 0;
  private double autoAdvanceRemaining = 0;
  private boolean mainPower = false;

  // Heat execution state
  private HeatExecutionManager executionManager;
  private RaceStatistics statistics;

  // Record tracking
  private double overallBestLap = Double.MAX_VALUE;
  private String overallBestLapHolder = "";
  private String overallBestLapHolderNickname = "";
  private long overallBestLapDate = 0;

  private double overallBestScore = 0;
  private String overallBestScoreHolder = "";
  private String overallBestScoreHolderNickname = "";
  private long overallBestScoreDate = 0;

  private double raceBestLap = Double.MAX_VALUE;
  private String raceBestLapHolder = "";
  private String raceBestLapHolderNickname = "";

  private double heatBestLap = Double.MAX_VALUE;
  private String heatBestLapHolder = "";
  private String heatBestLapHolderNickname = "";

  private Race(Builder builder) {
    this.model = builder.model;
    this.track = builder.track;
    this.drivers = builder.drivers;
    this.databaseContext = builder.databaseContext;

    // If not a restored race, ensure drivers are populated correctly and heats are
    // built
    if (builder.heats == null) {
      for (int i = 0; i < this.drivers.size(); i++) {
        this.drivers.get(i).setSeed(i + 1);
      }
      int numLanes = this.track.getLanes().size();
      while (this.drivers.size() < numLanes) {
        this.drivers.add(new RaceParticipant(Driver.EMPTY_DRIVER));
      }
      this.heats = HeatBuilder.buildHeats(this, this.drivers);
      this.currentHeat = this.heats.get(0);
      resetHeatRecords();
    } else {
      this.heats = builder.heats;
      if (builder.currentHeatIndex >= 0 && builder.currentHeatIndex < this.heats.size()) {
        this.currentHeat = this.heats.get(builder.currentHeatIndex);
      } else if (!this.heats.isEmpty()) {
        this.currentHeat = this.heats.get(0);
      }
    }

    this.accumulatedRaceTime = builder.accumulatedRaceTime;
    this.hasRacedInCurrentHeat = builder.hasRacedInCurrentHeat;
    this.autoStartFired = builder.autoStartFired;
    this.autoAdvanceFired = builder.autoAdvanceFired;
    this.autoAdvanceRemaining = 0;
    this.autoStartRemaining = 0;
    this.statistics = builder.statistics != null ? builder.statistics : new RaceStatistics();

    this.overallStandings = new OverallStandings(model.getHeatScoring(), model.getOverallScoring());
    this.createProtocols(builder.isDemoMode);

    this.executionManager = new HeatExecutionManager(this);
    initializeHeatExecutionState();

    if (builder.stateClassName != null) {
      try {
        Class<?> clazz = Class.forName(builder.stateClassName);
        this.state = (IRaceState) clazz.getDeclaredConstructor().newInstance();
      } catch (Exception e) {
        System.err.println(
            "Failed to restore race state: "
                + builder.stateClassName
                + ", falling back to NotStarted");
        this.state = new NotStarted();
      }
    } else {
      this.state = new NotStarted();
    }

    this.state.enter(this);

    if (builder.heats == null) {
      initializeFuelLevels();
    }

    loadGlobalRecords();
    broadcastRecords();

    // Ensure initial ranks are calculated and broadcasted immediately
    updateAndBroadcastOverallStandings();
  }

  private void loadGlobalRecords() {
    if (databaseContext == null) {
      System.err.println("DatabaseContext is missing - skipping global record loading.");
      return;
    }

    try {
      DatabaseService dbService = DatabaseService.getInstance();
      GlobalStatistics stats =
          dbService.getGlobalStatistics(
              databaseContext.getDatabase(), getRaceModel().getEntityId());
      if (stats != null) {
        this.overallBestLap = stats.getFastestLapTime();
        this.overallBestLapHolder = stats.getFastestLapDriverName();
        this.overallBestLapHolderNickname = stats.getFastestLapDriverNickname();
        this.overallBestLapDate = stats.getFastestLapDate();
        this.overallBestScore = stats.getHighestLapCount();
        this.overallBestScoreHolder = stats.getHighestLapCountHolderName();
        this.overallBestScoreHolderNickname = stats.getHighestLapCountHolderNickname();
        this.overallBestScoreDate = stats.getHighestLapCountDate();

        System.out.println(
            "Global records loaded: bestLap="
                + this.overallBestLap
                + ", bestScore="
                + this.overallBestScore
                + " for "
                + this.overallBestScoreHolderNickname);
      } else {
        System.out.println("No global records found in database.");
      }
    } catch (Exception e) {
      System.err.println("Failed to load global records: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public static class Builder {
    private com.antigravity.models.Race model;
    private List<RaceParticipant> drivers;
    private Track track;
    private boolean isDemoMode = false;
    private DatabaseContext databaseContext;
    private List<Heat> heats;
    private int currentHeatIndex = 0;
    private float accumulatedRaceTime = 0f;
    private boolean hasRacedInCurrentHeat = false;
    private boolean autoStartFired = false;
    private boolean autoAdvanceFired = false;
    private String stateClassName = null;
    private RaceStatistics statistics;

    public Builder model(com.antigravity.models.Race model) {
      this.model = model;
      return this;
    }

    public Builder drivers(List<RaceParticipant> drivers) {
      this.drivers = drivers;
      return this;
    }

    public Builder track(Track track) {
      this.track = track;
      return this;
    }

    public Builder isDemoMode(boolean isDemoMode) {
      this.isDemoMode = isDemoMode;
      return this;
    }

    public Builder databaseContext(DatabaseContext databaseContext) {
      this.databaseContext = databaseContext;
      return this;
    }

    public Builder heats(List<Heat> heats) {
      this.heats = heats;
      return this;
    }

    public Builder currentHeatIndex(int currentHeatIndex) {
      this.currentHeatIndex = currentHeatIndex;
      return this;
    }

    public Builder accumulatedRaceTime(float accumulatedRaceTime) {
      this.accumulatedRaceTime = accumulatedRaceTime;
      return this;
    }

    public Builder hasRacedInCurrentHeat(boolean hasRacedInCurrentHeat) {
      this.hasRacedInCurrentHeat = hasRacedInCurrentHeat;
      return this;
    }

    public Builder autoStartFired(boolean autoStartFired) {
      this.autoStartFired = autoStartFired;
      return this;
    }

    public Builder autoAdvanceFired(boolean autoAdvanceFired) {
      this.autoAdvanceFired = autoAdvanceFired;
      return this;
    }

    public Builder stateClassName(String stateClassName) {
      this.stateClassName = stateClassName;
      return this;
    }

    public Builder statistics(RaceStatistics statistics) {
      this.statistics = statistics;
      return this;
    }

    public Race build() {
      return new Race(this);
    }
  }

  public void init() {
    if (this.protocols != null) {
      this.protocols.open();
    }
  }

  public boolean isDemoMode() {
    return isDemoMode;
  }

  public FuelOptions getFuelOptions() {
    if (track != null && track.hasDigitalFuel()) {
      return model.getDigitalFuelOptions();
    }
    return model.getFuelOptions();
  }

  private void initializeFuelLevels() {
    FuelOptions fuelOptions = getFuelOptions();
    if (fuelOptions != null && fuelOptions.isEnabled()) {
      double initialLevel = (fuelOptions.getCapacity() * fuelOptions.getStartLevel()) / 100.0;
      for (RaceParticipant driver : drivers) {
        driver.setFuelLevel(initialLevel);
      }
    }
  }

  public void initializeHardwareState() {
    if (this.protocols == null) {
      return;
    }

    // 1. Race State and Flag
    this.protocols.setRaceState(
        getProtoState(state), state.getFlagType(this), getAutoStartRemaining());

    // 2. Heat Standings / Heat Leader
    if (this.currentHeat != null && this.currentHeat.getHeatStandings() != null) {
      List<String> standingsIds = currentHeat.getStandings();
      List<DriverHeatData> heatDrivers = currentHeat.getDrivers();
      List<Integer> rankings = new ArrayList<>();
      for (String id : standingsIds) {
        for (int i = 0; i < heatDrivers.size(); i++) {
          DriverHeatData dhd = heatDrivers.get(i);
          if (dhd.getObjectId().equals(id)) {
            // Only add if not an empty driver
            if (dhd.getActualDriver() != Driver.EMPTY_DRIVER) {
              rankings.add(i);
            }
            break;
          }
        }
      }
      this.protocols.setHeatStandings(rankings);
    }

    // 3. Fuel Levels
    FuelOptions fuelOptions = getFuelOptions();
    if (fuelOptions != null && fuelOptions.isEnabled() && fuelOptions.getCapacity() > 0) {
      double capacity = fuelOptions.getCapacity();
      for (int i = 0; i < drivers.size(); i++) {
        int currentPct = (int) ((drivers.get(i).getFuelLevel() / capacity) * 100.0);
        this.protocols.setFuelLevel(i, currentPct);
        this.protocols.setRefueling(i, false);
      }
    }

    // 4. Heat Progress
    this.protocols.setHeatProgress(0);
  }

  private void createProtocols(boolean isDemoMode) {
    this.isDemoMode = isDemoMode;
    List<com.antigravity.protocols.IProtocol> protocols_list = new ArrayList<>();
    if (isDemoMode) {
      AnalogFuelOptions fuelOptions = this.model.getFuelOptions();
      boolean isFuelRace = fuelOptions != null && fuelOptions.isEnabled();
      Demo protocol = new Demo(this.track.getLanes().size(), isFuelRace);
      protocol.setInterfaceIndex(0);
      protocols_list.add(protocol);
    } else {
      List<ArduinoConfig> configs = this.track.getArduinoConfigs();
      if (configs != null && !configs.isEmpty()) {
        for (int i = 0; i < configs.size(); i++) {
          ArduinoConfig config = configs.get(i);
          ArduinoProtocol protocol = new ArduinoProtocol(config, this.track.getLanes().size());
          protocol.setInterfaceIndex(i);
          protocols_list.add(protocol);
        }
      } else {
        throw new IllegalArgumentException(
            "Race created in Real Mode, but no ArduinoConfig found for track: "
                + this.track.getName());
      }
    }
    this.protocols = new ProtocolDelegate(protocols_list);
    this.protocols.setListener(this);
  }

  public com.antigravity.models.Race getRaceModel() {
    return model;
  }

  public Track getTrack() {
    return track;
  }

  public List<Heat> getHeats() {
    return heats;
  }

  public void setHeats(List<Heat> heats) {
    this.heats = heats;
  }

  public Heat getCurrentHeat() {
    return currentHeat;
  }

  public void setCurrentHeat(Heat currentHeat) {
    this.currentHeat = currentHeat;
    resetHeatRecords();
    broadcastRecords();
  }

  public IRaceState getState() {
    return state;
  }

  public RaceStatistics getStatistics() {
    return statistics;
  }

  public float getRaceTime() {
    return accumulatedRaceTime;
  }

  public void addRaceTime(float delta) {
    accumulatedRaceTime += delta;
  }

  public void resetRaceTime() {
    accumulatedRaceTime = 0.0f;
  }

  public void resetHeatRecords() {
    this.heatBestLap = Double.MAX_VALUE;
    this.heatBestLapHolder = "";
    this.heatBestLapHolderNickname = "";
  }

  public boolean hasRacedInCurrentHeat() {
    return hasRacedInCurrentHeat;
  }

  public void setHasRacedInCurrentHeat(boolean hasRaced) {
    this.hasRacedInCurrentHeat = hasRaced;
  }

  public void broadcastRaceTime(double autoAdvanceRemaining, double autoStartRemaining) {
    float displayTime = Math.max(0, this.getRaceTime());
    RaceTime raceTimeMsg =
        RaceTime.newBuilder()
            .setTime(displayTime)
            .setAutoAdvanceRemaining(autoAdvanceRemaining)
            .setAutoStartRemaining(autoStartRemaining)
            .build();

    RaceData raceDataMsg = RaceData.newBuilder().setRaceTime(raceTimeMsg).build();

    this.broadcast(raceDataMsg);
  }

  public void broadcastFlag(com.antigravity.proto.RaceFlag flag) {
    RaceData raceDataMsg = RaceData.newBuilder().setFlag(flag).build();
    this.broadcast(raceDataMsg);
    if (protocols != null) {
      protocols.setRaceState(getProtoState(state), flag, 0);
    }
  }

  public boolean isAutoStartFired() {
    return autoStartFired;
  }

  public void setAutoStartFired(boolean fired) {
    this.autoStartFired = fired;
  }

  public boolean isAutoAdvanceFired() {
    return autoAdvanceFired;
  }

  public void setAutoAdvanceFired(boolean fired) {
    this.autoAdvanceFired = fired;
  }

  public double getAutoStartRemaining() {
    return autoStartRemaining;
  }

  public void setAutoStartRemaining(double remaining) {
    this.autoStartRemaining = remaining;
  }

  public double getAutoAdvanceRemaining() {
    return autoAdvanceRemaining;
  }

  public void setAutoAdvanceRemaining(double remaining) {
    this.autoAdvanceRemaining = remaining;
  }

  public void clearAutoTimers() {
    this.autoStartRemaining = 0;
    this.autoAdvanceRemaining = 0;
    broadcastTime();
  }

  public void broadcastTime() {
    RaceTime raceTimeMsg =
        RaceTime.newBuilder()
            .setTime(this.getRaceTime())
            .setAutoStartRemaining(this.getAutoStartRemaining())
            .setAutoAdvanceRemaining(this.getAutoAdvanceRemaining())
            .build();

    RaceData raceDataMsg = RaceData.newBuilder().setRaceTime(raceTimeMsg).build();

    this.broadcast(raceDataMsg);
  }

  public void broadcast(GeneratedMessageV3 message) {
    ClientSubscriptionManager.getInstance().broadcast(message);
  }

  public synchronized void changeState(IRaceState newState) {
    IRaceState previousState = this.state;
    this.state = newState;

    // Initialize the new state immediately
    this.state.enter(this);

    // Calculate and broadcast the new state and flag for UI responsiveness
    // This happens before the potentially slow exit() of the previous state
    RaceState protoState = getProtoState(state);
    RaceFlag protoFlag = state.getFlagType(this);

    RaceData raceData = RaceData.newBuilder().setRaceState(protoState).setFlag(protoFlag).build();
    broadcast(raceData);

    if (protocols != null) {
      protocols.setRaceState(protoState, protoFlag, 0);
    }

    if (previousState != null) {
      previousState.exit(this);
    }

    if (state instanceof RaceOver) {
      ClientSubscriptionManager.getInstance().deleteAutoSave(model.getEntityId());
    }
  }

  public void startRace() {
    state.start(this);
  }

  public void pauseRace() {
    state.pause(this);
  }

  public void restartHeat() {
    state.restartHeat(this);
  }

  public void skipHeat() {
    state.skipHeat(this);
  }

  public void deferHeat() {
    state.deferHeat(this);
  }

  public void stop() {
    if (protocols != null) {
      protocols.clearLeds();
      protocols.close();
    }
    if (state != null) {
      state.exit(this);
    }
  }

  public void setMainPower(boolean on) {
    if (this.mainPower == on) {
      return;
    }
    this.mainPower = on;
    protocols.setMainPower(on);
  }

  public boolean isMainPower() {
    return mainPower;
  }

  public void setLanePower(boolean on, int lane) {
    if (lane < 0) {
      for (int i = 0; i < this.track.getLanes().size(); i++) {
        protocols.setLanePower(on, i);
      }
    } else {
      protocols.setLanePower(on, lane);
    }
  }

  public void startProtocols() {
    protocols.startTimer();
  }

  public List<com.antigravity.protocols.PartialTime> stopProtocols() {
    return protocols.stopTimer();
  }

  public void setHeatStandings(List<Integer> laneIndices) {
    if (protocols != null) {
      protocols.setHeatStandings(laneIndices);
    }
  }

  public void setRefueling(int laneIndex, boolean isRefueling) {
    if (protocols != null) {
      protocols.setRefueling(laneIndex, isRefueling);
    }
  }

  public void setFuelLevel(int laneIndex, int fuelLevelPct) {
    if (protocols != null) {
      protocols.setFuelLevel(laneIndex, fuelLevelPct);
    }
  }

  public void setHeatProgress(double percentage) {
    if (this.protocols != null) {
      this.protocols.setHeatProgress(percentage);
    }
  }

  public void setRaceState(
      com.antigravity.proto.RaceState state,
      com.antigravity.proto.RaceFlag flag,
      double countdown) {
    if (protocols != null) {
      protocols.setRaceState(state, flag, countdown);
    }
  }

  public void initializeHeatExecutionState() {
    int laneCount = 0;
    if (this.track != null && this.track.getLanes() != null) {
      laneCount = this.track.getLanes().size();
    }
    this.executionManager.initialize(laneCount);
  }

  public HeatExecutionManager getHeatExecutionManager() {
    return executionManager;
  }

  public void prepareHeat() {
    initializeHeatExecutionState();
    FuelOptions fuelOptions = null;
    if (track != null && track.hasDigitalFuel()) {
      fuelOptions = model.getDigitalFuelOptions();
    } else {
      fuelOptions = model.getFuelOptions();
    }

    if (fuelOptions == null || !fuelOptions.isEnabled()) {
      return;
    }

    boolean resetAtStart = fuelOptions.isResetFuelAtHeatStart();
    double startLevel = (fuelOptions.getCapacity() * fuelOptions.getStartLevel()) / 100.0;

    for (DriverHeatData heatData : currentHeat.getDrivers()) {
      RaceParticipant participant = heatData.getDriver();
      if (participant == null
          || participant.getDriver() == null
          || participant.getDriver().getEntityId() == null) {
        continue;
      }

      if (resetAtStart) {
        participant.setFuelLevel(startLevel);
      }

      // Store the initial fuel level for this heat to support restarts
      heatData.setInitialFuelLevel(participant.getFuelLevel());
    }
  }

  public void restoreHeatFuel() {
    FuelOptions fuelOptions = null;
    if (track != null && track.hasDigitalFuel()) {
      fuelOptions = model.getDigitalFuelOptions();
    } else {
      fuelOptions = model.getFuelOptions();
    }

    if (fuelOptions == null || !fuelOptions.isEnabled()) {
      return;
    }

    for (DriverHeatData heatData : currentHeat.getDrivers()) {
      heatData.getDriver().setFuelLevel(heatData.getInitialFuelLevel());
    }
  }

  public void resetCurrentHeat() {
    System.out.println("Race.resetCurrentHeat() called.");

    if (currentHeat != null) {
      statistics.incrementRestartCount();
      // Reset all drivers in the heat
      for (DriverHeatData driverData : currentHeat.getDrivers()) {
        driverData.reset();
      }

      // Reset standings to initial order
      currentHeat.getHeatStandings().reset();

      // Reset race time
      resetRaceTime();

      initializeHeatExecutionState();

      restoreHeatFuel();

      // Broadcast update to client
      Set<String> sentObjectIds = new HashSet<>();
      for (RaceParticipant p : getDrivers()) {
        sentObjectIds.add(HeatConverter.PARTICIPANT_PREFIX + p.getObjectId());
      }

      com.antigravity.proto.Race raceProto =
          com.antigravity.proto.Race.newBuilder()
              .setCurrentHeat(HeatConverter.toProto(currentHeat, sentObjectIds))
              .build();

      broadcast(RaceData.newBuilder().setRace(raceProto).build());

      // Reset heat records and broadcast them
      resetHeatRecords();
      broadcastRecords();

      // Also broadcast time reset
      broadcastTime();
    }
  }

  public void updateAndBroadcastOverallStandings() {
    overallStandings.recalculate(this.drivers, this.heats);

    // Broadcast updates
    List<com.antigravity.proto.RaceParticipant> participants = new ArrayList<>();
    Set<String> sentObjectIds = new HashSet<>();
    for (RaceParticipant driver : this.drivers) {
      if (driver.getDriver() != Driver.EMPTY_DRIVER) {
        participants.add(RaceParticipantConverter.toProto(driver, sentObjectIds));
      }
    }

    OverallStandingsUpdate update =
        OverallStandingsUpdate.newBuilder().addAllParticipants(participants).build();

    RaceData raceData = RaceData.newBuilder().setOverallStandingsUpdate(update).build();

    broadcast(raceData);
  }

  public boolean isActive() {
    return !(state instanceof RaceOver);
  }

  @Override
  public void onLap(int lane, double lapTime, int interfaceId, int interfaceIndex) {
    try {
      if (state.onLap(lane, lapTime, interfaceId, false)) {
        // Calculate effective lap time (including reaction time for first lap)
        // to match HeatExecutionManager calculation
        double effectiveLapTime = lapTime;
        DriverHeatData dhd = currentHeat.getDrivers().get(lane);
        if (dhd != null && dhd.getLapCount() == 1) { // dhd.addLap was already called in state.onLap
          // At this point, dhd.getLapCount() is already 1 because handleLapTime called addLap()
          // We need to look at the last lap time added to the driver data
          List<DriverHeatData.LapData> laps = dhd.getLaps();
          if (!laps.isEmpty()) {
            effectiveLapTime = laps.get(laps.size() - 1).getLapTime();
          }
        }

        // Update records with the same effective time shown in lane data
        updateRecords(lane, effectiveLapTime);
      }
    } catch (Exception e) {
      System.err.println("Error in onLap for lane " + lane + ": " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void updateRecords(int lane, double lapTime) {
    if (lapTime <= 0) return;

    DriverHeatData dhd = currentHeat.getDrivers().get(lane);
    if (dhd == null) return;

    String driverName = "Driver";
    String driverNickname = "Driver";

    Driver actualDriver = dhd.getActualDriver();
    if (actualDriver == Driver.EMPTY_DRIVER) return;

    if (actualDriver != null) {
      driverName = actualDriver.getName();
      driverNickname = actualDriver.getNickname();
      if (driverNickname == null || driverNickname.isEmpty()) {
        driverNickname = driverName;
      }
    } else if (dhd.getDriver() != null) {
      // Fallback to participant level driver/name
      driverName = dhd.getDriver().getDriver().getName();
      driverNickname = dhd.getDriver().getDriver().getNickname();
      if (driverNickname == null || driverNickname.isEmpty()) {
        driverNickname = driverName;
      }
    }

    String lapHolderName = driverName;
    String lapHolderNickname = driverNickname;
    String scoreHolderName = driverName;
    String scoreHolderNickname = driverNickname;

    RaceParticipant participant = dhd.getDriver();
    if (participant.isTeamParticipant() && participant.getTeam() != null) {
      scoreHolderName = participant.getTeam().getName();
      // For teams, we use the team name for both name and nickname for consistency
      scoreHolderNickname = scoreHolderName;
    }

    boolean changed = false;

    // Heat Best Lap
    if (lapTime < heatBestLap) {
      heatBestLap = lapTime;
      heatBestLapHolder = lapHolderName;
      heatBestLapHolderNickname = lapHolderNickname;
      changed = true;
    }

    // Race Best Lap
    if (lapTime < raceBestLap) {
      raceBestLap = lapTime;
      raceBestLapHolder = lapHolderName;
      raceBestLapHolderNickname = lapHolderNickname;
      changed = true;
    }

    // Overall Best Lap
    if (overallBestLap == 0 || lapTime < overallBestLap) {
      overallBestLap = lapTime;
      overallBestLapHolder = lapHolderName;
      overallBestLapHolderNickname = lapHolderNickname;
      overallBestLapDate = System.currentTimeMillis();
      changed = true;
    }

    // Overall Best Score (Highest Lap Count)
    double currentScore = participant.getRankValue();
    if (currentScore > overallBestScore) {
      overallBestScore = currentScore;
      overallBestScoreHolder = scoreHolderName;
      overallBestScoreHolderNickname = scoreHolderNickname;
      overallBestScoreDate = System.currentTimeMillis();
      changed = true;
    }

    if (changed) {
      broadcastRecords();
    }
  }

  public RecordData getRecordData() {
    return RecordData.newBuilder()
        .setOverallBestLap(
            RecordEntry.newBuilder()
                .setValue(overallBestLap == Double.MAX_VALUE ? 0 : overallBestLap)
                .setHolderName(overallBestLapHolder != null ? overallBestLapHolder : "")
                .setHolderNickname(
                    overallBestLapHolderNickname != null ? overallBestLapHolderNickname : "")
                .setDate(overallBestLapDate)
                .build())
        .setOverallBestScore(
            RecordEntry.newBuilder()
                .setValue(overallBestScore)
                .setHolderName(overallBestScoreHolder != null ? overallBestScoreHolder : "")
                .setHolderNickname(
                    overallBestScoreHolderNickname != null ? overallBestScoreHolderNickname : "")
                .setDate(overallBestScoreDate)
                .build())
        .setRaceBestLap(
            RecordEntry.newBuilder()
                .setValue(raceBestLap == Double.MAX_VALUE ? 0 : raceBestLap)
                .setHolderName(raceBestLapHolder != null ? raceBestLapHolder : "")
                .setHolderNickname(
                    raceBestLapHolderNickname != null ? raceBestLapHolderNickname : "")
                .build())
        .setHeatBestLap(
            RecordEntry.newBuilder()
                .setValue(heatBestLap == Double.MAX_VALUE ? 0 : heatBestLap)
                .setHolderName(heatBestLapHolder != null ? heatBestLapHolder : "")
                .setHolderNickname(
                    heatBestLapHolderNickname != null ? heatBestLapHolderNickname : "")
                .build())
        .build();
  }

  public void broadcastRecords() {
    RecordData recordData = getRecordData();
    broadcast(RaceData.newBuilder().setRecordData(recordData).build());
  }

  @Override
  public void onSegment(int lane, double segmentTime, int interfaceId, int interfaceIndex) {
    state.onSegment(lane, segmentTime, interfaceId);
  }

  @Override
  public void onCallbutton(int lane, int interfaceIndex) {
    state.onCallbutton(this, lane);
    // Broadcast as InterfaceEvent for UI feedback in Editor
    InterfaceEvent event =
        InterfaceEvent.newBuilder()
            .setCallbutton(
                CallbuttonEvent.newBuilder()
                    .setLane(lane)
                    .setInterfaceIndex(interfaceIndex)
                    .build())
            .build();
    ClientSubscriptionManager.getInstance().broadcastInterfaceEvent(event);
  }

  @Override
  public void onInterfaceStatus(InterfaceStatus status, int interfaceIndex) {
    InterfaceEvent event =
        InterfaceEvent.newBuilder()
            .setStatus(
                InterfaceStatusEvent.newBuilder()
                    .setStatus(status)
                    .setInterfaceIndex(interfaceIndex)
                    .build())
            .build();
    // Since this is an InterfaceEvent, we use broadcastInterfaceEvent if available
    // or just broadcast it if it's a generic message.
    // InterfaceEvent is generated from proto.
    ClientSubscriptionManager.getInstance().broadcastInterfaceEvent(event);
  }

  @Override
  public void onCarData(CarData carData) {
    state.onCarData(carData);
  }

  @Override
  public void onInterfaceEvent(InterfaceEvent event) {
    ClientSubscriptionManager.getInstance().broadcastInterfaceEvent(event);
  }

  public boolean isLastHeat() {
    return heats.indexOf(currentHeat) == heats.size() - 1;
  }

  // TODO(aufderheide): This synchronize probably isn't enough. We need to lock
  // the race object while we're creating the snapshot.
  public synchronized RaceData createSnapshot() {
    Set<String> sentObjectIds = new HashSet<>();
    com.antigravity.proto.Race raceUpdate = RaceConverter.toProto(this, sentObjectIds);

    // Update state and flag correctly from current state
    raceUpdate =
        raceUpdate.toBuilder()
            .setState(getProtoState(state))
            .setFlag(state.getFlagType(this))
            .build();

    RecordData recordData = getRecordData();
    System.out.println(
        "Snapshot created with records: overallBestLap="
            + recordData.getOverallBestLap().getValue()
            + " ("
            + recordData.getOverallBestLap().getHolderNickname()
            + ")");

    return RaceData.newBuilder().setRace(raceUpdate).setRecordData(recordData).build();
  }

  public void moveToNextHeat() {
    this.autoStartFired = false;
    this.autoAdvanceFired = false;
    this.autoStartRemaining = 0;
    this.autoAdvanceRemaining = 0;
    state.nextHeat(this);
  }

  // TODO(aufderheide): We should ask the state for it's enum value rather than
  // doing all these instanceof checks.
  private RaceState getProtoState(IRaceState state) {
    if (state instanceof NotStarted) {
      return RaceState.NOT_STARTED;
    } else if (state instanceof Starting) {
      return RaceState.STARTING;
    } else if (state instanceof Racing) {
      return RaceState.RACING;
    } else if (state instanceof Paused) {
      return RaceState.PAUSED;
    } else if (state instanceof HeatOver) {
      return RaceState.HEAT_OVER;
    } else if (state instanceof RaceOver) {
      return RaceState.RACE_OVER;
    }
    return RaceState.UNKNOWN_STATE;
  }
}
