import { ComponentHarness } from "@angular/cdk/testing";

import { HeatResultsHarnessBase } from "./heat-results.harness.base";

export class HeatResultsHarness
  extends ComponentHarness
  implements HeatResultsHarnessBase
{
  static hostSelector = HeatResultsHarnessBase.hostSelector;

  protected getHeatDriverExpandersEl = this.locatorForAll(
    HeatResultsHarnessBase.selectors.heatDriverExpander,
  );
  protected getTwinGraphsEl = this.locatorForOptional(
    HeatResultsHarnessBase.selectors.twinGraphs,
  );

  async hasHeatDriverExpander(): Promise<boolean> {
    return (await this.getHeatDriverExpandersEl()).length > 0;
  }

  async hasTwinGraphs(): Promise<boolean> {
    return (await this.getTwinGraphsEl()) !== null;
  }

  async getHeatDriverExpanderCount(): Promise<number> {
    return (await this.getHeatDriverExpandersEl()).length;
  }
}
