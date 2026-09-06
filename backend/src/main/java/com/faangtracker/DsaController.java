package com.faangtracker;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dsa")
@CrossOrigin(origins = "http://localhost:3000")
public class DsaController {
  private final JdbcTemplate jdbc;

  public DsaController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @GetMapping
  public Object all() {
    return jdbc.queryForList("""
      SELECT p.id, p.title, p.url, p.difficulty, p.pattern,
             COALESCE(x.status, 'NOT_STARTED') AS status,
             COALESCE(x.attempts, 0) AS attempts,
             COALESCE(x.time_minutes, 0) AS time_minutes,
             x.notes, x.solved_at
      FROM dsa_problem p
      LEFT JOIN dsa_progress x ON x.problem_id = p.id
      ORDER BY p.id
      """);
  }

  @PostMapping
  public ResponseEntity<?> update(@RequestBody Map<String, Object> body) {
    Object id = body.get("problemId");
    Object status = body.get("status");
    if (!(id instanceof String problemId) || problemId.isBlank() || !(status instanceof String nextStatus)) {
      return ResponseEntity.badRequest().body(Map.of("error", "problemId and status are required"));
    }
    int attempts = body.get("attempts") instanceof Number n ? n.intValue() : 0;
    int minutes = body.get("timeMinutes") instanceof Number n ? n.intValue() : 0;
    String notes = body.get("notes") instanceof String s ? s : null;
    Instant solvedAt = "SOLVED".equals(nextStatus) ? Instant.now() : null;

    jdbc.update("""
      INSERT INTO dsa_progress (problem_id, status, attempts, time_minutes, notes, solved_at)
      VALUES (?, ?, ?, ?, ?, ?)
      ON CONFLICT (problem_id) DO UPDATE SET
        status = EXCLUDED.status,
        attempts = EXCLUDED.attempts,
        time_minutes = EXCLUDED.time_minutes,
        notes = EXCLUDED.notes,
        solved_at = EXCLUDED.solved_at
      """, problemId, nextStatus, attempts, minutes, notes, solvedAt);

    return ResponseEntity.ok(Map.of("problemId", problemId, "status", nextStatus));
  }
}
