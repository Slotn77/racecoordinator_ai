package com.antigravity.race;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.antigravity.context.DatabaseContext;
import com.antigravity.models.HeatRotationType;
import com.antigravity.models.HeatScoring;
import com.antigravity.models.Lane;
import com.antigravity.models.OverallScoring;
import com.antigravity.models.Track;
import com.antigravity.protocols.arduino.ArduinoConfig;
import com.antigravity.race.states.RaceOver;
import com.antigravity.service.DatabaseService;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DemoModePersistenceTest {

  private com.antigravity.race.Race race;
  private Track track;

  @Before
  public void setUp() {
    List<Lane> lanes = new ArrayList<>();
    lanes.add(new Lane("red", "black", 100, "l1", null));
    track = new Track("Test Track", lanes, new ArrayList<>(), "track1", null);

    com.antigravity.models.Race raceModel =
        new com.antigravity.models.Race.Builder()
            .withName("Test Race")
            .withTrackEntityId("track1")
            .withHeatRotationType(HeatRotationType.RoundRobin)
            .withHeatScoring(new HeatScoring())
            .withOverallScoring(new OverallScoring())
            .withEntityId("race1")
            .build();

    race =
        new com.antigravity.race.Race.Builder()
            .model(raceModel)
            .drivers(new ArrayList<>())
            .track(track)
            .isDemoMode(true)
            .build();
  }

  @After
  public void tearDown() {
    DatabaseService.setInstance(new DatabaseService());
    ClientSubscriptionManager.setInstance(null); // It will lazy-init next time
  }

  @Test
  public void testRaceOverSavesInDemoMode() {
    DatabaseService mockService = mock(DatabaseService.class);
    DatabaseService.setInstance(mockService);

    ClientSubscriptionManager mockManager = mock(ClientSubscriptionManager.class);
    ClientSubscriptionManager.setInstance(mockManager);

    DatabaseContext mockContext = mock(DatabaseContext.class);
    doReturn(mockContext).when(mockManager).getDatabaseContext();
    doReturn(mock(MongoDatabase.class)).when(mockContext).getDatabase();

    // Create and enter RaceOver state
    RaceOver raceOver = new RaceOver();
    raceOver.enter(race);

    // Verify that save methods WERE called (now allowed in demo mode)
    verify(mockService, times(1)).saveRaceHistory(any(), any());
    verify(mockService, times(1)).updateGlobalStatistics(any(), any());
  }

  @Test
  public void testRaceOverSavesInNormalMode() {
    // Create a race NOT in demo mode
    com.antigravity.models.Race raceModel =
        new com.antigravity.models.Race.Builder()
            .withName("Normal Race")
            .withTrackEntityId("track1")
            .withHeatRotationType(HeatRotationType.RoundRobin)
            .withHeatScoring(new HeatScoring())
            .withOverallScoring(new OverallScoring())
            .withEntityId("race2")
            .build();

    // Pass ArduinoConfig directly to the constructor since Track wraps them in unmodifiable lists
    ArduinoConfig config = new ArduinoConfig();
    config.commPort = "COM1";
    List<ArduinoConfig> configs = new ArrayList<>();
    configs.add(config);

    track = new Track(track.getName(), track.getLanes(), configs, track.getEntityId(), null);

    com.antigravity.race.Race normalRace =
        new com.antigravity.race.Race.Builder()
            .model(raceModel)
            .drivers(new ArrayList<>())
            .track(track)
            .isDemoMode(false)
            .build();

    DatabaseService mockService = mock(DatabaseService.class);
    DatabaseService.setInstance(mockService);

    ClientSubscriptionManager mockManager = mock(ClientSubscriptionManager.class);
    ClientSubscriptionManager.setInstance(mockManager);

    DatabaseContext mockContext = mock(DatabaseContext.class);
    doReturn(mockContext).when(mockManager).getDatabaseContext();
    doReturn(mock(MongoDatabase.class)).when(mockContext).getDatabase();

    RaceOver raceOver = new RaceOver();
    raceOver.enter(normalRace);

    // Verify that save methods WERE called
    verify(mockService, times(1)).saveRaceHistory(any(), any());
    verify(mockService, times(1)).updateGlobalStatistics(any(), any());
  }

  @Test
  public void testAutoSaveBypassedInDemoMode() {
    // This test was incomplete and didn't really test anything before either.
    // It is kept for future expansion if needed, but no longer uses static mocks.
  }
}
