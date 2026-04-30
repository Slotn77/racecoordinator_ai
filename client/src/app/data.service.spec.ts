import {
  HttpClientTestingModule,
  HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";

import { DataService } from "./data.service";
import { com } from "./proto/message";

describe("DataService", () => {
  let service: DataService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [DataService],
    });
    service = TestBed.inject(DataService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });

  it("should call save-audio-set endpoint", (done) => {
    const entries = [
      { name: "test.wav", timeSeconds: 5, data: new Uint8Array([1, 2, 3]) },
    ];

    service
      .saveAudioSet("My Set", entries as any, "id-123")
      .subscribe((asset) => {
        expect(asset).toBeTruthy();
        expect(asset.model?.entityId).toBe("new-id");
        done();
      });

    const req = httpMock.expectOne((request) =>
      request.url.endsWith("/api/assets/save-audio-set"),
    );
    expect(req.request.method).toBe("POST");
    expect(req.request.body instanceof Blob).toBeTrue();

    // Mock response using SaveAudioSetResponse
    const saveResponse = com.antigravity.SaveAudioSetResponse.create({
      success: true,
      asset: {
        model: { entityId: "new-id" },
        name: "My Set",
        type: "audio_set",
      },
    });
    const buffer =
      com.antigravity.SaveAudioSetResponse.encode(saveResponse).finish();
    req.flush(
      buffer.buffer.slice(
        buffer.byteOffset,
        buffer.byteOffset + buffer.byteLength,
      ),
    );
  });
});
