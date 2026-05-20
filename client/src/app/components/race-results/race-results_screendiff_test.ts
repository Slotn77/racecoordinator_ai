import { expect, test } from "@playwright/test";
import { RaceData } from "@app/proto/antigravity";
import { TestSetupHelper } from "@app/testing/test-setup_helper";

test.describe("Race Results Visuals", () => {
  test.beforeEach(async ({ page }) => {
    await TestSetupHelper.setupStandardMocks(page);
    await TestSetupHelper.disableAnimations(page);
    await page.setViewportSize({ width: 1600, height: 900 });
  });

  test("should render initial standings table and SVG graphs", async ({
    page,
  }) => {
    const mockData = createMockRaceData();
    await injectMockRaceData(page, mockData);

    await TestSetupHelper.waitForLocalization(
      page,
      "en",
      page.goto("/race-results"),
    );

    // Verify page has finished loading
    await expect(page.locator(".results-table-body")).toBeVisible();
    await expect(page.locator(".results-svg-graphs")).toBeVisible();

    // Verify initial layout screenshot
    await expect(page).toHaveScreenshot("race-results-initial.png", {
      maxDiffPixelRatio: 0.05,
    });
  });

  test("should toggle driver visibility on single-click legend item", async ({
    page,
  }) => {
    const mockData = createMockRaceData();
    await injectMockRaceData(page, mockData);

    await TestSetupHelper.waitForLocalization(
      page,
      "en",
      page.goto("/race-results"),
    );

    // Initial check: 3 legend items visible
    await expect(page.locator(".legend-item")).toHaveCount(3);

    // Click "Alice" legend item to toggle her visibility off
    const aliceLegend = page
      .locator(".legend-item")
      .filter({ hasText: "Alice" });
    await aliceLegend.click();

    // Small delay to let the click timeout run and the path transitions complete (or animations are disabled)
    await page.waitForTimeout(400);

    // Verify Alice is visually hidden (grayed out legend)
    await expect(page).toHaveScreenshot("race-results-alice-hidden.png", {
      maxDiffPixelRatio: 0.05,
    });

    // Click again to toggle Alice back on
    await aliceLegend.click();
    await page.waitForTimeout(400);

    await expect(page).toHaveScreenshot("race-results-alice-restored.png", {
      maxDiffPixelRatio: 0.05,
    });
  });

  test("should solo driver on double-click legend item", async ({ page }) => {
    const mockData = createMockRaceData();
    await injectMockRaceData(page, mockData);

    await TestSetupHelper.waitForLocalization(
      page,
      "en",
      page.goto("/race-results"),
    );

    // Double-click "Bob" legend item to solo Bob
    const bobLegend = page.locator(".legend-item").filter({ hasText: "Bob" });
    await bobLegend.dblclick();

    await page.waitForTimeout(400);

    // Verify only Bob's graph line is visible, other lines hidden
    await expect(page).toHaveScreenshot("race-results-bob-soloed.png", {
      maxDiffPixelRatio: 0.05,
    });

    // Double-click Bob again to toggle back to showing all
    await bobLegend.dblclick();
    await page.waitForTimeout(400);

    await expect(page).toHaveScreenshot("race-results-all-restored.png", {
      maxDiffPixelRatio: 0.05,
    });
  });

  test("should highlight driver graph when hovering over a name on the legend", async ({
    page,
  }) => {
    const mockData = createMockRaceData();
    await injectMockRaceData(page, mockData);

    await TestSetupHelper.waitForLocalization(
      page,
      "en",
      page.goto("/race-results"),
    );

    // Hover over the "Bob" legend item
    const bobLegend = page.locator(".legend-item").filter({ hasText: "Bob" });
    await bobLegend.hover();

    await page.waitForTimeout(400);

    // Verify Bob's graph is highlighted, and others are faded
    await expect(page).toHaveScreenshot("race-results-bob-hovered.png", {
      maxDiffPixelRatio: 0.05,
    });
  });
});

// Helper to construct fully compliant mock RaceData structure
function createMockRaceData() {
  return {
    race: {
      race: {
        model: { entityId: "r1" },
        name: "Screendiff Grand Prix",
        track: {
          model: { entityId: "t1" },
          name: "Screendiff Raceway",
          lanes: [
            { backgroundColor: "#ef4444", foregroundColor: "#ffffff" },
            { backgroundColor: "#3b82f6", foregroundColor: "#ffffff" },
            { backgroundColor: "#10b981", foregroundColor: "#ffffff" },
          ],
        },
      },
      drivers: [
        {
          objectId: "rp1",
          rank: 1,
          totalLaps: 3,
          totalTime: 31.1,
          bestLapTime: 10.2,
          averageLapTime: 10.366,
          medianLapTime: 10.4,
          rankValue: 100,
          seed: 1,
          driver: {
            model: { entityId: "d1" },
            name: "Alice",
            nickname: "Alice",
          },
        },
        {
          objectId: "rp2",
          rank: 2,
          totalLaps: 3,
          totalTime: 32.6,
          bestLapTime: 10.7,
          averageLapTime: 10.866,
          medianLapTime: 10.8,
          rankValue: 80,
          seed: 2,
          driver: {
            model: { entityId: "d2" },
            name: "Bob",
            nickname: "Bob",
          },
        },
        {
          objectId: "rp3",
          rank: 3,
          totalLaps: 3,
          totalTime: 34.7,
          bestLapTime: 11.2,
          averageLapTime: 11.566,
          medianLapTime: 11.5,
          rankValue: 60,
          seed: 3,
          driver: {
            model: { entityId: "d3" },
            name: "Charlie",
            nickname: "Charlie",
          },
        },
      ],
      heats: [
        {
          objectId: "h1",
          heatNumber: 1,
          started: true,
          heatDrivers: [
            {
              objectId: "hd1",
              driver: {
                objectId: "rp1",
                driver: { model: { entityId: "d1" }, name: "Alice" },
              },
              actualDriver: { model: { entityId: "d1" }, name: "Alice" },
              laps: [{ lapTime: 10.5 }, { lapTime: 10.2 }, { lapTime: 10.4 }],
            },
            {
              objectId: "hd2",
              driver: {
                objectId: "rp2",
                driver: { model: { entityId: "d2" }, name: "Bob" },
              },
              actualDriver: { model: { entityId: "d2" }, name: "Bob" },
              laps: [{ lapTime: 11.1 }, { lapTime: 10.8 }, { lapTime: 10.7 }],
            },
            {
              objectId: "hd3",
              driver: {
                objectId: "rp3",
                driver: { model: { entityId: "d3" }, name: "Charlie" },
              },
              actualDriver: { model: { entityId: "d3" }, name: "Charlie" },
              laps: [{ lapTime: 12.0 }, { lapTime: 11.2 }, { lapTime: 11.5 }],
            },
          ],
        },
      ],
    },
  };
}

// Inject helper
async function injectMockRaceData(page: any, mockData: any) {
  const buffer = RaceData.encode(mockData).finish();
  const dataArray = Array.from(buffer);
  await page.addInitScript((data: number[]) => {
    // @ts-ignore
    window.mockRaceDataBuffer = new Uint8Array(data).buffer;
  }, dataArray);
}
