package com.meatmetrics.meatmetrics;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping({"/health", "/api/health"})
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "ok");
        body.put("time", Instant.now().toString());
        return ResponseEntity.ok(body);
    }

    @GetMapping({"/health/db", "/api/health/db"})
    public ResponseEntity<Map<String, Object>> healthDb() {
        Map<String, Object> body = new HashMap<>();
        try (Connection ignored = dataSource.getConnection()) {
            body.put("status", "ok");
        } catch (Exception e) {
            body.put("status", "down");
            body.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
            return ResponseEntity.status(503).body(body);
        }
        body.put("time", Instant.now().toString());
        return ResponseEntity.ok(body);
    }
}
