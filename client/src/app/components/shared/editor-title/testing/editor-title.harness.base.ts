export abstract class EditorTitleHarnessBase {
  static readonly hostSelector = "app-editor-title";

  static readonly selectors = {
    title: ".page-title",
    undoButton: "app-toolbar .undo",
    redoButton: "app-toolbar .redo",
    helpButton: "app-toolbar #help-track-btn",
  };

  /** Gets the title text */
  abstract getTitle(): Promise<string | null>;

  /** Clicks the undo button */
  abstract clickUndo(): Promise<void>;

  /** Clicks the redo button */
  abstract clickRedo(): Promise<void>;

  /** Clicks the help button */
  abstract clickHelp(): Promise<void>;

  /** Checks if undo is disabled */
  abstract isUndoDisabled(): Promise<boolean>;

  /** Checks if redo is disabled */
  abstract isRedoDisabled(): Promise<boolean>;
}
