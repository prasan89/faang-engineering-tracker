package com.faangtracker;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/skills")
@CrossOrigin(origins = "http://localhost:3000")
public class SkillController {
  private final JdbcTemplate jdbc;

  public SkillController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @GetMapping
  public List<Map<String, Object>> all() {
    return jdbc.queryForList("SELECT id, name, created_at FROM custom_skill WHERE active = TRUE ORDER BY name");
  }

  @PostMapping
  public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
    Object value = body.get("name");
    if (!(value instanceof String name) || name.isBlank()) {
      return ResponseEntity.badRequest().body(Map.of("error", "name is required"));
    }
    name = name.trim();
    if (name.length() > 50) {
      return ResponseEntity.badRequest().body(Map.of("error", "name must be 50 characters or less"));
    }
    jdbc.update("INSERT INTO custom_skill (name, active) VALUES (?, TRUE) ON CONFLICT (name) DO UPDATE SET active = TRUE", name);
    return ResponseEntity.ok(Map.of("saved", true, "name", name));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> remove(@PathVariable long id) {
    jdbc.update("UPDATE custom_skill SET active = FALSE WHERE id = ?", id);
    return ResponseEntity.ok(Map.of("saved", true));
  }
}
