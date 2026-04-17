package com.antigravity.race.states;

import com.antigravity.proto.RaceFlag;
import com.antigravity.protocols.CarData;
import com.antigravity.race.Race;

public interface IRaceState {

  RaceFlag getFlagType(Race race);

  void enter(Race race);

  void exit(Race race);

  void start(Race race);

  void pause(Race race);

  void nextHeat(Race race);

  void restartHeat(Race race);

  void skipHeat(Race race);

  void deferHeat(Race race);

  // From the protocol listener
  boolean onLap(int lane, double lapTime, int interfaceId, boolean isDrift);

  default boolean handleLap(Race race, int lane, double lapTime, int interfaceId, boolean isDrift) {
    if (race != null && race.getHeatExecutionManager() != null) {
      return race.getHeatExecutionManager().onLap(lane, lapTime, interfaceId, false, true, isDrift);
    }
    return false;
  }

  void onSegment(int lane, double segmentTime, int interfaceId);

  void onCarData(CarData carData);

  void onCallbutton(Race race, int lane);
}
