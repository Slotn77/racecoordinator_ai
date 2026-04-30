import { Locator } from "@playwright/test";

import { AudioSelectorHarnessBase } from "./audio-selector.harness.base";

export class AudioSelectorHarnessE2e implements AudioSelectorHarnessBase {
  constructor(private locator: Locator) {}

  private get base() {
    return AudioSelectorHarnessBase;
  }

  private get labelElement() {
    return this.locator.locator(this.base.selectors.label);
  }
  private get toggleSpans() {
    return this.locator.locator(this.base.selectors.toggleSpans);
  }
  private get selectWrapper() {
    return this.locator.locator(this.base.selectors.selectWrapper);
  }
  private get selectedSoundNameElement() {
    return this.locator.locator(this.base.selectors.selectedSoundName);
  }
  private get ttsInput() {
    return this.locator.locator(this.base.selectors.ttsInput);
  }
  private get playButton() {
    return this.locator.locator(this.base.selectors.playButton);
  }

  async getLabel(): Promise<string> {
    if (await this.labelElement.isVisible()) {
      return await this.labelElement.innerText();
    }
    return "";
  }

  async getAudioType(): Promise<"preset" | "tts" | "none" | "audio_set"> {
    const spans = await this.toggleSpans.all();
    for (const span of spans) {
      const classList = (await span.getAttribute("class")) || "";
      if (classList.includes("active")) {
        const id = (await span.getAttribute("id")) || "";
        // If we added IDs, that would be better. Let's check innerText as fallback
        const text = await span.innerText();
        if (text.match(/preset/i)) return "preset";
        if (text.match(/audio set/i)) return "audio_set";
        if (text.match(/tts|text/i)) return "tts";
        if (text.match(/none/i)) return "none";
      }
    }
    return "preset"; // Default fallback
  }

  async clickPresetType(): Promise<void> {
    await this.locator.locator("span", { hasText: /preset/i }).click();
  }

  async clickAudioSetType(): Promise<void> {
    await this.locator.locator("span", { hasText: /audio set/i }).click();
  }

  async clickTtsType(): Promise<void> {
    await this.locator.locator("span", { hasText: /tts|text/i }).click();
  }

  async clickNoneType(): Promise<void> {
    await this.locator.locator("span", { hasText: /none/i }).click();
  }

  async clickSelectSound(): Promise<void> {
    await this.selectWrapper.click();
  }

  async getSelectedSoundName(): Promise<string> {
    if (await this.selectedSoundNameElement.isVisible()) {
      const text = await this.selectedSoundNameElement.innerText();
      return text.trim();
    }
    return "";
  }

  async getTtsText(): Promise<string> {
    if (await this.ttsInput.isVisible()) {
      return await this.ttsInput.inputValue();
    }
    return "";
  }

  async setTtsText(text: string): Promise<void> {
    await this.ttsInput.fill(text);
  }

  async clickPlay(): Promise<void> {
    await this.playButton.click();
  }
}
