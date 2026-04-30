package com.antigravity.handlers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antigravity.context.DatabaseContext;
import com.antigravity.proto.AssetMessage;
import com.antigravity.service.AssetService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

public class AssetTaskHandlerTest {

  private DatabaseContext databaseContext;
  private com.mongodb.client.MongoDatabase mongoDatabase;
  private com.mongodb.client.MongoCollection<Document> assetCollection;
  private Javalin app;
  private AssetTaskHandler handler;
  private Context ctx;

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() {
    databaseContext = mock(DatabaseContext.class);
    mongoDatabase = mock(com.mongodb.client.MongoDatabase.class);
    assetCollection = mock(com.mongodb.client.MongoCollection.class);
    app = mock(Javalin.class);
    ctx = mock(Context.class);

    when(databaseContext.getDatabase()).thenReturn(mongoDatabase);
    when(mongoDatabase.getCollection(eq("assets"))).thenReturn(assetCollection);
    when(databaseContext.getDataRoot()).thenReturn("/tmp/data/");
    when(databaseContext.getCurrentDatabaseName()).thenReturn("TestDB");

    AssetService assetService = mock(AssetService.class);
    handler = org.mockito.Mockito.spy(new AssetTaskHandler(databaseContext, app));
    org.mockito.Mockito.doReturn(assetService).when(handler).getAssetService();

    // Use doNothing for our wrapper methods
    org.mockito.Mockito.doNothing().when(handler).setStatus(any(), anyInt());
    org.mockito.Mockito.doNothing().when(handler).setResult(any(), anyString());
    org.mockito.Mockito.doNothing().when(handler).setResult(any(), any(byte[].class));
    org.mockito.Mockito.doNothing().when(handler).setStream(any(), any());
    org.mockito.Mockito.doNothing().when(handler).setContentType(any(), anyString());
    org.mockito.Mockito.doReturn("dummy").when(handler).getPathParam(any(), anyString());
    org.mockito.Mockito.doReturn(new byte[0]).when(handler).getBodyBytes(any());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testDownloadAsset_ImageSetFallback() throws Exception {
    String assetId = "set-123";
    org.mockito.Mockito.doReturn(assetId).when(handler).getPathParam(ctx, "id");

    AssetService assetService = handler.getAssetService();
    when(assetService.getAssetById(assetId)).thenReturn(AssetMessage.newBuilder().build());

    // Mock document for image set
    Document doc =
        new Document("_id", assetId)
            .append("type", "image_set")
            .append("url", "/assets/thumb_123.png");

    com.mongodb.client.FindIterable<Document> findIterable =
        mock(com.mongodb.client.FindIterable.class);
    when(assetCollection.find(any(org.bson.conversions.Bson.class))).thenReturn(findIterable);
    when(findIterable.first()).thenReturn(doc);

    // We also need to mock or ignore serveFile since it will fail on file check
    // But we want to see if it resolved the filename correctly
    java.io.File assetsDir = new java.io.File("/tmp/data/TestDB/assets");
    assetsDir.mkdirs();
    new java.io.File(assetsDir, "thumb_123.png").createNewFile();

    handler.downloadAsset(ctx);

    // If it reached filename resolution, verify(handler).setStatus(ctx, 404) should NOT be called
    verify(handler, org.mockito.Mockito.never()).setStatus(eq(ctx), eq(404));
    // Verify it tried to set the stream (meaning file was found)
    verify(handler).setStream(eq(ctx), any());
  }

  @Test
  public void testSaveAudioSet() throws Exception {
    com.antigravity.proto.SaveAudioSetRequest request =
        com.antigravity.proto.SaveAudioSetRequest.newBuilder().setName("My Audio Set").build();
    org.mockito.Mockito.doReturn(request.toByteArray()).when(handler).getBodyBytes(any());

    AssetService assetService = handler.getAssetService();
    when(assetService.saveAudioSet(any(), anyString(), any()))
        .thenReturn(AssetMessage.newBuilder().build());

    handler.saveAudioSet(ctx);

    verify(assetService).saveAudioSet(any(), eq("My Audio Set"), any());
    verify(handler).setContentType(eq(ctx), eq("application/octet-stream"));
    verify(handler).setResult(eq(ctx), any(byte[].class));
  }
}
