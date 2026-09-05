'use client';
import { useMemo, useState } from 'react';
import roadmap from '../data/roadmap.json';

type Task = { id:string; title:string; track:string; minutes:number; done?:boolean };
export default function Home() {
  const [completed, setCompleted] = useState<Record<string,boolean>>({});
  const tasks = roadmap.flatMap((w:any)=>w.tasks as Task[]);
  const done = tasks.filter(t=>completed[t.id]).length;
  const pct = Math.round((done / Math.max(tasks.length,1))*100);
  const today = roadmap[0];
  const tracks = useMemo(() => {
    const names = ['DSA','Java','JVM Performance','System Design','Distributed Systems','AI Engineering','Recognition'];
    return names.map(name => {
      const all = tasks.filter(t=>t.track===name);
      const d = all.filter(t=>completed[t.id]).length;
      return {name, pct: Math.round((d/Math.max(all.length,1))*100)};
    });
  }, [completed, tasks]);
  return <main>
    <header className="topbar"><div><div className="eyebrow">6-MONTH CAREER SPRINT</div><h1>FAANG Engineering Tracker</h1><p>Java • JVM Performance • DSA • Distributed Systems • System Design • AI</p></div><div className="badge">Week {today.week} / 26</div></header>
    <section className="hero"><div><span>Overall progress</span><strong>{pct}%</strong></div><div className="progress"><i style={{width:`${pct}%`}}/></div><div className="stats"><span>{done} completed</span><span>{tasks.length-done} remaining</span><span>Target: ~300 DSA problems</span></div></section>
    <section className="grid">
      <div className="card wide"><h2>Today</h2><p className="muted">{today.theme}</p>{today.tasks.map((t:Task)=><label className="task" key={t.id}><input type="checkbox" checked={!!completed[t.id]} onChange={e=>setCompleted({...completed,[t.id]:e.target.checked})}/><span><b>{t.title}</b><small>{t.track} · {t.minutes} min</small></span></label>)}</div>
      <div className="card"><h2>Skill matrix</h2>{tracks.map(x=><div className="skill" key={x.name}><div><span>{x.name}</span><b>{x.pct}%</b></div><div className="bar"><i style={{width:`${x.pct}%`}}/></div></div>)}</div>
      <div className="card wide"><h2>26-week roadmap</h2><div className="weeks">{roadmap.map((w:any)=><div className="week" key={w.week}><span>W{w.week}</span><div><b>{w.theme}</b><small>{w.focus}</small></div></div>)}</div></div>
      <div className="card"><h2>Recognition pipeline</h2><div className="pipeline"><span>GitHub projects</span><span>Technical writing</span><span>Open-source PRs</span><span>Networking</span><span>Referrals</span><span>Applications</span></div></div>
    </section><footer>Next: connect PostgreSQL + authentication + GitHub Actions + Vercel deployment.</footer>
  </main>
}