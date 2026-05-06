import { Locator } from "@playwright/test";

import { DatabaseManagerHarnessBase } from "./database-manager.harness.base";

export class DatabaseManagerHarnessE2e implements DatabaseManagerHarnessBase {
  constructor(private locator: Locator) {}

  private get base() {
    return DatabaseManagerHarnessBase;
  }

  private get listItems() {
    return this.locator.locator(this.base.selectors.listItem);
  }
  private get createBtn() {
    return this.locator.locator(this.base.selectors.createBtn);
  }
  private get importBtn() {
    return this.locator.locator(this.base.selectors.importBtn);
  }
  private get exportBtn() {
    return this.locator.locator(this.base.selectors.exportBtn);
  }
  private get copyBtn() {
    return this.locator.locator(this.base.selectors.copyBtn);
  }
  private get resetBtn() {
    return this.locator.locator(this.base.selectors.resetBtn);
  }
  private get deleteBtn() {
    return this.locator.locator(this.base.selectors.deleteBtn);
  }
  private get useBtn() {
    return this.locator.locator(this.base.selectors.useBtn);
  }
  private get detailHeader() {
    return this.locator.locator(this.base.selectors.detailHeader);
  }
  private get inputModal() {
    return this.locator.page().locator(this.base.selectors.modalBackdrop);
  }

  async waitForInputModalVisible(timeout = 10000): Promise<void> {
    await this.inputModal
      .locator(this.base.selectors.modalTitle)
      .waitFor({ state: "visible", timeout });
  }

  async getDatabaseCount(): Promise<number> {
    return await this.listItems.count();
  }

  async getDatabaseName(index: number): Promise<string> {
    return await this.listItems
      .nth(index)
      .locator(this.base.selectors.itemName)
      .innerText();
  }

  async selectDatabase(index: number): Promise<void> {
    await this.listItems.nth(index).click();
  }

  async getSelectedDatabaseName(): Promise<string | null> {
    if (await this.detailHeader.isVisible()) {
      return await this.detailHeader.innerText();
    }
    return null;
  }

  async clickCreateDatabase(): Promise<void> {
    await this.createBtn.click();
  }

  async clickImportDatabase(): Promise<void> {
    await this.importBtn.click();
  }

  async clickExportDatabase(): Promise<void> {
    await this.exportBtn.click();
  }

  async clickCopyDatabase(): Promise<void> {
    await this.copyBtn.click();
  }

  async clickResetDatabase(): Promise<void> {
    await this.resetBtn.click();
  }

  async clickDeleteDatabase(): Promise<void> {
    await this.deleteBtn.click();
  }

  async clickUseDatabase(): Promise<void> {
    await this.useBtn.click();
  }

  async isUseDatabaseEnabled(): Promise<boolean> {
    return await this.useBtn.isEnabled();
  }

  // Modal interactions
  async isInputModalVisible(): Promise<boolean> {
    return await this.inputModal.isVisible();
  }

  async getInputModalTitle(): Promise<string> {
    return await this.inputModal
      .locator(this.base.selectors.modalTitle)
      .innerText();
  }

  async setInputModalValue(value: string): Promise<void> {
    await this.inputModal.locator(this.base.selectors.input).fill(value);
  }

  async clickInputModalConfirm(): Promise<void> {
    await this.inputModal.locator(this.base.selectors.btnConfirm).click();
  }

  async isInputModalConfirmEnabled(): Promise<boolean> {
    return await this.inputModal
      .locator(this.base.selectors.btnConfirm)
      .isEnabled();
  }

  async isInputModalErrorVisible(): Promise<boolean> {
    return await this.inputModal
      .locator(this.base.selectors.errorMsg)
      .isVisible();
  }
}
