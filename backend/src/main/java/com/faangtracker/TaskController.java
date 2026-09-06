package com.faangtracker;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "http://localhost:3000")
public class TaskController {
  private final JdbcTemplate jdbc;

  public TaskController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @GetMapping
  public Object all() {
    return jdbc.queryForList("SELECT task_id, completed, completed_at FROM task_completion ORDER BY task_id");
  }

  @PostMapping
  public ResponseEntity<?> update(@RequestBody Map<String, Object> body) {
    Object id = body.get("taskId");
    Object value = body.get("completed");
    if (!(id instanceof String taskId) || taskId.isBlank() || !(value instanceof Boolean completed)) {
      return ResponseEntity.badRequest().body(Map.of("error", "taskId and completed are required"));
    }
    jdbc.update("""
      INSERT INTO task_completion (task_id, completed, completed_at)
      VALUES (?, ?, ?)
      ON CONFLICT (task_id) DO UPDATE SET
        completed = EXCLUDED.completed,
        completed_at = EXCLUDED.completed_at
      """, taskId, completed, completed ? Instant.now() : null);
    return ResponseEntity.ok(Map.of("taskId", taskId, "completed", completed));
  }
}
