package com.faangtracker;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(origins = "http://localhost:3000")
public class StudySessionController {
  private final JdbcTemplate jdbc;

  public StudySessionController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @GetMapping
  public List<Map<String, Object>> all() {
    return jdbc.queryForList("SELECT id, session_date, track, minutes, notes FROM study_session ORDER BY id DESC LIMIT 100");
  }

  @PostMapping
  public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
    Object track = body.get("track");
    Object minutesValue = body.get("minutes");
    String notes = body.get("notes") instanceof String s ? s : null;
    if (!(track instanceof String t) || t.isBlank() || !(minutesValue instanceof Number n)) {
      return ResponseEntity.badRequest().body(Map.of("error", "track and minutes are required"));
    }
    int minutes = n.intValue();
    if (minutes < 1 || minutes > 1440) {
      return ResponseEntity.badRequest().body(Map.of("error", "minutes must be between 1 and 1440"));
    }
    jdbc.update("INSERT INTO study_session (session_date, track, minutes, notes) VALUES (CURRENT_DATE, ?, ?, ?)", t, minutes, notes);
    return ResponseEntity.ok(Map.of("saved", true));
  }
}
