import { TranslationService } from "@app/services/translation.service";

export interface LaneEqualityReportItem {
  key: string;
  params?: any;
}

export interface LaneEqualityResult {
  allEqual: boolean;
  reports: LaneEqualityReportItem[];
}

export function checkLaneEquality(
  numLanes: number,
  driverIds: string[],
  heats: (string | null)[][],
  driverNames?: Map<string, string>,
  translationService?: TranslationService,
): LaneEqualityResult {
  const numDrivers = driverIds.length;
  const numHeats = heats.length;

  if (numDrivers <= 0 || numHeats === 0) {
    return {
      allEqual: false,
      reports: [{ key: "AM_REPORT_NO_DRIVERS" }],
    };
  }

  const { driverLaneCounts, laneTotals, reports, initialAllEqual } =
    populateLaneCounts(numLanes, driverIds, heats, driverNames);

  const { allEqual, finalReports } = checkDiffsAndBuildReports(
    numLanes,
    driverIds,
    driverLaneCounts,
    laneTotals,
    driverNames,
    translationService,
    reports,
    initialAllEqual,
  );

  if (allEqual && finalReports.length === 0) {
    return {
      allEqual: true,
      reports: [{ key: "AM_REPORT_ALL_EQUAL" }],
    };
  }

  return {
    allEqual,
    reports: finalReports,
  };
}

function populateLaneCounts(
  numLanes: number,
  driverIds: string[],
  heats: (string | null)[][],
  driverNames?: Map<string, string>,
) {
  const driverLaneCounts = new Map<string, number[]>();
  const laneTotals = new Array(numLanes).fill(0);
  const reports: LaneEqualityReportItem[] = [];
  let initialAllEqual = true;

  for (const dId of driverIds) {
    driverLaneCounts.set(dId, new Array(numLanes).fill(0));
  }

  const driverIdSet = new Set(driverIds);

  heats.forEach((heat, hIdx) => {
    const isHeatEmpty = heat.every((dId) => !dId || dId === "EMPTY_LANE");
    if (isHeatEmpty) {
      initialAllEqual = false;
      reports.push({
        key: "AM_REPORT_EMPTY_HEAT",
        params: { heat: hIdx + 1 },
      });
    }

    heat.forEach((dId, laneIdx) => {
      if (dId && dId !== "EMPTY_LANE") {
        if (driverIdSet.has(dId)) {
          laneTotals[laneIdx]++;
          const counts = driverLaneCounts.get(dId);
          if (counts && laneIdx < numLanes) {
            counts[laneIdx]++;
          }
        } else {
          initialAllEqual = false;
          const displayName = driverNames?.get(dId) || dId;
          reports.push({
            key: "AM_REPORT_INVALID_DRIVER",
            params: { heat: hIdx + 1, driver: displayName },
          });
        }
      }
    });
  });

  return { driverLaneCounts, laneTotals, reports, initialAllEqual };
}

function checkDiffsAndBuildReports(
  numLanes: number,
  driverIds: string[],
  driverLaneCounts: Map<string, number[]>,
  laneTotals: number[],
  driverNames?: Map<string, string>,
  translationService?: TranslationService,
  initialReports: LaneEqualityReportItem[] = [],
  initialAllEqual: boolean = true,
) {
  const finalReports = [...initialReports];
  let allEqual = initialAllEqual;
  let totalAssignments = 0;
  const numDrivers = driverIds.length;

  const getHeatLabel = (count: number): string => {
    if (translationService) {
      return translationService.translate(
        count === 1 ? "AM_LABEL_HEAT_SINGULAR" : "AM_LABEL_HEAT_PLURAL",
      );
    }
    return count === 1 ? "heat" : "heats";
  };

  for (let l = 0; l < numLanes; l++) {
    totalAssignments += laneTotals[l];
    for (let i = 0; i < numDrivers; i++) {
      const d1 = driverIds[i];
      const count1 = driverLaneCounts.get(d1)![l];
      const d1Name = driverNames?.get(d1) || d1;

      for (let j = 0; j < numDrivers; j++) {
        if (i === j) continue;
        const d2 = driverIds[j];
        const count2 = driverLaneCounts.get(d2)![l];
        const d2Name = driverNames?.get(d2) || d2;

        if (count1 !== count2) {
          allEqual = false;
          finalReports.push({
            key: "AM_REPORT_LANE_DIFF",
            params: {
              lane: l + 1,
              d1: d1Name,
              count1: count1,
              heat1: getHeatLabel(count1),
              d2: d2Name,
              count2: count2,
              heat2: getHeatLabel(count2),
            },
          });
        }
      }
    }
  }

  if (totalAssignments === 0) {
    allEqual = false;
    finalReports.push({ key: "AM_REPORT_NO_DRIVERS" });
  }

  return { allEqual, finalReports };
}
