import { FileSystemService } from "@app/services/file-system.service";

import { LogAppender, LogEntry, LogLevel } from "./log-appender";

export class FileAppender implements LogAppender {
  private readonly LOG_FILENAME = "race_coordinator_client.log";
  private buffer: string[] = [];
  private isProcessing = false;
  private channel: BroadcastChannel;
  private initialized = false;

  constructor(private fileSystemService: FileSystemService) {
    this.channel = new BroadcastChannel("rc_logging_channel");
    this.checkIfFirstTab();
  }

  private checkIfFirstTab(): void {
    let peerResponded = false;

    this.channel.onmessage = (event) => {
      if (event.data === "PING") {
        this.channel.postMessage("PONG");
      } else if (event.data === "PONG") {
        peerResponded = true;
      }
    };

    this.channel.postMessage("PING");

    setTimeout(async () => {
      if (!peerResponded) {
        await this.fileSystemService.deleteFile(this.LOG_FILENAME);
      }
      this.initialized = true;
      this.processBuffer();
    }, 100);
  }

  append(entry: LogEntry): void {
    const timestampStr = entry.timestamp.toISOString();
    const levelStr = LogLevel[entry.level];
    const argsStr =
      entry.args && entry.args.length > 0
        ? " " +
          entry.args
            .map((a) => (typeof a === "object" ? JSON.stringify(a) : a))
            .join(" ")
        : "";
    const logLine = `[${timestampStr}] [${levelStr}] ${entry.message}${argsStr}\n`;

    this.buffer.push(logLine);
    if (this.initialized) {
      this.processBuffer();
    }
  }

  private async processBuffer(): Promise<void> {
    if (this.isProcessing || this.buffer.length === 0) return;

    this.isProcessing = true;
    try {
      const content = this.buffer.join("");
      this.buffer = [];
      await this.fileSystemService.appendToFile(this.LOG_FILENAME, content);
    } catch (err) {
      // If file writing fails, we might want to log it to console but avoid infinite loop
      console.error("FileAppender failed to write logs:", err);
    } finally {
      this.isProcessing = false;
      // Check if more logs were added while processing
      if (this.buffer.length > 0) {
        this.processBuffer();
      }
    }
  }
}
