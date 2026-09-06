package com.faangtracker;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/phase2")
@CrossOrigin(origins = "http://localhost:3000")
public class PhaseTwoController {
  private final JdbcTemplate jdbc;
  public PhaseTwoController(JdbcTemplate jdbc){this.jdbc=jdbc;}

  @GetMapping("/reviews")
  public List<Map<String,Object>> reviews(){
    return jdbc.queryForList("SELECT id,skill_name,topic,source_type,source_id,due_date,status,notes FROM learning_review WHERE status <> 'DONE' ORDER BY due_date,id");
  }
  @PostMapping("/reviews")
  public ResponseEntity<?> addReview(@RequestBody Map<String,Object> b){
    String skill=text(b.get("skillName")), topic=text(b.get("topic"));
    if(skill==null||topic==null)return ResponseEntity.badRequest().body(Map.of("error","skillName and topic are required"));
    String due=text(b.get("dueDate"));
    jdbc.update("INSERT INTO learning_review(skill_name,topic,source_type,due_date,status,notes) VALUES(?,?,?,?,?,?)",skill,topic,text(b.get("sourceType"))==null?"MANUAL":text(b.get("sourceType")),due==null?Date.valueOf(LocalDate.now().plusDays(7)):Date.valueOf(due),"DUE",text(b.get("notes")));
    return ResponseEntity.ok(Map.of("saved",true));
  }
  @PostMapping("/reviews/{id}/complete")
  public ResponseEntity<?> completeReview(@PathVariable long id){jdbc.update("UPDATE learning_review SET status='DONE' WHERE id=?",id);return ResponseEntity.ok(Map.of("saved",true));}
  @PostMapping("/reviews/{id}/snooze")
  public ResponseEntity<?> snoozeReview(@PathVariable long id,@RequestBody Map<String,Object> b){String due=text(b.get("dueDate"));if(due==null)return ResponseEntity.badRequest().body(Map.of("error","dueDate is required"));jdbc.update("UPDATE learning_review SET due_date=?,status='DUE' WHERE id=?",Date.valueOf(due),id);return ResponseEntity.ok(Map.of("saved",true));}

  @GetMapping("/projects")
  public List<Map<String,Object>> projects(){return jdbc.queryForList("SELECT id,name,skill_name,status,outcome,repo_url,started_date,target_date,notes FROM learning_project ORDER BY target_date NULLS LAST,id DESC");}
  @PostMapping("/projects")
  public ResponseEntity<?> addProject(@RequestBody Map<String,Object> b){
    String name=text(b.get("name")),skill=text(b.get("skillName"));if(name==null||skill==null)return ResponseEntity.badRequest().body(Map.of("error","name and skillName are required"));
    jdbc.update("INSERT INTO learning_project(name,skill_name,status,outcome,repo_url,started_date,target_date,notes) VALUES(?,?,?,?,?,?,?,?)",name,skill,text(b.get("status"))==null?"PLANNED":text(b.get("status")),text(b.get("outcome")),text(b.get("repoUrl")),date(b.get("startedDate")),date(b.get("targetDate")),text(b.get("notes")));
    return ResponseEntity.ok(Map.of("saved",true));
  }
  @PatchMapping("/projects/{id}")
  public ResponseEntity<?> updateProject(@PathVariable long id,@RequestBody Map<String,Object> b){String status=text(b.get("status"));if(status!=null)jdbc.update("UPDATE learning_project SET status=? WHERE id=?",status,id);return ResponseEntity.ok(Map.of("saved",true));}
  @DeleteMapping("/projects/{id}")
  public ResponseEntity<?> deleteProject(@PathVariable long id){jdbc.update("DELETE FROM learning_project WHERE id=?",id);return ResponseEntity.ok(Map.of("saved",true));}

  @GetMapping("/weekly-review")
  public Map<String,Object> weekly(){
    LocalDate start=LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    Map<String,Object> out=new java.util.LinkedHashMap<>();
    out.put("weekStart",start);out.put("minutes",jdbc.queryForObject("SELECT COALESCE(SUM(minutes),0) FROM study_session WHERE session_date>=?",Integer.class,Date.valueOf(start)));
    out.put("sessions",jdbc.queryForObject("SELECT COUNT(*) FROM study_session WHERE session_date>=?",Integer.class,Date.valueOf(start)));
    out.put("completedTasks",jdbc.queryForObject("SELECT COUNT(*) FROM task_completion WHERE completed=true AND completed_at>=?",Integer.class,java.sql.Timestamp.valueOf(start.atStartOfDay())));
    out.put("projects",jdbc.queryForList("SELECT id,name,skill_name,status,target_date FROM learning_project WHERE status <> 'DONE' ORDER BY target_date NULLS LAST,id DESC"));
    out.put("dueReviews",jdbc.queryForObject("SELECT COUNT(*) FROM learning_review WHERE status <> 'DONE' AND due_date<=?",Integer.class,Date.valueOf(LocalDate.now())));
    out.put("review",jdbc.queryForList("SELECT id,week_start,wins,gaps,next_week,rating FROM weekly_review WHERE week_start=?",Date.valueOf(start)));
    return out;
  }
  @PostMapping("/weekly-review")
  public ResponseEntity<?> saveWeekly(@RequestBody Map<String,Object> b){
    LocalDate start=LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    int rating=b.get("rating") instanceof Number n?n.intValue():0;if(rating<0||rating>10)return ResponseEntity.badRequest().body(Map.of("error","rating must be 0-10"));
    jdbc.update("INSERT INTO weekly_review(week_start,wins,gaps,next_week,rating) VALUES(?,?,?,?,?) ON CONFLICT(week_start) DO UPDATE SET wins=excluded.wins,gaps=excluded.gaps,next_week=excluded.next_week,rating=excluded.rating",Date.valueOf(start),text(b.get("wins")),text(b.get("gaps")),text(b.get("nextWeek")),rating);
    return ResponseEntity.ok(Map.of("saved",true));
  }
  private String text(Object o){if(!(o instanceof String s))return null;s=s.trim();return s.isBlank()?null:s;}
  private Date date(Object o){String s=text(o);return s==null?null:Date.valueOf(s);}
}
