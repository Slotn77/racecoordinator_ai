import { TranslationService } from "@app/services/translation.service";

import { checkLaneEquality } from "./lane-equality";

describe("checkLaneEquality", () => {
  let mockTranslationService: jasmine.SpyObj<TranslationService>;

  beforeEach(() => {
    mockTranslationService = jasmine.createSpyObj("TranslationService", [
      "translate",
    ]);
    mockTranslationService.translate.and.callFake((key: string) => {
      if (key === "AM_LABEL_HEAT_SINGULAR") return "singular_heat";
      if (key === "AM_LABEL_HEAT_PLURAL") return "plural_heats";
      return key;
    });
  });

  it("should return AM_REPORT_NO_DRIVERS if there are no drivers", () => {
    const result = checkLaneEquality(4, [], [["d1", null, null, null]]);
    expect(result.allEqual).toBeFalse();
    expect(result.reports).toEqual([{ key: "AM_REPORT_NO_DRIVERS" }]);
  });

  it("should return AM_REPORT_NO_DRIVERS if there are no heats", () => {
    const result = checkLaneEquality(4, ["d1"], []);
    expect(result.allEqual).toBeFalse();
    expect(result.reports).toEqual([{ key: "AM_REPORT_NO_DRIVERS" }]);
  });

  it("should return AM_REPORT_ALL_EQUAL when all drivers have equal lane assignments", () => {
    const driverIds = ["d1", "d2"];
    const heats = [
      ["d1", "d2"],
      ["d2", "d1"],
    ];
    const result = checkLaneEquality(2, driverIds, heats);
    expect(result.allEqual).toBeTrue();
    expect(result.reports).toEqual([{ key: "AM_REPORT_ALL_EQUAL" }]);
  });

  it("should report empty heats", () => {
    const driverIds = ["d1", "d2"];
    const heats = [
      ["d1", "d2"],
      [null, null],
    ];
    const result = checkLaneEquality(2, driverIds, heats);
    expect(result.allEqual).toBeFalse();
    expect(result.reports).toContain(
      jasmine.objectContaining({
        key: "AM_REPORT_EMPTY_HEAT",
        params: { heat: 2 },
      }),
    );
  });

  it("should report invalid drivers in heats", () => {
    const driverIds = ["d1"];
    const heats = [
      ["d1", "d2"], // d2 is not in driverIds list
    ];
    const result = checkLaneEquality(2, driverIds, heats);
    expect(result.allEqual).toBeFalse();
    expect(result.reports).toContain(
      jasmine.objectContaining({
        key: "AM_REPORT_INVALID_DRIVER",
        params: { heat: 1, driver: "d2" },
      }),
    );
  });

  it("should report lane differences when assignments are unequal", () => {
    const driverIds = ["d1", "d2"];
    const heats = [
      ["d1", "d2"],
      ["d1", "d2"], // d1 is on lane 1 twice, d2 is on lane 2 twice
    ];
    const result = checkLaneEquality(
      2,
      driverIds,
      heats,
      undefined,
      mockTranslationService,
    );
    expect(result.allEqual).toBeFalse();
    const laneDiffReport = result.reports.find(
      (r) => r.key === "AM_REPORT_LANE_DIFF",
    );
    expect(laneDiffReport).toBeDefined();
    expect(laneDiffReport?.params).toEqual(
      jasmine.objectContaining({
        lane: 1,
        d1: "d1",
        count1: 2,
        heat1: "plural_heats",
        d2: "d2",
        count2: 0,
        heat2: "plural_heats",
      }),
    );
  });

  it("should return AM_REPORT_NO_DRIVERS if total assignments is 0", () => {
    const driverIds = ["d1", "d2"];
    const heats = [
      [null, null],
      [null, null],
    ];
    const result = checkLaneEquality(2, driverIds, heats);
    expect(result.allEqual).toBeFalse();
    expect(result.reports).toContain(
      jasmine.objectContaining({
        key: "AM_REPORT_NO_DRIVERS",
      }),
    );
  });
});
