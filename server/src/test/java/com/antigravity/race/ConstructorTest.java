package com.antigravity.race;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.antigravity.models.HeatRotationType;
import com.antigravity.models.Race;
import com.antigravity.models.Track;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class ConstructorTest {

  @Test
  public void test() {
    Race model = mock(Race.class);
    when(model.getHeatRotationType()).thenReturn(HeatRotationType.RoundRobin);
    List<RaceParticipant> drivers = new ArrayList<>();
    drivers.add(
        new RaceParticipant(
            new com.antigravity.models.Driver(
                "d1", "Driver 1", null, null, null, null, null, null, null, null, null, "d1", null),
            "p1"));
    Track track = mock(Track.class);
    com.antigravity.race.Race race =
        new com.antigravity.race.Race.Builder()
            .model(model)
            .drivers(drivers)
            .track(track)
            .isDemoMode(true)
            .build();
  }

  @Test
  public void testCustomRotationsLoadedOnResume() {
    Race model = mock(Race.class);
    when(model.getHeatRotationType()).thenReturn(HeatRotationType.Custom);
    when(model.getCustomRotationAssetId()).thenReturn("asset1");
    List<RaceParticipant> drivers = new ArrayList<>();
    drivers.add(
        new RaceParticipant(
            new com.antigravity.models.Driver(
                "d1", "Driver 1", null, null, null, null, null, null, null, null, null, "d1", null),
            "p1"));
    Track track = mock(Track.class);

    com.antigravity.context.DatabaseContext dbContext =
        mock(com.antigravity.context.DatabaseContext.class);
    com.mongodb.client.MongoDatabase mongoDb = mock(com.mongodb.client.MongoDatabase.class);
    com.mongodb.client.MongoCollection assetCollection =
        mock(com.mongodb.client.MongoCollection.class);
    com.mongodb.client.FindIterable iterable = mock(com.mongodb.client.FindIterable.class);

    when(dbContext.getDatabase()).thenReturn(mongoDb);
    when(mongoDb.getCollection("assets")).thenReturn(assetCollection);
    when(assetCollection.find(org.mockito.ArgumentMatchers.any(org.bson.conversions.Bson.class)))
        .thenReturn(iterable);

    org.bson.Document doc = new org.bson.Document();
    List<org.bson.Document> rotations = new java.util.ArrayList<>();
    org.bson.Document rotation =
        new org.bson.Document("num_drivers", 1).append("heats", new java.util.ArrayList<>());
    rotations.add(rotation);
    doc.append("custom_rotations", rotations);
    when(iterable.first()).thenReturn(doc);

    // Simulate resuming by providing heats in builder
    List<Heat> heats = new ArrayList<>();
    heats.add(mock(Heat.class));

    com.antigravity.race.Race race =
        new com.antigravity.race.Race.Builder()
            .model(model)
            .drivers(drivers)
            .track(track)
            .databaseContext(dbContext)
            .heats(heats)
            .isDemoMode(true)
            .build();

    org.junit.Assert.assertEquals(1, race.getCustomRotations().size());
  }
}
