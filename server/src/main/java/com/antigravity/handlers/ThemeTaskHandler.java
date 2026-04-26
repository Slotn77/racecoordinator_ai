package com.antigravity.handlers;

import com.antigravity.context.DatabaseContext;
import com.antigravity.models.Theme;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bson.Document;

public class ThemeTaskHandler {

  private final DatabaseContext databaseContext;

  public ThemeTaskHandler(DatabaseContext databaseContext, Javalin app) {
    this.databaseContext = databaseContext;

    app.get("/api/themes", this::listThemes);
    app.get("/api/themes/default", this::getDefaultTheme);
    app.get("/api/themes/{id}", this::getTheme);
    app.post("/api/themes", this::createTheme);
    app.put("/api/themes/{id}", this::updateTheme);
    app.delete("/api/themes/{id}", this::deleteTheme);
    app.post("/api/themes/{id}/duplicate", this::duplicateTheme);
  }

  MongoCollection<Theme> getThemeCollection() {
    return databaseContext.getDatabase().getCollection("themes", Theme.class);
  }

  protected MongoCollection<Document> getCountersCollection() {
    return databaseContext.getDatabase().getCollection("counters");
  }

  <T> T getBody(Context ctx, Class<T> clazz) {
    return ctx.bodyAsClass(clazz);
  }

  void setStatus(Context ctx, int status) {
    ctx.status(status);
  }

  void setResult(Context ctx, String result) {
    ctx.result(result);
  }

  void setJson(Context ctx, Object obj) {
    ctx.json(obj);
  }

  String getPathParam(Context ctx, String key) {
    return ctx.pathParam(key);
  }

  void listThemes(Context ctx) {
    try {
      List<Theme> themes = new ArrayList<>();
      getThemeCollection().find().forEach(themes::add);
      setJson(ctx, themes);
    } catch (Exception e) {
      e.printStackTrace();
      setStatus(ctx, 500);
      setResult(ctx, "Error listing themes: " + e.getMessage());
    }
  }

  void getDefaultTheme(Context ctx) {
    try {
      Theme theme = getThemeCollection().find(Filters.eq("is_default", true)).first();
      if (theme == null) {
        setStatus(ctx, 404);
        setResult(ctx, "No default theme found");
        return;
      }
      setJson(ctx, theme);
    } catch (Exception e) {
      e.printStackTrace();
      setStatus(ctx, 500);
      setResult(ctx, "Error getting default theme: " + e.getMessage());
    }
  }

  void getTheme(Context ctx) {
    try {
      String id = getPathParam(ctx, "id");
      Theme theme = getThemeCollection().find(Filters.eq("entity_id", id)).first();
      if (theme == null) {
        setStatus(ctx, 404);
        setResult(ctx, "Theme not found");
        return;
      }
      setJson(ctx, theme);
    } catch (Exception e) {
      e.printStackTrace();
      setStatus(ctx, 500);
      setResult(ctx, "Error getting theme: " + e.getMessage());
    }
  }

  void createTheme(Context ctx) {
    try {
      Theme theme = getBody(ctx, Theme.class);
      MongoCollection<Theme> col = getThemeCollection();

      // Uniqueness check on name
      Theme existing = col.find(Filters.eq("name", theme.getName())).first();
      if (existing != null) {
        setStatus(ctx, 409);
        setResult(ctx, "Theme name already exists");
        return;
      }

      // Assign entity ID if needed
      if (theme.getEntityId() == null
          || theme.getEntityId().isEmpty()
          || "new".equals(theme.getEntityId())) {
        String nextId = getNextSequence("themes");
        theme =
            new Theme(
                theme.getName(), false, theme.getSlots(), theme.getAudioSlots(), nextId, null);
      }

      col.insertOne(theme);
      setStatus(ctx, 201);
      setJson(ctx, theme);
    } catch (Exception e) {
      e.printStackTrace();
      setStatus(ctx, 500);
      setResult(ctx, "Error creating theme: " + e.getMessage());
    }
  }

