package com.faangtracker;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/learning")
@CrossOrigin(origins = "http://localhost:3000")
public class LearningController {
  private final JdbcTemplate jdbc;
  public LearningController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @GetMapping("/dashboard")
  public Map<String, Object> dashboard() {
    LocalDate today = LocalDate.now();
    LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    LocalDate monthStart = today.withDayOfMonth(1);
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("totalMinutes", scalar("SELECT COALESCE(SUM(minutes),0) FROM study_session"));
    out.put("weekMinutes", scalar("SELECT COALESCE(SUM(minutes),0) FROM study_session WHERE session_date >= ?", Date.valueOf(weekStart)));
    out.put("monthMinutes", scalar("SELECT COALESCE(SUM(minutes),0) FROM study_session WHERE session_date >= ?", Date.valueOf(monthStart)));
    out.put("sessionCount", scalar("SELECT COUNT(*) FROM study_session"));
    out.put("bestDay", jdbc.queryForList("SELECT session_date, SUM(minutes) AS minutes FROM study_session GROUP BY session_date ORDER BY minutes DESC, session_date DESC LIMIT 1"));
    out.put("bySkill", jdbc.queryForList("SELECT track AS skill, SUM(minutes) AS minutes, COUNT(*) AS sessions, MAX(session_date) AS last_studied FROM study_session GROUP BY track ORDER BY minutes DESC"));
    out.put("weekBySkill", jdbc.queryForList("SELECT track AS skill, SUM(minutes) AS minutes FROM study_session WHERE session_date >= ? GROUP BY track ORDER BY minutes DESC", Date.valueOf(weekStart)));
    out.put("activity", jdbc.queryForList("SELECT session_date AS date, SUM(minutes) AS minutes FROM study_session WHERE session_date >= ? GROUP BY session_date ORDER BY session_date", Date.valueOf(today.minusDays(29))));
    out.put("growth", jdbc.queryForList("SELECT session_date AS date, track AS skill, minutes FROM study_session ORDER BY session_date, track"));
    out.put("weeklyTargetMinutes", 600);
    out.put("streak", streak());
    return out;
  }

  @GetMapping("/skills/{skill}")
  public Map<String, Object> skill(@PathVariable String skill) {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("skill", skill);
    out.put("summary", jdbc.queryForList("SELECT COALESCE(SUM(minutes),0) AS minutes, COUNT(*) AS sessions, MAX(session_date) AS last_studied FROM study_session WHERE track = ?", skill));
    out.put("recent", jdbc.queryForList("SELECT id, session_date, minutes, notes FROM study_session WHERE track = ? ORDER BY session_date DESC, id DESC LIMIT 100", skill));
    out.put("streak", skillStreak(skill));
    return out;
  }

  @GetMapping("/goals")
  public List<Map<String,Object>> goals() {
    return jdbc.queryForList("SELECT id, skill_name, target_outcome, deadline, weekly_minutes, created_at FROM learning_goal ORDER BY deadline NULLS LAST, id DESC");
  }

  @PostMapping("/goals")
  public ResponseEntity<?> createGoal(@RequestBody Map<String,Object> body) {
    String skill = text(body.get("skillName"));
    String outcome = text(body.get("targetOutcome"));
    if (skill == null || outcome == null) return ResponseEntity.badRequest().body(Map.of("error","skillName and targetOutcome are required"));
    if (skill.length() > 50 || outcome.length() > 500) return ResponseEntity.badRequest().body(Map.of("error","skill or outcome is too long"));
    int weekly = number(body.get("weeklyMinutes"), 0);
    if (weekly < 0 || weekly > 10080) return ResponseEntity.badRequest().body(Map.of("error","weeklyMinutes must be between 0 and 10080"));
    String deadline = text(body.get("deadline"));
    jdbc.update("INSERT INTO learning_goal (skill_name,target_outcome,deadline,weekly_minutes) VALUES (?,?,?,?)", skill, outcome, deadline == null ? null : Date.valueOf(deadline), weekly);
    return ResponseEntity.ok(Map.of("saved",true));
  }

  @DeleteMapping("/goals/{id}")
  public ResponseEntity<?> deleteGoal(@PathVariable long id) {
    jdbc.update("DELETE FROM learning_goal WHERE id = ?", id);
    return ResponseEntity.ok(Map.of("saved",true));
  }

  @GetMapping("/journal")
  public List<Map<String,Object>> journal() {
    return jdbc.queryForList("SELECT id, journal_date, skill_name, learned, built, difficult, revisit, resources FROM learning_journal ORDER BY journal_date DESC, id DESC LIMIT 100");
  }

  @PostMapping("/journal")
  public ResponseEntity<?> createJournal(@RequestBody Map<String,Object> body) {
    String skill = text(body.get("skillName"));
    if (skill == null) return ResponseEntity.badRequest().body(Map.of("error","skillName is required"));
    String date = text(body.get("date"));
    jdbc.update("INSERT INTO learning_journal (journal_date,skill_name,learned,built,difficult,revisit,resources) VALUES (?,?,?,?,?,?,?)",
      date == null ? Date.valueOf(LocalDate.now()) : Date.valueOf(date), skill, text(body.get("learned")), text(body.get("built")), text(body.get("difficult")), text(body.get("revisit")), text(body.get("resources")));
    return ResponseEntity.ok(Map.of("saved",true));
  }

  private Object scalar(String sql, Object... args) { return jdbc.queryForObject(sql, Object.class, args); }
  private int number(Object o, int fallback) { return o instanceof Number n ? n.intValue() : fallback; }
  private String text(Object o) { if (!(o instanceof String s)) return null; s=s.trim(); return s.isBlank()?null:s; }

  private int streak() {
    List<LocalDate> dates = jdbc.query("SELECT DISTINCT session_date FROM study_session ORDER BY session_date DESC", (rs,n)->rs.getDate(1).toLocalDate());
    if (dates.isEmpty()) return 0;
    LocalDate cursor = LocalDate.now();
    if (!dates.get(0).equals(cursor)) cursor = cursor.minusDays(1);
    int count=0;
    for (LocalDate d: dates) { if (!d.equals(cursor)) break; count++; cursor=cursor.minusDays(1); }
    return count;
  }
  private int skillStreak(String skill) {
    List<LocalDate> dates = jdbc.query("SELECT DISTINCT session_date FROM study_session WHERE track = ? ORDER BY session_date DESC", (rs,n)->rs.getDate(1).toLocalDate(), skill);
    if (dates.isEmpty()) return 0;
    LocalDate cursor=LocalDate.now(); if (!dates.get(0).equals(cursor)) cursor=cursor.minusDays(1);
    int count=0; for(LocalDate d:dates){if(!d.equals(cursor))break;count++;cursor=cursor.minusDays(1);} return count;
  }
}
