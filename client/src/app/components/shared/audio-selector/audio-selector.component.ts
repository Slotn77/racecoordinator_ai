import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  Output,
} from "@angular/core";
import { DataService } from "src/app/data.service";
import { TranslationService } from "src/app/services/translation.service";
import { mockTTSContext, playSound } from "src/app/utils/audio";

@Component({
  selector: "app-audio-selector",
  templateUrl: "./audio-selector.component.html",
  styleUrls: ["./audio-selector.component.css"],
  standalone: false,
})
export class AudioSelectorComponent {
  @Input() label: string = "Audio";
  @Input() type: "preset" | "tts" | "none" | "audio_set" | undefined = "preset";
  @Input() mode: "single" | "set" = "single";
  @Input() readonly: boolean = false;

  @Output() typeChange = new EventEmitter<
    "preset" | "tts" | "none" | "audio_set"
  >();

  @Input() url?: string;
  @Output() urlChange = new EventEmitter<string>();

  @Input() assetId?: string;

  @Input() text?: string;
  @Output() textChange = new EventEmitter<string>();

  @Output() assetSelected = new EventEmitter<any>();

  @Input() assets: any[] = [];

  // Back button configuration passed through to Item Selector
  @Input() backButtonRoute: string | null = null;
  @Input() backButtonQueryParams: any = {};
  @Input() context?: any;

  showItemSelector = false;

  get filteredAssets(): any[] {
    if (this.mode === "set") {
      return this.assets.filter((a) => a.type === "audio_set");
    }
    return this.assets.filter((a) => a.type !== "audio_set");
  }

  get selectedAssetName(): string {
    if (this.type === "none") {
      return this.translationService
        ? this.translationService.translate("AS_OPTION_NONE")
        : "None";
    }
    const lookupValue = this.assetId || this.url;
    if (!lookupValue)
      return this.translationService
        ? this.translationService.translate("AS_SELECT_SOUND")
        : "Select Sound...";

    const normalize = (u: string) => {
      if (!u) return "";
      // Extract the path after /api/ if it exists, otherwise return as is
      const apiIndex = u.indexOf("/api/");
      if (apiIndex !== -1) {
        return u.substring(apiIndex);
      }
      return u;
    };

    const normalizedLookup = normalize(lookupValue);

    const asset = this.assets.find((a) => {
      if (a.model?.entityId === lookupValue || a.entity_id === lookupValue)
        return true;
      if (normalize(a.url) === normalizedLookup) return true;
      return false;
    });

    return asset
      ? asset.name
      : this.translationService
        ? this.translationService.translate("AS_UNKNOWN_ASSET")
        : "Unknown Asset";
  }

  constructor(
    private dataService: DataService,
    private cdr: ChangeDetectorRef,
    private translationService: TranslationService,
  ) {}

  onTypeChange(newType: "preset" | "tts" | "none" | "audio_set" | undefined) {
    if (newType) {
      this.type = newType;
      this.typeChange.emit(this.type);
    }
  }

  onUrlChange(newUrl: string) {
    this.url = newUrl;
    this.urlChange.emit(this.url);
  }

  onTextChange(newText: string) {
    this.text = newText;
    this.textChange.emit(this.text);
  }

  openItemSelector() {
    this.showItemSelector = true;
  }

  closeItemSelector() {
    this.showItemSelector = false;
  }

  onAssetSelected(asset: any) {
    if (!asset) return;

    // Prevent cross-mode selection
    if (this.mode === "set" && asset.type !== "audio_set") return;
    if (this.mode === "single" && asset.type === "audio_set") return;

    const val =
      asset?.model?.entityId || asset?.entity_id || asset?.url || asset?.id;
    if (val) {
      this.onUrlChange(val);
      this.assetSelected.emit(asset);
      const targetType = asset.type === "audio_set" ? "audio_set" : "preset";
      if (this.type !== targetType) {
        this.onTypeChange(targetType);
      }
    }
    this.closeItemSelector();
  }

  onPlayPreview(item: any) {
    const playContext = this.context || mockTTSContext();
    playSound(
      item.type === "audio_set" ? "audio_set" : "preset",
      item.url || item.model?.entityId || item.entity_id,
      "",
      this.dataService.serverUrl,
      playContext,
    );
  }

  play() {
    if (this.type === "none") return;
    const playContext = this.context || mockTTSContext();
    playSound(
      this.type,
      this.url,
      this.text,
      this.dataService.serverUrl,
      playContext,
    );
  }

  // Drag & Drop
  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();

    const data = event.dataTransfer?.getData("application/json");
    if (data) {
      try {
        const asset = JSON.parse(data);
        this.onAssetSelected(asset);
      } catch (e) {
        console.error("Failed to parse dropped asset data", e);
      }
    }
  }
}
