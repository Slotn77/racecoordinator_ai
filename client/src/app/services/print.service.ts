import { Injectable } from "@angular/core";

@Injectable({
  providedIn: "root",
})
export class PrintService {
  constructor() {}

  /**
   * Triggers the native browser print dialog.
   * @param pageName Optional name to use in the exported file title (<pageName> -- yyyy-mm-dd)
   * @param fullScroll If true, adds a class to the body to unroll scrollable regions before printing.
   * @param timestamp Optional fixed date to use for the filename. Defaults to the current time.
   */
  public print(
    pageName?: string,
    fullScroll: boolean = false,
    timestamp?: Date,
  ): void {
    if (fullScroll) {
      document.body.classList.add("print-full-scroll");
    }

    let originalTitle = "";
    if (pageName) {
      originalTitle = document.title;
      const now = timestamp || new Date();
      const yyyy = now.getFullYear();
      const mm = String(now.getMonth() + 1).padStart(2, "0");
      const dd = String(now.getDate()).padStart(2, "0");

      let hours = now.getHours();
      const ampm = hours >= 12 ? "PM" : "AM";
      hours = hours % 12;
      hours = hours ? hours : 12; // Handle 0 as 12
      const hh = String(hours).padStart(2, "0");

      const min = String(now.getMinutes()).padStart(2, "0");
      const ss = String(now.getSeconds()).padStart(2, "0");
      document.title = `${pageName} -- ${yyyy}-${mm}-${dd} -- ${hh}-${min}-${ss}_${ampm}`;
    }

    // Use a slight timeout to allow the browser to reflow the DOM if a class was added
    setTimeout(() => {
      window.print();

      if (fullScroll) {
        document.body.classList.remove("print-full-scroll");
      }

      if (pageName) {
        document.title = originalTitle;
      }
    }, 100);
  }
}
