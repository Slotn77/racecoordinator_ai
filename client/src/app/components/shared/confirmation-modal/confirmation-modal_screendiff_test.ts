import { expect, test } from "@playwright/test";
import { TestSetupHelper } from "@app/testing/test-setup_helper";

import { ConfirmationModalHarnessE2e } from "./testing/confirmation-modal.harness.e2e";

test.describe("Confirmation Modal Visuals", () => {
  test.beforeEach(async ({ page }) => {
    // Setup standard mocks
    await TestSetupHelper.setupStandardMocks(page);
    await TestSetupHelper.setupRaceWebSocketMocks(page);
    await TestSetupHelper.setupAssetMocks(page);
  });

  test("should display confirmation modal", async ({ page }) => {
    await TestSetupHelper.waitForLocalization(
      page,
      "en",
      page.goto("/raceday-setup"),
    );
    await page.evaluate(async () => {
      await (window as any).angularRouter.navigateByUrl("/raceday");
    });
    await page.locator(".menu-bar").waitFor({ state: "visible" });

    // Wait for the menu bar to ensure page loaded
    const menuBar = page.locator(".menu-bar");
    await menuBar.waitFor({ state: "visible" });

    // Trigger exit modal via browser back button
    await page.evaluate(() => window.history.back());

    // Wait for confirmation modal
    const modal = page.locator("app-confirmation-modal");
    const _harness = new ConfirmationModalHarnessE2e(modal);

    // Wait for confirmation modal
    await modal.locator(".modal-content").waitFor({ state: "visible" });

    // Screenshot the modal
    await expect(modal.locator(".modal-content")).toHaveScreenshot(
      "confirmation-modal.png",
    );
  });
});
