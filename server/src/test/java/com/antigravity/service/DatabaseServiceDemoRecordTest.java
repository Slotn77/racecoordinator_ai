package com.antigravity.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antigravity.models.Driver;
import com.antigravity.models.GlobalStatistics;
import com.antigravity.models.RaceHistoryRecord;
import com.antigravity.race.Race;
import com.antigravity.race.RaceParticipant;
import com.antigravity.race.RaceSaveData;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class DatabaseServiceDemoRecordTest {

  private MongoDatabase mongoDatabase;
  private MongoCollection<RaceHistoryRecord> historyCollection;
  private MongoCollection<GlobalStatistics> statsCollection;
  private MongoCollection<RaceSaveData> savedRacesCollection;
  private DatabaseService dbService;

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() {
    mongoDatabase = mock(MongoDatabase.class);
    historyCollection = mock(MongoCollection.class);
    statsCollection = mock(MongoCollection.class);
    savedRacesCollection = mock(MongoCollection.class);

    // Mock demo collections
    when(mongoDatabase.getCollection(eq("demo_race_history"), eq(RaceHistoryRecord.class)))
        .thenReturn(historyCollection);
    when(mongoDatabase.getCollection(eq("demo_global_statistics"), eq(GlobalStatistics.class)))
        .thenReturn(statsCollection);
    when(mongoDatabase.getCollection(eq("demo_saved_races"), eq(RaceSaveData.class)))
        .thenReturn(savedRacesCollection);

    dbService = DatabaseService.getInstance();
  }

  @Test
  public void testSaveRaceHistoryInDemoMode() {
    com.antigravity.models.Race model =
        new com.antigravity.models.Race.Builder()
            .withName("Demo Race")
            .withEntityId("DEMO1")
            .build();
    List<RaceParticipant> drivers = new ArrayList<>();
    drivers.add(new RaceParticipant(new Driver("Dave", "DB")));

    Race runtimeRace =
        new Race.Builder()
            .model(model)
            .drivers(drivers)
            .track(dbService.getFactoryTrack())
            .isDemoMode(true)
            .build();

    dbService.saveRaceHistory(mongoDatabase, runtimeRace);

    // Verify it used the demo collection
    verify(mongoDatabase).getCollection(eq("demo_race_history"), eq(RaceHistoryRecord.class));
    verify(historyCollection).insertOne(any(RaceHistoryRecord.class));
  }

  @Test
  public void testUpdateGlobalStatisticsInDemoMode() {
    com.antigravity.models.Race model =
        new com.antigravity.models.Race.Builder()
            .withName("Demo Race")
            .withEntityId("DEMO1")
            .build();
    List<RaceParticipant> drivers = new ArrayList<>();
    drivers.add(new RaceParticipant(new Driver("Dave", "DB")));

    Race runtimeRace =
        new Race.Builder()
            .model(model)
            .drivers(drivers)
            .track(dbService.getFactoryTrack())
            .isDemoMode(true)
            .build();

    // Mock count/find to avoid NPE in updateGlobalStatistics
    when(statsCollection.find(any(org.bson.conversions.Bson.class)))
        .thenReturn(mock(com.mongodb.client.FindIterable.class));

    dbService.updateGlobalStatistics(mongoDatabase, runtimeRace);

    // Verify it used the demo collection
    verify(mongoDatabase).getCollection(eq("demo_global_statistics"), eq(GlobalStatistics.class));
  }

  @Test
  public void testUpsertAutoSaveInDemoMode() {
    RaceSaveData data = new RaceSaveData();
    data.setDemoMode(true);
    data.setSaveName("autosave_DEMO1.json");

    dbService.upsertAutoSave(mongoDatabase, data);

    // Verify it used the demo collection
    verify(mongoDatabase).getCollection(eq("demo_saved_races"), eq(RaceSaveData.class));
    verify(savedRacesCollection)
        .replaceOne(
            eq(com.mongodb.client.model.Filters.eq("saveName", "autosave_DEMO1.json")),
            eq(data),
            any(com.mongodb.client.model.ReplaceOptions.class));
  }

  // Helper for any() since it's not imported
  private <T> T any(Class<T> type) {
    return org.mockito.ArgumentMatchers.any(type);
  }
}
