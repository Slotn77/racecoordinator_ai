import { Driver } from "@app/models/driver";
import { FinishMethod } from "@app/models/heat_scoring";
import { OverallRanking } from "@app/models/overall_scoring";
import { Race } from "@app/models/race";
import { RaceParticipant } from "@app/models/race_participant";
import { Track } from "@app/models/track";
import { RaceState } from "@app/proto/antigravity";
import { DriverHeatData } from "@app/race/driver_heat_data";
import { Heat } from "@app/race/heat";

export interface MockEditorData {
  raceState: RaceState;
  time: number;
  race: Race;
  track: Track;
  participants: RaceParticipant[];
  heat: Heat;
  heats: Heat[];
  groupParticipants: RaceParticipant[];
}

export function createMockEditorData(): MockEditorData {
  const raceState = RaceState.RACING;
  const time = 3600;
  const race = {
    name: "Mock Editor Race",
    track_id: "mock_track_1",
    heats_run: 1,
    overall_scoring: {
      rankingMethod: OverallRanking.OR_LAP_COUNT,
      finishMethod: FinishMethod.Timed,
      points: [],
    },
    group_options: {
      enabled: true,
    },
  } as unknown as Race;

  const track = createMockTrack();
  const participants = createMockRaceParticipants();

  const heatDrivers = participants.map((p: any, index) => {
    const hd = new DriverHeatData(`mock_hd_${index}`, p, p.lane - 1);
    hd.addLapTime(
      p.lap_count,
      p.last_lap_time,
      p.average_lap_time,
      p.median_lap_time,
      p.best_lap_time,
      p.lap_count,
    );
    hd.reactionTime = p.reaction_time || 0.123;
    hd.gapLeader = p.gap_leader || 0;
    hd.gapPosition = p.gap_position || 0;

    // Add mock fuel and seed to participant if missing
    if (hd.participant) {
      (hd.participant as any).fuelLevel = p.fuelLevel || 50;
      (hd.participant as any).seed = p.seed || index + 1;
      (hd.participant as any).team = p.team || { name: `Team ${index + 1}` };
    }

    // Add mock segment times
    hd.addSegmentTime(0, 0.5);
    hd.addSegmentTime(1, 0.6);
    hd.addSegmentTime(2, 0.7);

    // Inject custom column attributes that may be dynamically accessed
    (hd as any).rankHeat = p.rank;
    (hd as any).rankOverall = p.rank + 1;
    (hd as any).speedMph = 15.5;
    (hd as any).speedKph = 25.0;
    (hd as any).speedFph = 80000;
    (hd as any).fuelPercentage = p.fuelLevel;
    (hd as any).fuelCapacity = 100;

    return hd;
  });

  const heat = {
    id: "mock_heat_1",
    objectId: "mock_heat_1",
    race_id: "mock_race_1",
    heatNumber: 1,
    start_time: "2026-06-05T12:00:00Z",
    end_time: "2026-06-05T12:03:00Z",
    heatDrivers: heatDrivers,
  } as unknown as Heat;

  const nextHeatDrivers = createMockNextHeatParticipants().map(
    (p: any, index) => {
      const hd = new DriverHeatData(`mock_hd_next_${index}`, p, p.lane - 1);
      hd.reactionTime = 0.15;
      if (hd.participant) {
        (hd.participant as any).fuelLevel = p.fuelLevel || 100;
        (hd.participant as any).seed = p.seed || index + 5;
        (hd.participant as any).team = p.team;
      }
      return hd;
    },
  );

  const nextHeat = {
    id: "mock_heat_2",
    objectId: "mock_heat_2",
    race_id: "mock_race_1",
    heatNumber: 2,
    started: false,
    heatDrivers: nextHeatDrivers,
  } as unknown as Heat;

  const heats = [heat, nextHeat];
  const groupParticipants = participants;

  return {
    raceState,
    time,
    race,
    track,
    participants,
    heat,
    heats,
    groupParticipants,
  };
}

function createMockTrack(): Track {
  return {
    name: "Mock Editor Track",
    hasDigitalFuel: () => true,
    lanes: [
      { id: 1, color: "#FF0000" },
      { id: 2, color: "#0000FF" },
      { id: 3, color: "#FFFF00" },
      { id: 4, color: "#00FF00" },
    ],
  } as unknown as Track;
}

