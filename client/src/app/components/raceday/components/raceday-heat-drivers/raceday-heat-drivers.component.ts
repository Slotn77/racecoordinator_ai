import { CommonModule } from "@angular/common";
import { Component, computed, input, ViewEncapsulation } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { Track } from "@app/models/track";
import { TranslatePipe } from "@app/pipes/translate.pipe";
import { DriverHeatData } from "@app/race/driver_heat_data";
import { Heat } from "@app/race/heat";

@Component({
  standalone: true,
  selector: "app-raceday-heat-drivers",
  templateUrl: "./raceday-heat-drivers.component.html",
  styleUrls: ["./raceday-heat-drivers.component.css"],
  encapsulation: ViewEncapsulation.None,
  imports: [CommonModule, TranslatePipe, FormsModule],
})
export class RacedayHeatDriversComponent {
  type = input<"next-heat" | "on-deck">("next-heat");
  track = input<Track | undefined>(undefined);
  currentHeat = input<Heat | undefined>(undefined);
  heats = input<Heat[]>([]);
  parent = input<any>(undefined);
  widget = input<any>(undefined);

  nextHeatNumber = computed<number>(() => {
    const cur = this.currentHeat();
    return cur ? cur.heatNumber + 1 : 0;
  });

  drivers = computed<DriverHeatData[]>(() => {
    const cur = this.currentHeat();
    const hts = this.heats();
    if (!cur || !hts || hts.length === 0) {
      return [];
    }
    const nextHeat = hts.find((h) => h.heatNumber === cur.heatNumber + 1);
    if (!nextHeat || !nextHeat.heatDrivers) {
      return [];
    }
    const activeDrivers = nextHeat.heatDrivers.filter((hd) => {
      return hd.driver && !hd.driver.isEmpty();
    });

    if (this.type() === "on-deck") {
      const currentDriverIds = new Set(
        cur.heatDrivers
          ?.map((d) => d.driver?.objectId || d.driver?.entity_id)
          .filter(Boolean) || [],
      );
      return activeDrivers.filter((hd) => {
        const id = hd.driver.objectId || hd.driver.entity_id;
        return id && !currentDriverIds.has(id);
      });
    }

    return activeDrivers;
  });

  isTeam(hd: DriverHeatData): boolean {
    return this.parent()?.isTeam(hd) ?? false;
  }

  isEditMode(): boolean {
    const parent = this.parent();
    if (!parent) return false;
    const isCustomizing = parent.isLayoutCustomizing;
    const isUIEditor =
      typeof parent.isUIEditorMode === "function"
        ? parent.isUIEditorMode()
        : parent.isUIEditorMode;
    return !!(isCustomizing || isUIEditor);
  }

  getTeammates(hd: DriverHeatData): any[] {
    return this.parent()?.getTeammates(hd) ?? [];
  }

  getDropdownArrowBg(hd: DriverHeatData): string {
    if (!this.parent()) return "";
    const color = this.getLaneForegroundColor(hd.laneIndex);
    return this.parent().getDropdownIcon(color);
  }

  getDriverStats(hd: DriverHeatData, driverId: string): string {
    return this.parent()?.getDriverStats(hd, driverId) ?? "";
  }

  onTeammateChange(hd: DriverHeatData, event: any) {
    const nextHeatNum = this.nextHeatNumber();
    if (this.parent() && nextHeatNum > 0) {
      this.parent().onNextHeatTeammateChange(hd, event, nextHeatNum);
    }
  }

  getLaneBackgroundColor(laneIndex: number): string {
    return this.track()?.lanes?.[laneIndex]?.background_color || "#333333";
  }

  getLaneForegroundColor(laneIndex: number): string {
    return this.track()?.lanes?.[laneIndex]?.foreground_color || "#ffffff";
  }

  trackByDriver(index: number, hd: DriverHeatData): string {
    return hd.driver?.objectId || hd.driver?.entity_id || String(index);
  }
}
