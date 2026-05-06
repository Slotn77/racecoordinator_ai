import { ComponentHarness } from "@angular/cdk/testing";

import { DatabaseManagerHarnessBase } from "./database-manager.harness.base";

export class DatabaseManagerHarness
  extends ComponentHarness
  implements DatabaseManagerHarnessBase
{
  static hostSelector = DatabaseManagerHarnessBase.hostSelector;

  private get base() {
    return DatabaseManagerHarnessBase;
  }

  protected getListItems = this.locatorForAll(
    DatabaseManagerHarnessBase.selectors.listItem,
  );
  protected getListItemNames = this.locatorForAll(
    `${DatabaseManagerHarnessBase.selectors.listItem} ${DatabaseManagerHarnessBase.selectors.itemName}`,
  );
  protected getCreateBtn = this.locatorFor(
    DatabaseManagerHarnessBase.selectors.createBtn,
  );
  protected getImportBtn = this.locatorFor(
    DatabaseManagerHarnessBase.selectors.importBtn,
  );
  protected getExportBtn = this.locatorFor(
    DatabaseManagerHarnessBase.selectors.exportBtn,
  );
  protected getCopyBtn = this.locatorFor(
    DatabaseManagerHarnessBase.selectors.copyBtn,
  );
  protected getResetBtn = this.locatorFor(
    DatabaseManagerHarnessBase.selectors.resetBtn,
  );
  protected getDeleteBtn = this.locatorFor(
    DatabaseManagerHarnessBase.selectors.deleteBtn,
  );
  protected getUseBtn = this.locatorFor(
    DatabaseManagerHarnessBase.selectors.useBtn,
  );
  protected getDetailHeader = this.locatorForOptional(
    DatabaseManagerHarnessBase.selectors.detailHeader,
  );
  protected getInputModal = this.locatorForOptional(
    DatabaseManagerHarnessBase.selectors.modalBackdrop,
  );
  protected getModalTitle = this.locatorForOptional(
    `${DatabaseManagerHarnessBase.selectors.modalBackdrop} ${DatabaseManagerHarnessBase.selectors.modalTitle}`,
  );
  protected getModalInput = this.locatorForOptional(
    `${DatabaseManagerHarnessBase.selectors.modalBackdrop} ${DatabaseManagerHarnessBase.selectors.input}`,
  );
  protected getModalBtnConfirm = this.locatorForOptional(
    `${DatabaseManagerHarnessBase.selectors.modalBackdrop} ${DatabaseManagerHarnessBase.selectors.btnConfirm}`,
  );
  protected getModalErrorMsg = this.locatorForOptional(
    `${DatabaseManagerHarnessBase.selectors.modalBackdrop} ${DatabaseManagerHarnessBase.selectors.errorMsg}`,
  );

  async getDatabaseCount(): Promise<number> {
    return (await this.getListItems()).length;
  }

  async getDatabaseName(index: number): Promise<string> {
    const names = await this.getListItemNames();
    if (index < names.length) {
      return await names[index].text();
    }
    return "";
  }

  async selectDatabase(index: number): Promise<void> {
    const items = await this.getListItems();
    if (index < items.length) {
      await items[index].click();
    }
  }

  async getSelectedDatabaseName(): Promise<string | null> {
    const header = await this.getDetailHeader();
    return header ? await header.text() : null;
  }

  async clickCreateDatabase(): Promise<void> {
    await (await this.getCreateBtn()).click();
  }

  async clickImportDatabase(): Promise<void> {
    await (await this.getImportBtn()).click();
  }

  async clickExportDatabase(): Promise<void> {
    await (await this.getExportBtn()).click();
  }

  async clickCopyDatabase(): Promise<void> {
    await (await this.getCopyBtn()).click();
  }

  async clickResetDatabase(): Promise<void> {
    await (await this.getResetBtn()).click();
  }

  async clickDeleteDatabase(): Promise<void> {
    await (await this.getDeleteBtn()).click();
  }

  async clickUseDatabase(): Promise<void> {
    await (await this.getUseBtn()).click();
  }

  async isUseDatabaseEnabled(): Promise<boolean> {
    const btn = await this.getUseBtn();
    return !((await btn.getAttribute("disabled")) === ""); // Or similar check
  }

  // Modal interactions
  async isInputModalVisible(): Promise<boolean> {
    return (await this.getInputModal()) !== null;
  }

  async getInputModalTitle(): Promise<string> {
    const title = await this.getModalTitle();
    return title ? await title.text() : "";
  }

  async setInputModalValue(value: string): Promise<void> {
    const input = await this.getModalInput();
    if (input) {
      // CDK way to set value or simulate typing
      await input.clear();
      await input.sendKeys(value);
    }
  }

  async clickInputModalConfirm(): Promise<void> {
    const btn = await this.getModalBtnConfirm();
    if (btn) {
      await btn.click();
    }
  }

  async isInputModalConfirmEnabled(): Promise<boolean> {
    const btn = await this.getModalBtnConfirm();
    if (btn) {
      return !((await btn.getAttribute("disabled")) === "");
    }
    return false;
  }

  async isInputModalErrorVisible(): Promise<boolean> {
    const error = await this.getModalErrorMsg();
    if (error) {
      return (await error.getAttribute("style")) === "visibility: visible";
    }
    return false;
  }
}
