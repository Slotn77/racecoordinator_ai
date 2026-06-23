import { CommonModule } from "@angular/common";
import {
  AfterViewInit,
  Component,
  effect,
  ElementRef,
  input,
  OnDestroy,
  viewChild,
  ViewEncapsulation,
} from "@angular/core";
import { AbsoluteWidgetNode } from "@app/models/settings";
import { TranslatePipe } from "@app/pipes/translate.pipe";

@Component({
  standalone: true,
  selector: "app-raceday-timer",
  templateUrl: "./raceday-timer.component.html",
  styleUrls: ["./raceday-timer.component.css"],
  encapsulation: ViewEncapsulation.None,
  imports: [CommonModule, TranslatePipe],
})
export class RacedayTimerComponent implements AfterViewInit, OnDestroy {
  widget = input<AbsoluteWidgetNode | null>(null);
  formattedTime = input<string>("");
  autoStatusLabel = input<string>("");
  isWarmup = input<boolean>(false);
  showCountdownOverlay = input<boolean>(false);

  private timerText = viewChild<ElementRef<HTMLElement>>("timerText");
  private timerPanel = viewChild<ElementRef<HTMLElement>>("timerPanel");
  private resizeObserver?: ResizeObserver;

  constructor() {
    effect(() => {
      // Trigger whenever inputs change
      this.formattedTime();
      this.autoStatusLabel();
      this.isWarmup();
      this.widget();

      // Schedule fit on next microtask
      setTimeout(() => this.fitText(), 0);
    });
  }

  ngAfterViewInit() {
    const panelEl = this.timerPanel()?.nativeElement;
    if (panelEl && typeof ResizeObserver !== "undefined") {
      this.resizeObserver = new ResizeObserver(() => {
        this.fitText();
      });
      this.resizeObserver.observe(panelEl);
    }
  }

  ngOnDestroy() {
    if (this.resizeObserver) {
      this.resizeObserver.disconnect();
    }
  }

  private fitText() {
    const textEl = this.timerText()?.nativeElement;
    const panelEl = this.timerPanel()?.nativeElement;
    const widgetData = this.widget();

    if (!textEl || !panelEl) return;

    const isAuto = !widgetData || widgetData.scaleMode === "auto";
    if (!isAuto) {
      panelEl.style.removeProperty("--timer-font-size");
      return;
    }

    const textString = textEl.textContent?.trim() || "00:00";
    const style = window.getComputedStyle(textEl);
    const fontFamily = style.fontFamily || "'Courier New', Courier, monospace";
    const fontWeight = style.fontWeight || "bold";

    // Use canvas to measure text width without touching the DOM and causing layout thrashing
    const canvas = document.createElement("canvas");
    const context = canvas.getContext("2d");
    let textWidth = 100;
    if (context) {
      context.font = `${fontWeight} 100px ${fontFamily}`;
      textWidth = context.measureText(textString).width || 1;
    }

    // Height of text at 100px font-size is roughly 100px (line-height is 1)
    const textHeight = 100;

    // Pad container bounds (12% horizontal margin, 24% vertical margin to accommodate labels)
    const containerWidth = panelEl.clientWidth * 0.88;
    const containerHeight = panelEl.clientHeight * 0.76;

    const scaleX = containerWidth / textWidth;
    const scaleY = containerHeight / textHeight;
    const scale = Math.min(scaleX, scaleY);

    const baseScaleFactor = widgetData?.textScaleFactor ?? 1;
    const targetFontSize = Math.floor(100 * scale * baseScaleFactor);

    // Apply via CSS custom property so children scale too
    panelEl.style.setProperty("--timer-font-size", `${targetFontSize}px`);
  }
}
