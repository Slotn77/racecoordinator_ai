import { TestBed } from "@angular/core/testing";

import { DEFAULT_FONTS, FontService } from "./font.service";

describe("FontService", () => {
  let service: FontService;

  beforeEach(() => {
    // Delete queryLocalFonts if it was mocked in a previous run
    if ("queryLocalFonts" in window) {
      delete (window as any).queryLocalFonts;
    }

    TestBed.configureTestingModule({
      providers: [FontService],
    });
  });

  it("should be created with default fonts", () => {
    service = TestBed.inject(FontService);
    expect(service).toBeTruthy();
    expect(service.availableFonts()).toEqual(DEFAULT_FONTS);
  });

  it("should load local fonts if queryLocalFonts is available", async () => {
    const mockLocalFonts = [
      { family: "Zeta Font" },
      { family: "Arial" }, // Duplicate, should be de-duplicated
      { family: "Alpha Font" },
    ];

    (window as any).queryLocalFonts = jasmine
      .createSpy("queryLocalFonts")
      .and.resolveTo(mockLocalFonts);

    service = TestBed.inject(FontService);
    await service.loadLocalFonts();

    const result = service.availableFonts();
    expect(result).toContain("Zeta Font");
    expect(result).toContain("Alpha Font");
    expect(result.indexOf("Alpha Font")).toBeLessThan(
      result.indexOf("Zeta Font"),
    );
    // Checking it is sorted
    const sorted = [...result].sort((a, b) => a.localeCompare(b));
    expect(result).toEqual(sorted);
  });

  it("should not crash and keep default fonts if queryLocalFonts throws an error", async () => {
    (window as any).queryLocalFonts = jasmine
      .createSpy("queryLocalFonts")
      .and.rejectWith(new Error("Permission denied"));

    service = TestBed.inject(FontService);
    await service.loadLocalFonts();

    expect(service.availableFonts()).toEqual(DEFAULT_FONTS);
  });
});
