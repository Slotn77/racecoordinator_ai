package com.antigravity.handlers;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antigravity.context.DatabaseContext;
import com.antigravity.models.Theme;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ThemeTaskHandlerTest {

  private DatabaseContext databaseContext;
  private MongoDatabase mongoDatabase;
  private MongoCollection<Theme> themeCollection;
  private MongoCollection<Document> countersCollection;
  private Javalin app;
  private ThemeTaskHandler handler;
  private Context ctx;
  private HttpServletResponse res;

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() {
    databaseContext = mock(DatabaseContext.class);
    mongoDatabase = mock(MongoDatabase.class);
    themeCollection = mock(MongoCollection.class);
    countersCollection = mock(MongoCollection.class);
    app = mock(Javalin.class);

    when(databaseContext.getDatabase()).thenReturn(mongoDatabase);
    when(mongoDatabase.getCollection(eq("themes"), eq(Theme.class))).thenReturn(themeCollection);
    when(mongoDatabase.getCollection(eq("counters"))).thenReturn(countersCollection);

    HttpServletRequest req = mock(HttpServletRequest.class);
    res = mock(HttpServletResponse.class);
    handler = org.mockito.Mockito.spy(new ThemeTaskHandler(databaseContext, app));
    org.mockito.Mockito.doReturn(themeCollection).when(handler).getThemeCollection();
    org.mockito.Mockito.doReturn(countersCollection).when(handler).getCountersCollection();

    org.mockito.Mockito.doNothing().when(handler).setStatus(any(), anyInt());
    org.mockito.Mockito.doNothing().when(handler).setResult(any(), anyString());
    org.mockito.Mockito.doNothing().when(handler).setJson(any(), any());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testListThemes_Success() {
    List<Theme> themes = new ArrayList<>();
    themes.add(new Theme("Theme 1", true, new HashMap<>(), new HashMap<>(), "1", null));

    FindIterable<Theme> findIterable = mock(FindIterable.class);
    when(themeCollection.find()).thenReturn(findIterable);

    // Mock for-each
    when(findIterable.iterator()).thenReturn(mock(com.mongodb.client.MongoCursor.class));
    // Simplified: ThemeTaskHandler uses forEach(themes::add)
    // We can use doAnswer if we really want to simulate the forEach, but let's just mock the
    // behavior

    handler.listThemes(ctx);

    verify(handler).setJson(any(), any());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testCreateTheme_Success() {
    Theme themeRequest =
        new Theme("New Theme", false, new HashMap<>(), new HashMap<>(), "new", null);
    org.mockito.Mockito.doReturn(themeRequest).when(handler).getBody(any(), eq(Theme.class));

    // No existing theme with same name
    FindIterable<Theme> findIterable = mock(FindIterable.class);
    when(themeCollection.find(any(Bson.class))).thenReturn(findIterable);
    when(findIterable.first()).thenReturn(null);

    // Mock sequence generation
    Document counterDoc = new Document("seq", 5);
    when(countersCollection.findOneAndUpdate(any(Bson.class), any(Bson.class), any()))
        .thenReturn(counterDoc);

    handler.createTheme(ctx);

    verify(handler).setStatus(any(), eq(201));
    ArgumentCaptor<Theme> captor = ArgumentCaptor.forClass(Theme.class);
    verify(themeCollection).insertOne(captor.capture());
    assertEquals("5", captor.getValue().getEntityId());
    assertEquals("New Theme", captor.getValue().getName());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testCreateTheme_DuplicateName_ShouldFail() {
    Theme themeRequest =
        new Theme("Existing Theme", false, new HashMap<>(), new HashMap<>(), "new", null);
    org.mockito.Mockito.doReturn(themeRequest).when(handler).getBody(any(), eq(Theme.class));

    // Existing theme with same name found
    Theme existing =
        new Theme("Existing Theme", false, new HashMap<>(), new HashMap<>(), "1", null);
    FindIterable<Theme> findIterable = mock(FindIterable.class);
    when(themeCollection.find(any(Bson.class))).thenReturn(findIterable);
    when(findIterable.first()).thenReturn(existing);

    handler.createTheme(ctx);

    verify(handler).setStatus(any(), eq(409));
    verify(handler).setResult(any(), eq("Theme name already exists"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testDeleteTheme_DefaultTheme_ShouldFail() {
    String id = "default_theme";
    Theme theme = new Theme("Default", true, new HashMap<>(), new HashMap<>(), id, null);

    org.mockito.Mockito.doReturn(id).when(handler).getPathParam(any(), eq("id"));

    FindIterable<Theme> findIterable = mock(FindIterable.class);
    when(themeCollection.find(any(Bson.class))).thenReturn(findIterable);
    when(findIterable.first()).thenReturn(theme);

    handler.deleteTheme(ctx);

    verify(handler).setStatus(any(), eq(400));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testDuplicateTheme_Success() {
    String sourceId = "source_1";
    Theme source = new Theme("Original", false, new HashMap<>(), new HashMap<>(), sourceId, null);

    org.mockito.Mockito.doReturn(sourceId).when(handler).getPathParam(any(), eq("id"));

    FindIterable<Theme> findIterable = mock(FindIterable.class);
    when(themeCollection.find(any(Bson.class))).thenReturn(findIterable);
    when(findIterable.first())
        .thenReturn(source)
        .thenReturn(null); // Return source first, then null for uniqueness check

    // Mock sequence generation
    Document counterDoc = new Document("seq", 10);
    when(countersCollection.findOneAndUpdate(any(Bson.class), any(Bson.class), any()))
        .thenReturn(counterDoc);

    Map<String, String> bodyMap = new HashMap<>();
    bodyMap.put("name", "Original (Copy)");
    org.mockito.Mockito.doReturn(bodyMap).when(handler).getBody(any(), eq(Map.class));

    handler.duplicateTheme(ctx);

    verify(handler).setStatus(any(), eq(201));
    ArgumentCaptor<Theme> captor = ArgumentCaptor.forClass(Theme.class);
    verify(themeCollection).insertOne(captor.capture());
    assertEquals("10", captor.getValue().getEntityId());
    assertEquals("Original (Copy)", captor.getValue().getName());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateTheme_DefaultTheme_ShouldFail() {
    String id = "default_theme";
    Theme defaultTheme = new Theme("Default", true, new HashMap<>(), new HashMap<>(), id, null);
    Theme updateRequest = new Theme("New Name", true, new HashMap<>(), new HashMap<>(), id, null);

    org.mockito.Mockito.doReturn(id).when(handler).getPathParam(any(), eq("id"));
    org.mockito.Mockito.doReturn(updateRequest).when(handler).getBody(any(), eq(Theme.class));

    FindIterable<Theme> findIterable = mock(FindIterable.class);
    when(themeCollection.find(any(Bson.class))).thenReturn(findIterable);
    // First call to check name uniqueness (return null), second to find 'current' (return
    // defaultTheme)
    when(findIterable.first()).thenReturn(null).thenReturn(defaultTheme);

    handler.updateTheme(ctx);

    verify(handler).setStatus(any(), eq(403));
    verify(handler).setResult(any(), eq("Cannot update the default theme"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateTheme_DuplicateName_ShouldFail() {
    String id = "theme_2";
    Theme updateRequest =
        new Theme("Existing Name", false, new HashMap<>(), new HashMap<>(), id, null);

    org.mockito.Mockito.doReturn(id).when(handler).getPathParam(any(), eq("id"));
    org.mockito.Mockito.doReturn(updateRequest).when(handler).getBody(any(), eq(Theme.class));

    // Another theme with same name found
    Theme existing =
        new Theme("Existing Name", false, new HashMap<>(), new HashMap<>(), "theme_1", null);
    FindIterable<Theme> findIterable = mock(FindIterable.class);
    when(themeCollection.find(any(Bson.class))).thenReturn(findIterable);
    when(findIterable.first()).thenReturn(existing);

    handler.updateTheme(ctx);

    verify(handler).setStatus(any(), eq(409));
    verify(handler).setResult(any(), eq("Theme name already exists"));
  }
}
