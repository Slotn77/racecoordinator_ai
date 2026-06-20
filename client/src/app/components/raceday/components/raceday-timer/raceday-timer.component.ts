import { CommonModule } from "@angular/common";
import { Component, input, ViewEncapsulation } from "@angular/core";
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
export class RacedayTimerComponent {
  widget = input<AbsoluteWidgetNode | null>(null);
  formattedTime = input<string>("");
  autoStatusLabel = input<string>("");
  isWarmup = input<boolean>(false);
  showCountdownOverlay = input<boolean>(false);
}
