import { Injectable } from "@angular/core";
import { NavigationEnd, Router } from "@angular/router";
import { filter } from "rxjs/operators";

@Injectable({
  providedIn: "root",
})
export class NavigationService {
  private history: string[] = [];
  private direction: "forward" | "backward" = "forward";
  private previousUrl: string | null = null;

  constructor(private router: Router) {
    // We use a simple stack to detect if we're going backward.
    // If the new URL matches the previous URL in the stack, it's a "back" operation.
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        const url = event.urlAfterRedirects;
        const prev = this.history[this.history.length - 1] || null;

        if (
          this.history.length > 1 &&
          this.history[this.history.length - 2] === url
        ) {
          // Navigating back
          this.direction = "backward";
          this.history.pop();
        } else {
          // Navigating forward
          this.direction = "forward";
          this.history.push(url);
        }
        this.previousUrl = prev;
      });
  }

  getDirection(): "forward" | "backward" {
    return this.direction;
  }

  getPreviousUrl(): string | null {
    return this.previousUrl;
  }
}
