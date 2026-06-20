import { Injectable, signal } from "@angular/core";

export const DEFAULT_FONTS = [
  "Orbitron",
  "Courier New",
  "Inter",
  "Georgia",
  "Arial",
  "Arial Black",
  "Helvetica",
  "Times New Roman",
  "Trebuchet MS",
  "Verdana",
  "Impact",
  "Comic Sans MS",
];

@Injectable({
  providedIn: "root",
})
export class FontService {
  private fontsList = signal<string[]>(DEFAULT_FONTS);
  readonly availableFonts = this.fontsList.asReadonly();
  private hasLoadedLocal = false;

  constructor() {
    this.tryLoadLocalFonts();
  }

  async loadLocalFonts(): Promise<void> {
    if (this.hasLoadedLocal) return;

    if ("queryLocalFonts" in window) {
      try {
        const queryLocalFontsFn = (window as any).queryLocalFonts;
        if (typeof queryLocalFontsFn === "function") {
          const localFonts = await queryLocalFontsFn();
          if (localFonts && localFonts.length > 0) {
            const families = new Set<string>(DEFAULT_FONTS);
            for (const font of localFonts) {
              if (font.family) {
                families.add(font.family);
              }
            }
            const sorted = Array.from(families).sort((a, b) =>
              a.localeCompare(b),
            );
            this.fontsList.set(sorted);
            this.hasLoadedLocal = true;
          }
        }
      } catch (err) {
        console.warn("Failed to query local fonts:", err);
      }
    }
  }

  private async tryLoadLocalFonts() {
    try {
      await this.loadLocalFonts();
    } catch {
      // Ignored
    }
  }
}
