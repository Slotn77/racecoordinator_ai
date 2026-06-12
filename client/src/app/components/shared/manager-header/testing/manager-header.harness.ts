import { ComponentHarness } from "@angular/cdk/testing";

import { ToolbarHarness } from "../../toolbar/testing/toolbar.harness";
import { ManagerHeaderHarnessBase } from "./manager-header.harness.base";

export class ManagerHeaderHarness
  extends ComponentHarness
  implements ManagerHeaderHarnessBase
{
  static hostSelector = ManagerHeaderHarnessBase.hostSelector;

  protected getTitleText = this.locatorFor(
    ManagerHeaderHarnessBase.selectors.title,
  );

  async getTitle(): Promise<string> {
    const title = await this.getTitleText();
    return (await title.text()).trim();
  }

  async hasToolbar(): Promise<boolean> {
    const toolbars = await this.locatorForAll(
      ManagerHeaderHarnessBase.selectors.toolbar,
    )();
    return toolbars.length > 0;
  }

  async getToolbar(): Promise<ToolbarHarness> {
    return this.locatorFor(ToolbarHarness)();
  }
}
