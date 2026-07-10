import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";

export interface UpdateCheckResult {
  updateAvailable: boolean;
  latestVersion: string;
  downloadUrl: string;
  releaseNotes: string;
  releaseUrl: string;
  isWindows: boolean;
}

@Injectable({
  providedIn: "root",
})
export class UpdateService {
  private apiUrl = "http://localhost:7070/api/update";

  constructor(private http: HttpClient) {
    const currentOrigin = window.location.origin;
    if (currentOrigin && !currentOrigin.includes("localhost:4200")) {
      this.apiUrl = `${currentOrigin}/api/update`;
    }
  }

  checkForUpdates(): Observable<UpdateCheckResult> {
    return this.http.get<UpdateCheckResult>(`${this.apiUrl}/check`);
  }

  installUpdate(downloadUrl: string): Observable<any> {
    return this.http.post(
      `${this.apiUrl}/install`,
      { downloadUrl },
      { responseType: "text" },
    );
  }

  skipUpdate(version: string): Observable<any> {
    return this.http.post(
      `${this.apiUrl}/skip`,
      { version },
      { responseType: "text" },
    );
  }
}
