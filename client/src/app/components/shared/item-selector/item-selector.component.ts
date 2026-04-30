import { Component, EventEmitter, Input, Output } from "@angular/core";
import { Router } from "@angular/router";

@Component({
  selector: "app-item-selector",
  standalone: false,
  templateUrl: "./item-selector.component.html",
  styleUrls: ["./item-selector.component.css"],
})
export class ItemSelectorComponent {
  @Input() visible = false;
  @Input() title?: string;
  @Input() items: any[] = [];
  searchTerm: string = "";

  @Input() itemType: "image" | "sound" | "image_set" | "audio" | "audio_set" =
    "image";

  @Input() backButtonRoute: string | null = null;
  @Input() backButtonQueryParams: any = {};
  @Input() backButtonLabel?: string;

  get filteredItems() {
    let results = this.items;

    // Filter by type if itemType is specified
    if (this.itemType) {
      if (this.itemType === "audio") {
        results = results.filter(
          (item) => item.type === "sound" || item.type === "audio_set",
        );
      } else {
        results = results.filter((item) => item.type === this.itemType);
      }
    }

    if (!this.searchTerm) {
      return results;
    }

    const lowerTerm = this.searchTerm.toLowerCase();
    return results.filter(
      (item) => item.name && item.name.toLowerCase().includes(lowerTerm),
    );
  }

  @Output() select = new EventEmitter<any>();
  @Output() play = new EventEmitter<any>();
  @Output() close = new EventEmitter<void>();

  constructor(private router: Router) {}

  onBack() {
    if (this.backButtonRoute) {
      this.router.navigate([this.backButtonRoute], {
        queryParams: this.backButtonQueryParams,
      });
    }
    this.close.emit();
  }

  onSelect(item: any) {
    this.select.emit(item);
  }

  onPlay(event: MouseEvent, item: any) {
    event.stopPropagation();
    event.stopImmediatePropagation();
    this.play.emit(item);
  }

  onClose() {
    this.close.emit();
  }
}
