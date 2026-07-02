export abstract class HeatResultsHarnessBase {
  static readonly hostSelector = "app-heat-results";

  static readonly selectors = {
    heatDriverExpander: "app-heat-driver-expander",
    twinGraphs: "app-twin-graphs",
    legendItem: ".legend-item",
    loaderOverlay: ".loader-overlay",
  };

  abstract hasHeatDriverExpander(): Promise<boolean>;
  abstract hasTwinGraphs(): Promise<boolean>;
  abstract getHeatDriverExpanderCount(): Promise<number>;
}
