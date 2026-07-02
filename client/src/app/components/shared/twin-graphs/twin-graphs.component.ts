/* eslint-disable no-restricted-syntax */
import { CommonModule, DecimalPipe } from "@angular/common";
import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
} from "@angular/core";
import { TranslatePipe } from "@app/pipes/translate.pipe";

export interface GraphPoint {
  x: number;
  y: number;
  isOwnLap?: boolean;
  causesStandingsChange?: boolean;
}

export interface DriverLine {
  objectId: string;
  driverId?: string;
  driverName: string;
  color: string;
  backgroundColor: string;
  points: GraphPoint[];
  pathData: string;
  rankPoints: GraphPoint[];
  rankPathData: string;
  legendX?: number;
  legendY?: number;
}

@Component({
  selector: "app-twin-graphs",
  standalone: true,
  imports: [CommonModule, DecimalPipe, TranslatePipe],
  templateUrl: "./twin-graphs.component.html",
  styleUrls: ["./twin-graphs.component.css"],
})
export class TwinGraphsComponent implements OnChanges {
  @Input() driverLines: DriverLine[] = [];
  @Input() xTicks: number[] = [];
  @Input() yTicksLeft: number[] = [];
  @Input() yTicks: number[] = [];

  @Input() width = 1400;
  @Input() height = 750;
  @Input() padding = { top: 80, right: 100, bottom: 150, left: 100 };
  @Input() maxX = 10;
  @Input() maxY = 5;

  @Input() titleKey = "OR_TITLE";
  @Input() titleParams: any = {};
  @Input() leftTitleKey = "OR_RANKINGS_HEADER";
  @Input() rightTitleKey = "HR_LAP_TIMES_HEADER";
  @Input() leftXAxisKey = "OR_X_AXIS";
  @Input() rightXAxisKey = "OR_X_AXIS";
  @Input() leftYAxisKey = "HR_HEAT_POSITION";
  @Input() rightYAxisKey = "HR_Y_AXIS";

  @Output() driverClick = new EventEmitter<string>();

  hoveredDriverId: string | null = null;
  hiddenDriverIds = new Set<string>();

  ngOnChanges(changes: SimpleChanges) {
    if (
      changes["driverLines"] ||
      changes["width"] ||
      changes["height"] ||
      changes["padding"] ||
      changes["maxX"] ||
      changes["maxY"]
    ) {
      this.calculateLegendPositions();
      this.generatePaths();
    }
  }

  private generatePaths() {
    if (!this.driverLines) return;
    this.driverLines.forEach((line) => {
      if (line.points && line.points.length > 0) {
        line.pathData = this.generatePath(line.points, false);
      }
      if (line.rankPoints && line.rankPoints.length > 0) {
        line.rankPathData = this.generatePath(line.rankPoints, true);
      }
    });
  }

  private generatePath(points: GraphPoint[], isRank: boolean): string {
    if (!points || points.length === 0) return "";
    return points
      .map((point, index) => {
        const x = isRank ? this.scaleXLeft(point.x) : this.scaleXRight(point.x);
        const y = isRank ? this.scaleYLeft(point.y) : this.scaleY(point.y);
        return (index === 0 ? "M" : "L") + ` ${x} ${y}`;
      })
      .join(" ");
  }

  private calculateLegendPositions() {
    if (!this.driverLines) return;
    const legendItemWidth = 180;
    const legendItemHeight = 30;
    const maxLegendWidth = this.width - 40; // 20px padding on each side
    const itemsPerRow = Math.max(
      1,
      Math.floor(maxLegendWidth / legendItemWidth),
    );
    const N = this.driverLines.length;

    this.driverLines.forEach((line, i) => {
      const r = Math.floor(i / itemsPerRow);
      const col = i % itemsPerRow;

      const startIdx = r * itemsPerRow;
      const endIdx = Math.min(N, (r + 1) * itemsPerRow);
      const itemsInRow = endIdx - startIdx;
      const rowStartX = (this.width - itemsInRow * legendItemWidth) / 2;

      line.legendX = rowStartX + col * legendItemWidth;
      line.legendY =
        this.height - this.padding.bottom + 85 + r * legendItemHeight;
    });
  }

  protected scaleXLeft(x: number): number {
    const graphWidth =
      (this.width - this.padding.left - this.padding.right - 80) / 2;
    return this.padding.left + (x / this.maxX) * graphWidth;
  }

  protected scaleXRight(x: number): number {
    const graphWidth =
      (this.width - this.padding.left - this.padding.right - 80) / 2;
    const offset = this.padding.left + graphWidth + 80;
    return offset + (x / this.maxX) * graphWidth;
  }

  protected scaleYLeft(rank: number): number {
    const graphHeight = this.height - this.padding.top - this.padding.bottom;
    const N = this.driverLines.length || 4;
    if (N <= 1) return this.padding.top + graphHeight / 2;
    return this.padding.top + ((rank - 1) / (N - 1)) * graphHeight;
  }

  protected scaleY(y: number): number {
    const graphHeight = this.height - this.padding.top - this.padding.bottom;
    return this.height - this.padding.bottom - (y / this.maxY) * graphHeight;
  }

  trackByDriverLine(index: number, line: DriverLine): string {
    return line.objectId;
  }

  isDriverVisible(objectId: string): boolean {
    return !this.hiddenDriverIds.has(objectId);
  }

  onLegendClick(objectId: string) {
    if (this.hiddenDriverIds.has(objectId)) {
      this.hiddenDriverIds.delete(objectId);
    } else {
      this.hiddenDriverIds.add(objectId);
    }
  }

  onLegendDblClick(objectId: string) {
    if (
      this.hiddenDriverIds.size === this.driverLines.length - 1 &&
      !this.hiddenDriverIds.has(objectId)
    ) {
      this.hiddenDriverIds.clear();
    } else {
      this.driverLines.forEach((line) => {
        if (line.objectId !== objectId) {
          this.hiddenDriverIds.add(line.objectId);
        }
      });
      this.hiddenDriverIds.delete(objectId);
    }
  }
}