  void updateTheme(Context ctx) {
    try {
      String id = getPathParam(ctx, "id");
      Theme theme = getBody(ctx, Theme.class);
      MongoCollection<Theme> col = getThemeCollection();

      // Check name uniqueness (excluding self)
      Theme existing =
          col.find(Filters.and(Filters.ne("entity_id", id), Filters.eq("name", theme.getName())))
              .first();
      if (existing != null) {
        setStatus(ctx, 409);
        setResult(ctx, "Theme name already exists");
        return;
      }

      // Preserve the is_default flag from the existing record
      Theme current = col.find(Filters.eq("entity_id", id)).first();
      if (current == null) {
        setStatus(ctx, 404);
        setResult(ctx, "Theme not found");
        return;
      }

      // Prevent update of the default theme
      if (current.isDefault()) {
        setStatus(ctx, 403);
        setResult(ctx, "Cannot update the default theme");
        return;
      }

      theme =
          new Theme(
              theme.getName(),
              current.isDefault(),
              theme.getSlots(),
              theme.getAudioSlots(),
              id,
              current.getId());

      col.replaceOne(Filters.eq("entity_id", id), theme);
      setJson(ctx, theme);
    } catch (Exception e) {
      e.printStackTrace();
      setStatus(ctx, 500);
      setResult(ctx, "Error updating theme: " + e.getMessage());
    }
  }

  void deleteTheme(Context ctx) {
    try {
      String id = getPathParam(ctx, "id");

      // Prevent deletion of the default theme
      Theme theme = getThemeCollection().find(Filters.eq("entity_id", id)).first();
      if (theme != null && theme.isDefault()) {
        setStatus(ctx, 400);
        setResult(ctx, "Cannot delete the default theme");
        return;
      }

      DeleteResult result = getThemeCollection().deleteOne(Filters.eq("entity_id", id));
      if (result.getDeletedCount() == 0) {
        setStatus(ctx, 404);
        setResult(ctx, "Theme not found");
        return;
      }
      setStatus(ctx, 204);
    } catch (Exception e) {
      e.printStackTrace();
      setStatus(ctx, 500);
      setResult(ctx, "Error deleting theme: " + e.getMessage());
    }
  }

  void duplicateTheme(Context ctx) {
    try {
      String id = getPathParam(ctx, "id");
      Theme source = getThemeCollection().find(Filters.eq("entity_id", id)).first();
      if (source == null) {
        setStatus(ctx, 404);
        setResult(ctx, "Source theme not found");
        return;
      }

      // Read optional name from body, or generate one
      String newName;
      try {
        Map<String, String> body = getBody(ctx, Map.class);
        newName = body.get("name");
      } catch (Exception e) {
        newName = null;
      }
      if (newName == null || newName.isEmpty()) {
        newName = source.getName() + " (Copy)";
      }

      // Ensure unique name
      MongoCollection<Theme> col = getThemeCollection();
      if (col.find(Filters.eq("name", newName)).first() != null) {
        // Append a number to make it unique
        int suffix = 2;
        while (col.find(Filters.eq("name", newName + " " + suffix)).first() != null) {
          suffix++;
        }
        newName = newName + " " + suffix;
      }

      String nextId = getNextSequence("themes");
      Theme copy =
          new Theme(newName, false, source.getSlots(), source.getAudioSlots(), nextId, null);
      col.insertOne(copy);
      setStatus(ctx, 201);
      setJson(ctx, copy);
    } catch (Exception e) {
      e.printStackTrace();
      setStatus(ctx, 500);
      setResult(ctx, "Error duplicating theme: " + e.getMessage());
    }
  }

  private String getNextSequence(String collectionName) {
    MongoCollection<Document> counters = getCountersCollection();
    Document counter =
        counters.findOneAndUpdate(
            Filters.eq("_id", collectionName),
            Updates.inc("seq", 1),
            new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER));
    return String.valueOf(counter.getInteger("seq"));
  }
}