function createMockRaceParticipants(): RaceParticipant[] {
  return [
    {
      id: "p1",
      objectId: "p1",
      driver: new Driver(
        "d1",
        "Mario",
        "Jumpman",
        "https://api.dicebear.com/7.x/pixel-art/svg?seed=Mario",
      ),
      lane: 1,
      total_time: 120,
      lap_count: 5,
      last_lap_time: 2.1,
      best_lap_time: 2.0,
      average_lap_time: 2.5,
      median_lap_time: 2.4,
      rank: 1,
      reaction_time: 0.05,
      gap_leader: 0,
      gap_position: 0,
      fuelLevel: 100,
      seed: 1,
      team: { name: "Mushroom Kingdom", driverIds: ["d1", "d2", "d4"] },
    } as unknown as RaceParticipant,
    {
      id: "p2",
      objectId: "p2",
      driver: new Driver(
        "d2",
        "Luigi",
        "Green Mario",
        "https://api.dicebear.com/7.x/pixel-art/svg?seed=Luigi",
      ),
      lane: 2,
      total_time: 125,
      lap_count: 4,
      last_lap_time: 2.8,
      best_lap_time: 2.5,
      average_lap_time: 3.0,
      median_lap_time: 2.9,
      rank: 2,
      reaction_time: 0.15,
      gap_leader: 5,
      gap_position: 5,
      fuelLevel: 80,
      seed: 2,
      team: { name: "Mushroom Kingdom", driverIds: ["d1", "d2", "d4"] },
    } as unknown as RaceParticipant,
    {
      id: "p3",
      objectId: "p3",
      driver: new Driver(
        "d3",
        "Bowser",
        "King Koopa",
        "https://api.dicebear.com/7.x/pixel-art/svg?seed=Bowser",
      ),
      lane: 3,
      total_time: 130,
      lap_count: 4,
      last_lap_time: 3.1,
      best_lap_time: 2.9,
      average_lap_time: 3.2,
      median_lap_time: 3.1,
      rank: 3,
      reaction_time: 0.223,
      gap_leader: 10,
      gap_position: 5,
      fuelLevel: 60,
      seed: 3,
      team: { name: "Koopa Troop", driverIds: ["d3"] },
    } as unknown as RaceParticipant,
    {
      id: "p4",
      objectId: "p4",
      driver: new Driver(
        "d4",
        "Peach",
        "Princess",
        "https://api.dicebear.com/7.x/pixel-art/svg?seed=Peach",
      ),
      lane: 4,
      total_time: 140,
      lap_count: 3,
      last_lap_time: 3.5,
      best_lap_time: 3.2,
      average_lap_time: 3.6,
      median_lap_time: 3.5,
      rank: 4,
      reaction_time: 0.101,
      gap_leader: 20,
      gap_position: 10,
      fuelLevel: 40,
      seed: 4,
      team: { name: "Mushroom Kingdom", driverIds: ["d1", "d2", "d4"] },
    } as unknown as RaceParticipant,
  ];
}

function createMockNextHeatParticipants(): RaceParticipant[] {
  return [
    {
      id: "p5",
      objectId: "p5",
      driver: new Driver(
        "d5",
        "Yoshi",
        "Green Dino",
        "https://api.dicebear.com/7.x/pixel-art/svg?seed=Yoshi",
      ),
      lane: 1,
      total_time: 0,
      lap_count: 0,
      last_lap_time: 0,
      best_lap_time: 0,
      average_lap_time: 0,
      median_lap_time: 0,
      rank: 5,
      reaction_time: 0,
      gap_leader: 0,
      gap_position: 0,
      fuelLevel: 100,
      seed: 5,
      team: { name: "Yoshi Island", driverIds: ["d5"] },
    } as unknown as RaceParticipant,
    {
      id: "p6",
      objectId: "p6",
      driver: new Driver(
        "d6",
        "Donkey Kong",
        "DK",
        "https://api.dicebear.com/7.x/pixel-art/svg?seed=DK",
      ),
      lane: 2,
      total_time: 0,
      lap_count: 0,
      last_lap_time: 0,
      best_lap_time: 0,
      average_lap_time: 0,
      median_lap_time: 0,
      rank: 6,
      reaction_time: 0,
      gap_leader: 0,
      gap_position: 0,
      fuelLevel: 100,
      seed: 6,
      team: { name: "DK Crew", driverIds: ["d6"] },
    } as unknown as RaceParticipant,
    {
      id: "p7",
      objectId: "p7",
      driver: new Driver(
        "d7",
        "Wario",
        "Greedy",
        "https://api.dicebear.com/7.x/pixel-art/svg?seed=Wario",
      ),
      lane: 3,
      total_time: 0,
      lap_count: 0,
      last_lap_time: 0,
      best_lap_time: 0,
      average_lap_time: 0,
      median_lap_time: 0,
      rank: 7,
      reaction_time: 0,
      gap_leader: 0,
      gap_position: 0,
      fuelLevel: 100,
      seed: 7,
      team: { name: "Wario Land", driverIds: ["d7"] },
    } as unknown as RaceParticipant,
    {
      id: "p8",
      objectId: "p8",
      driver: new Driver(
        "d8",
        "Waluigi",
        "Purple",
        "https://api.dicebear.com/7.x/pixel-art/svg?seed=Waluigi",
      ),
      lane: 4,
      total_time: 0,
      lap_count: 0,
      last_lap_time: 0,
      best_lap_time: 0,
      average_lap_time: 0,
      median_lap_time: 0,
      rank: 8,
      reaction_time: 0,
      gap_leader: 0,
      gap_position: 0,
      fuelLevel: 100,
      seed: 8,
      team: { name: "Waluigi Pinball", driverIds: ["d8"] },
    } as unknown as RaceParticipant,
  ];
}
