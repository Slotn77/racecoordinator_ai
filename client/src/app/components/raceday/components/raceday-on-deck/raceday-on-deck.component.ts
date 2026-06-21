import { Component, input, ViewEncapsulation } from "@angular/core";
import { RacedayHeatDriversComponent } from "@app/components/raceday/components/raceday-heat-drivers/raceday-heat-drivers.component";
import { Track } from "@app/models/track";
import { Heat } from "@app/race/heat";

@Component({
  standalone: true,
  selector: "app-raceday-on-deck",
  templateUrl: "./raceday-on-deck.component.html",
  encapsulation: ViewEncapsulation.None,
  imports: [RacedayHeatDriversComponent],
})
export class RacedayOnDeckComponent {
  track = input<Track | undefined>(undefined);
  currentHeat = input<Heat | undefined>(undefined);
  heats = input<Heat[]>([]);
  parent = input<any>(undefined);
  widget = input<any>(undefined);
}
