import { ToolbarHarnessBase } from "../../toolbar/testing/toolbar.harness.base";

export abstract class ManagerHeaderHarnessBase {
  static readonly hostSelector = "app-manager-header";

  static readonly selectors = {
    title: ".page-title",
    toolbar: ToolbarHarnessBase.hostSelector,
  };

  abstract getTitle(): Promise<string>;
  abstract hasToolbar(): Promise<boolean>;
  abstract getToolbar(): Promise<any>;
}
