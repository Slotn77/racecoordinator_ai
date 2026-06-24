import { CommonModule } from "@angular/common";
import { Component, input, output } from "@angular/core";
import { AbsoluteWidgetNode } from "@app/models/settings";

import { ImageInspectorComponent } from "./image-inspector/image-inspector.component";
import { LaneViewInspectorComponent } from "./lane-view-inspector/lane-view-inspector.component";
import { LeaderboardInspectorComponent } from "./leaderboard-inspector/leaderboard-inspector.component";
import { MenuInspectorComponent } from "./menu-inspector/menu-inspector.component";
import { RecordsInspectorComponent } from "./records-inspector/records-inspector.component";
import { TextInfoInspectorComponent } from "./text-info-inspector/text-info-inspector.component";
import { TimerInspectorComponent } from "./timer-inspector/timer-inspector.component";
import { UpcomingInspectorComponent } from "./upcoming-inspector/upcoming-inspector.component";

@Component({
  standalone: true,
  selector: "app-widget-inspector-fields",
  templateUrl: "./widget-inspector-fields.component.html",
  imports: [
    CommonModule,
    LeaderboardInspectorComponent,
    LaneViewInspectorComponent,
    RecordsInspectorComponent,
    TimerInspectorComponent,
    ImageInspectorComponent,
    UpcomingInspectorComponent,
    TextInfoInspectorComponent,
    MenuInspectorComponent,
  ],
})
export class WidgetInspectorFieldsComponent {
  widget = input.required<AbsoluteWidgetNode>();
  change = output<void>();

  onSettingsChange() {
    this.change.emit();
  }
}
