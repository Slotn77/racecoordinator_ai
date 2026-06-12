import { Locator } from "@playwright/test";

import { ToolbarHarnessE2e } from "../../toolbar/testing/toolbar.harness.e2e";
import { ManagerHeaderHarnessBase } from "./manager-header.harness.base";

export class ManagerHeaderHarnessE2e implements ManagerHeaderHarnessBase {
  constructor(private locator: Locator) {}

  private get base() {
    return ManagerHeaderHarnessBase;
  }

  async getTitle(): Promise<string> {
    const title = this.locator.locator(this.base.selectors.title);
    return (await title.innerText()).trim();
  }

  async hasToolbar(): Promise<boolean> {
    const toolbar = this.locator.locator(this.base.selectors.toolbar);
    return await toolbar.isVisible();
  }

  async getToolbar(): Promise<ToolbarHarnessE2e> {
    return new ToolbarHarnessE2e(
      this.locator.locator(this.base.selectors.toolbar),
    );
  }
}
