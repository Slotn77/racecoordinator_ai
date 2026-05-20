package com.antigravity.race.states;

import com.antigravity.converters.HeatConverter;
import com.antigravity.proto.RaceData;
import com.antigravity.race.Heat;
import com.antigravity.race.Race;
import com.antigravity.race.RaceParticipant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Common {

  public static void advanceToNextHeat(Race race) {
    List<Heat> heats = race.getHeats();
    Heat currentHeat = race.getCurrentHeat();
    int currentIndex = heats.indexOf(currentHeat);

    if (currentIndex < heats.size() - 1) {
      race.setCurrentHeat(heats.get(currentIndex + 1));
      race.resetRaceTime();
      race.prepareHeat();
      race.setAutoStartFired(false);
      race.setAutoAdvanceFired(false);
      race.changeState(new NotStarted());

      // Optimized update: send currentHeat and all heats
      Set<String> sentObjectIds = new HashSet<>();
      for (RaceParticipant p : race.getDrivers()) {
        sentObjectIds.add(HeatConverter.PARTICIPANT_PREFIX + p.getObjectId());
      }

      com.antigravity.proto.Race raceProto = // fqn-collision
          com.antigravity.proto.Race.newBuilder() // fqn-collision
              .setCurrentHeat(HeatConverter.toProto(race.getCurrentHeat(), sentObjectIds))
              .addAllHeats(
                  heats.stream()
                      .map(h -> HeatConverter.toProto(h, sentObjectIds))
                      .collect(Collectors.toList()))
              .build();

      race.broadcast(RaceData.newBuilder().setRace(raceProto).build());
    } else {
      race.changeState(new RaceOver());
    }
  }
}
