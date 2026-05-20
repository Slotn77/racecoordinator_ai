package com.antigravity.race.states;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.antigravity.proto.RaceData;
import com.antigravity.race.Heat;
import com.antigravity.race.Race;
import com.antigravity.race.RaceParticipant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class CommonTest {

  @Test
  public void testAdvanceToNextHeat_NotLastHeat() {
    Race race = mock(Race.class);
    Heat h1 = mock(Heat.class);
    Heat h2 = mock(Heat.class);

    when(h1.getObjectId()).thenReturn("h1-id");
    when(h2.getObjectId()).thenReturn("h2-id");

    List<Heat> heats = new ArrayList<>(Arrays.asList(h1, h2));
    when(race.getHeats()).thenReturn(heats);
    when(race.getCurrentHeat()).thenReturn(h1);

    RaceParticipant p1 = mock(RaceParticipant.class);
    when(p1.getObjectId()).thenReturn("p1-id");
    when(race.getDrivers()).thenReturn(Collections.singletonList(p1));

    Common.advanceToNextHeat(race);

    // Verifications on race state/setup
    verify(race).setCurrentHeat(h2);
    verify(race).resetRaceTime();
    verify(race).prepareHeat();
    verify(race).setAutoStartFired(false);
    verify(race).setAutoAdvanceFired(false);

    // Verifies that state was changed to NotStarted
    ArgumentCaptor<IRaceState> stateCaptor = ArgumentCaptor.forClass(IRaceState.class);
    verify(race).changeState(stateCaptor.capture());
    assertTrue(stateCaptor.getValue() instanceof NotStarted);

    // Verifies that broadcast was called with expected proto
    ArgumentCaptor<RaceData> dataCaptor = ArgumentCaptor.forClass(RaceData.class);
    verify(race).broadcast(dataCaptor.capture());
    RaceData broadcastData = dataCaptor.getValue();
    assertNotNull(broadcastData);
    assertTrue(broadcastData.hasRace());
    assertNotNull(broadcastData.getRace().getCurrentHeat());
    assertEquals(2, broadcastData.getRace().getHeatsCount());
  }

  @Test
  public void testAdvanceToNextHeat_LastHeat() {
    Race race = mock(Race.class);
    Heat h1 = mock(Heat.class);

    when(h1.getObjectId()).thenReturn("h1-id");

    List<Heat> heats = new ArrayList<>(Collections.singletonList(h1));
    when(race.getHeats()).thenReturn(heats);
    when(race.getCurrentHeat()).thenReturn(h1);

    Common.advanceToNextHeat(race);

    // Verifies that state was changed to RaceOver
    ArgumentCaptor<IRaceState> stateCaptor = ArgumentCaptor.forClass(IRaceState.class);
    verify(race).changeState(stateCaptor.capture());
    assertTrue(stateCaptor.getValue() instanceof RaceOver);

    // No next heat, so setCurrentHeat etc. shouldn't be called
    verify(race, never()).setCurrentHeat(any());
    verify(race, never()).resetRaceTime();
    verify(race, never()).prepareHeat();
    verify(race, never()).broadcast(any());
  }
}
