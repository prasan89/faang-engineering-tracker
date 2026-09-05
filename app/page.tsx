'use client';
import { useEffect, useMemo, useState } from 'react';
import roadmap from '../data/roadmap.json';

type Task={id:string;title:string;track:string;minutes:number};
const defaultTasks=(w:number):Task[]=>[
{id:`w${w}-dsa`,title:'DSA: timed practice + pattern review',track:'DSA',minutes:120},
{id:`w${w}-java`,title:'Java deep dive: internals / concurrency',track:'Java',minutes:75},
{id:`w${w}-jvm`,title:'JVM performance: profiling / GC / latency',track:'JVM Performance',minutes:60},
{id:`w${w}-sys`,title:'System Design: one production-scale design',track:'System Design',minutes:60},
{id:`w${w}-dist`,title:'Distributed Systems: trade-off study',track:'Distributed Systems',minutes:45},
{id:`w${w}-ai`,title:'AI Engineering: LLM / RAG / agents project work',track:'AI Engineering',minutes:60},
{id:`w${w}-rec`,title:'Recognition: writing / OSS / networking / referral',track:'Recognition',minutes:30}
];
const weeks=roadmap.map((w:any)=>({...w,tasks:(w.tasks?.length?w.tasks:defaultTasks(w.week)) as Task[]}));

export default function Home(){
 const[currentWeek,setCurrentWeek]=useState(1);
 const[completed,setCompleted]=useState<Record<string,boolean>>({});
 const[saving,setSaving]=useState<string|null>(null);
 const[backendError,setBackendError]=useState(false);
 useEffect(()=>{fetch('/api/tasks').then(r=>{if(!r.ok)throw new Error();return r.json()}).then(rows=>{const next:Record<string,boolean>={};(rows.completions??rows).forEach((x:any)=>{next[x.task_id??x.taskId]=!!x.completed});setCompleted(next);}).catch(()=>setBackendError(true));},[]);
 const tasks=weeks.flatMap(w=>w.tasks); const done=tasks.filter(t=>completed[t.id]).length; const pct=Math.round(done/Math.max(tasks.length,1)*100); const selected=weeks.find(w=>w.week===currentWeek)??weeks[0];
 const selectedDone=selected.tasks.filter((t:Task)=>completed[t.id]).length;
 const selectedPct=Math.round(selectedDone/Math.max(selected.tasks.length,1)*100);
 const tracks=useMemo(()=>['DSA','Java','JVM Performance','System Design','Distributed Systems','AI Engineering','Recognition'].map(name=>{const all=tasks.filter(t=>t.track===name);const d=all.filter(t=>completed[t.id]).length;return{name,pct:Math.round(d/Math.max(all.length,1)*100)}}),[completed,tasks]);
 const toggle=async(id:string,v:boolean)=>{setCompleted(x=>({...x,[id]:v}));setSaving(id);setBackendError(false);try{const r=await fetch('/api/tasks',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({taskId:id,completed:v})});if(!r.ok)throw new Error();}catch{setBackendError(true);setCompleted(x=>({...x,[id]:!v}));}finally{setSaving(null)}};
 return <main><header className="topbar"><div><div className="eyebrow">6-MONTH CAREER SPRINT</div><h1>FAANG Engineering Tracker</h1><p>Java • JVM Performance • DSA • Distributed Systems • System Design • AI</p></div><div className="badge">Week {currentWeek} / 26</div></header>
 {backendError&&<div className="notice">Database connection unavailable. Changes will not be saved until the backend is running.</div>}
 <section className="hero"><div><span>Overall progress</span><strong>{pct}%</strong></div><div className="progress"><i style={{width:`${pct}%`}}/></div><div className="stats"><span>{done} completed</span><span>{tasks.length-done} remaining</span><span>Target: ~300 DSA problems</span></div></section>
 <section className="grid"><div className="card wide"><div className="section-head"><div><h2>Week {selected.week}</h2><p className="muted">{selected.theme} · {selected.focus}</p></div><strong>{selectedPct}% complete</strong></div><div className="week-picker">{weeks.map(w=><button key={w.week} className={w.week===currentWeek?'active':''} onClick={()=>setCurrentWeek(w.week)}>W{w.week}</button>)}</div>{selected.tasks.map((t:Task)=><label className="task" key={t.id}><input type="checkbox" checked={!!completed[t.id]} disabled={saving===t.id} onChange={e=>toggle(t.id,e.target.checked)}/><span><b>{t.title}</b><small>{t.track} · {t.minutes} min{saving===t.id?' · saving…':''}</small></span></label>)}</div>
 <div className="card"><h2>Skill matrix</h2>{tracks.map(x=><div className="skill" key={x.name}><div><span>{x.name}</span><b>{x.pct}%</b></div><div className="bar"><i style={{width:`${x.pct}%`}}/></div></div>)}</div>
 <div className="card wide"><h2>26-week roadmap</h2><div className="weeks">{weeks.map(w=><button className={`week ${w.week===currentWeek?'selected':''}`} key={w.week} onClick={()=>setCurrentWeek(w.week)}><span>W{w.week}</span><div><b>{w.theme}</b><small>{w.focus}</small></div></button>)}</div></div>
 <div className="card"><h2>Recognition pipeline</h2><div className="pipeline"><span>GitHub projects</span><span>Technical writing</span><span>Open-source PRs</span><span>Networking</span><span>Referrals</span><span>Applications</span></div></div></section><footer>Persistence: Next.js → Spring Boot → H2</footer></main>
}