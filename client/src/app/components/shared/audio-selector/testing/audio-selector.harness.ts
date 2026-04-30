import { ComponentHarness } from "@angular/cdk/testing";

import { AudioSelectorHarnessBase } from "./audio-selector.harness.base";

export class AudioSelectorHarness
  extends ComponentHarness
  implements AudioSelectorHarnessBase
{
  static hostSelector = AudioSelectorHarnessBase.hostSelector;

  protected getLabelElement = this.locatorForOptional(
    AudioSelectorHarnessBase.selectors.label,
  );
  protected getToggleSpans = this.locatorForAll(
    AudioSelectorHarnessBase.selectors.toggleSpans,
  );
  protected getSelectWrapper = this.locatorForOptional(
    AudioSelectorHarnessBase.selectors.selectWrapper,
  );
  protected getSelectedSoundNameElement = this.locatorForOptional(
    AudioSelectorHarnessBase.selectors.selectedSoundName,
  );
  protected getTtsInput = this.locatorForOptional(
    AudioSelectorHarnessBase.selectors.ttsInput,
  );
  protected getPlayButton = this.locatorForOptional(
    AudioSelectorHarnessBase.selectors.playButton,
  );

  async getLabel(): Promise<string> {
    const el = await this.getLabelElement();
    return el ? await el.text() : "";
  }

  async getAudioType(): Promise<"preset" | "tts" | "none" | "audio_set"> {
    const spans = await this.getToggleSpans();
    for (const span of spans) {
      if (await span.hasClass("active")) {
        const text = (await span.text()).toLowerCase();
        if (text.includes("set")) return "audio_set";
        if (text.includes("tts")) return "tts";
        if (text.includes("none")) return "none";
        return "preset";
      }
    }
    return "preset";
  }

  async clickPresetType(): Promise<void> {
    const spans = await this.getToggleSpans();
    if (spans.length >= 1) {
      await spans[0].click();
    }
  }

  async clickTtsType(): Promise<void> {
    const spans = await this.getToggleSpans();
    for (const span of spans) {
      const text = await span.text();
      if (text.includes("TTS")) {
        await span.click();
        return;
      }
    }
  }

  async clickAudioSetType(): Promise<void> {
    const spans = await this.getToggleSpans();
    for (const span of spans) {
      const text = await span.text();
      if (text.includes("Set")) {
        await span.click();
        return;
      }
    }
  }

  async clickNoneType(): Promise<void> {
    const spans = await this.getToggleSpans();
    for (const span of spans) {
      const text = await span.text();
      if (text.includes("None")) {
        await span.click();
        return;
      }
    }
  }

  async clickSelectSound(): Promise<void> {
    const wrapper = await this.getSelectWrapper();
    if (wrapper) await wrapper.click();
  }

  async getSelectedSoundName(): Promise<string> {
    const el = await this.getSelectedSoundNameElement();
    const text = el ? await el.text() : "";
    return text.trim();
  }

  async getTtsText(): Promise<string> {
    const input = await this.getTtsInput();
    return input ? await input.getProperty("value") : "";
  }

  async setTtsText(text: string): Promise<void> {
    const input = await this.getTtsInput();
    if (input) {
      await input.setInputValue(text);
      await input.dispatchEvent("input");
    }
  }

  async clickPlay(): Promise<void> {
    const btn = await this.getPlayButton();
    if (btn) await btn.click();
  }
}
