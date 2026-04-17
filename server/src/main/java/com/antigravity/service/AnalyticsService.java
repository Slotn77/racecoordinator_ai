package com.antigravity.service;

import com.antigravity.race.Race;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyticsService {

  private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);
  private static AnalyticsService instance;

  private String measurementId;
  private String apiSecret;
  private final boolean enabled;
  private boolean userEnabled = true;
  private final ObjectMapper mapper;
  private final ServerConfigService configService;

  private AnalyticsService() {
    this.mapper = new ObjectMapper();

    Properties props = new Properties();
    try (InputStream input =
        getClass().getClassLoader().getResourceAsStream("analytics.properties")) {
      if (input != null) {
        props.load(input);
        this.measurementId = props.getProperty("ga.measurement.id");
        this.apiSecret = props.getProperty("ga.api.secret");
      }
    } catch (Exception ex) {
      logger.warn("Could not load analytics.properties", ex);
    }

    this.configService = new ServerConfigService();
    this.userEnabled = configService.isShareAnalyticsEnabled();

    if (this.measurementId != null
        && !this.measurementId.contains("XXXXX")
        && !this.measurementId.isEmpty()
        && this.apiSecret != null
        && !this.apiSecret.contains("your_api_secret")
        && !this.apiSecret.isEmpty()) {
      this.enabled = true;
      logger.info("Google Analytics Measurement Protocol configured and enabled.");
    } else {
      this.enabled = false;
      logger.info("Google Analytics Measurement Protocol disabled (no valid credentials found).");
    }
  }

  public void setUserEnabled(boolean enabled) {
    this.userEnabled = enabled;
    this.configService.setShareAnalyticsEnabled(enabled);
    logger.info("Analytics sharing {}", enabled ? "enabled" : "disabled");
    trackAnalyticsToggle(enabled);
  }

  public void trackAnalyticsToggle(boolean newStatus) {
    if (!enabled) {
      return;
    }

    Map<String, Object> params = new HashMap<>();
    params.put("analytics_enabled", newStatus);
    params.put("engagement_time_msec", 1L);
    params.put("session_id", System.currentTimeMillis());

    sendPayload(createPayload("analytics_toggled", params));
  }

  public boolean isUserEnabled() {
    return this.userEnabled;
  }

  public static synchronized AnalyticsService getInstance() {
    if (instance == null) {
      instance = new AnalyticsService();
    }
    return instance;
  }

  public String getClientId() {
    return this.configService.getAnalyticsClientId();
  }

  public String getMeasurementId() {
    return this.measurementId;
  }

  public void trackRaceStart(Race race) {
    if (!enabled || !userEnabled || race == null) {
      return;
    }

    Map<String, Object> params = new HashMap<>();
    params.put(
        "number_of_lanes",
        race.getTrack() != null && race.getTrack().getLanes() != null
            ? race.getTrack().getLanes().size()
            : 0);
    params.put("driver_count", race.getDrivers() != null ? race.getDrivers().size() : 0);
    params.put("is_demo", race.isDemoMode());
    params.put("engagement_time_msec", 1L);
    params.put("session_id", System.currentTimeMillis());

    sendPayload(createPayload("backend_race_started", params));
  }

  /* package */ Map<String, Object> createPayload(String eventName, Map<String, Object> params) {
    Map<String, Object> event = new HashMap<>();
    event.put("name", eventName);
    event.put("params", params);

    Map<String, Object> payload = new HashMap<>();
    payload.put("client_id", getClientId());
    payload.put("events", Collections.singletonList(event));
    return payload;
  }

  private void sendPayload(Map<String, Object> payload) {
    CompletableFuture.runAsync(
        () -> {
          try {
            String jsonPayload = mapper.writeValueAsString(payload);
            String urlString =
                String.format(
                    "https://www.google-analytics.com/mp/collect?measurement_id=%s&api_secret=%s",
                    measurementId, apiSecret);

            URL url = new URL(urlString);
            HttpURLConnection conn = createConnection(url);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
              byte[] input = jsonPayload.getBytes("utf-8");
              os.write(input, 0, input.length);
            }

            int statusCode = conn.getResponseCode();
            if (statusCode >= 400) {
              StringBuilder responseBody = new StringBuilder();
              try (InputStream is = conn.getErrorStream()) {
                if (is != null) {
                  byte[] buffer = new byte[1024];
                  int bytesRead;
                  while ((bytesRead = is.read(buffer)) != -1) {
                    responseBody.append(new String(buffer, 0, bytesRead, "utf-8"));
                  }
                }
              } catch (Exception bodyEx) {
                responseBody
                    .append("(could not read error body: ")
                    .append(bodyEx.getMessage())
                    .append(")");
              }
              logger.warn(
                  "Failed to send GA telemetry. Status: {}. Response: {}",
                  statusCode,
                  responseBody);
            } else {
              logger.debug("Successfully sent GA telemetry. Status: {}", statusCode);
            }
          } catch (Exception e) {
            logger.warn("Error sending GA telemetry: {}", e.getMessage());
          }
        });
  }

  protected HttpURLConnection createConnection(URL url) throws Exception {
    return (HttpURLConnection) url.openConnection();
  }
}
