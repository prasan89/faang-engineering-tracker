'use client';

import { useEffect, useMemo, useState } from 'react';

type Problem = {
  id: string;
  title: string;
  url: string;
  difficulty: string;
  pattern: string;
  status: 'NOT_STARTED' | 'SOLVED' | 'REVIEW';
  attempts: number;
  time_minutes: number;
  notes?: string | null;
};

const TARGET = 300;
const statuses = ['ALL', 'NOT_STARTED', 'REVIEW', 'SOLVED'] as const;

export default function DsaPage() {
  const [problems, setProblems] = useState<Problem[]>([]);
  const [pattern, setPattern] = useState('ALL');
  const [difficulty, setDifficulty] = useState('ALL');
  const [status, setStatus] = useState<(typeof statuses)[number]>('ALL');
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [saving, setSaving] = useState<string | null>(null);

  useEffect(() => {
    fetch('/api/dsa', { cache: 'no-store' })
      .then((r) => { if (!r.ok) throw new Error(); return r.json(); })
      .then(setProblems)
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  }, []);

  const solved = problems.filter((p) => p.status === 'SOLVED').length;
  const review = problems.filter((p) => p.status === 'REVIEW').length;
  const targetPct = Math.min(100, Math.round((solved / TARGET) * 100));

  const patterns = useMemo(() => ['ALL', ...Array.from(new Set(problems.map((p) => p.pattern))).sort()], [problems]);
  const difficulties = useMemo(() => ['ALL', ...Array.from(new Set(problems.map((p) => p.difficulty))).sort()], [problems]);

  const filtered = problems.filter((p) =>
    (pattern === 'ALL' || p.pattern === pattern) &&
    (difficulty === 'ALL' || p.difficulty === difficulty) &&
    (status === 'ALL' || p.status === status) &&
    p.title.toLowerCase().includes(search.toLowerCase())
  );

  async function updateProblem(problem: Problem, nextStatus: Problem['status']) {
    const previous = problem;
    const next = { ...problem, status: nextStatus, solved_at: undefined };
    setProblems((items) => items.map((p) => p.id === problem.id ? next : p));
    setSaving(problem.id);
    setError(false);
    try {
      const response = await fetch('/api/dsa', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          problemId: problem.id,
          status: nextStatus,
          attempts: problem.attempts,
          timeMinutes: problem.time_minutes,
          notes: problem.notes ?? ''
        })
      });
      if (!response.ok) throw new Error();
    } catch {
      setProblems((items) => items.map((p) => p.id === problem.id ? previous : p));
      setError(true);
    } finally {
      setSaving(null);
    }
  }

  return (
    <main>
      <header className="topbar">
        <div>
          <div className="eyebrow">FAANG ENGINEERING TRACKER</div>
          <h1>DSA Command Center</h1>
          <p>Pattern recognition → timed solving → review → interview readiness</p>
        </div>
        <a className="back-link" href="/">← Dashboard</a>
      </header>

      {error && <div className="notice">Could not save or load DSA progress. Make sure the Spring Boot backend is running.</div>}

      <section className="dsa-stats">
        <div className="metric"><span>Solved</span><strong>{solved}</strong><small>/ {TARGET} target</small></div>
        <div className="metric"><span>In review</span><strong>{review}</strong><small>needs another pass</small></div>
        <div className="metric"><span>Remaining</span><strong>{Math.max(TARGET - solved, 0)}</strong><small>to reach target</small></div>
        <div className="metric"><span>Library</span><strong>{problems.length}</strong><small>currently loaded</small></div>
      </section>

      <section className="card dsa-progress-card">
        <div className="section-head"><div><h2>300-problem target</h2><p className="muted">Build breadth first, then repeatedly revisit weak patterns.</p></div><strong>{targetPct}%</strong></div>
        <div className="progress"><i style={{ width: `${targetPct}%` }} /></div>
      </section>

      <section className="card filters">
        <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search problems…" />
        <select value={pattern} onChange={(e) => setPattern(e.target.value)}>{patterns.map((x) => <option key={x} value={x}>{x === 'ALL' ? 'All patterns' : x}</option>)}</select>
        <select value={difficulty} onChange={(e) => setDifficulty(e.target.value)}>{difficulties.map((x) => <option key={x} value={x}>{x === 'ALL' ? 'All difficulty' : x}</option>)}</select>
        <select value={status} onChange={(e) => setStatus(e.target.value as typeof status)}>{statuses.map((x) => <option key={x} value={x}>{x === 'ALL' ? 'All status' : x.replace('_', ' ')}</option>)}</select>
      </section>

      <section className="card">
        <div className="section-head"><div><h2>Problem library</h2><p className="muted">Showing {filtered.length} of {problems.length} problems</p></div></div>
        {loading ? <p className="muted">Loading DSA library…</p> : filtered.length === 0 ? <p className="muted">No problems match the current filters.</p> : (
          <div className="dsa-list">
            {filtered.map((p) => (
              <div className="problem-row" key={p.id}>
                <div className="problem-main">
                  <a href={p.url} target="_blank" rel="noreferrer">{p.title} ↗</a>
                  <div><span className={`difficulty ${p.difficulty.toLowerCase()}`}>{p.difficulty}</span><span>{p.pattern}</span><span>{p.attempts} attempts</span>{p.time_minutes > 0 && <span>{p.time_minutes} min</span>}</div>
                </div>
                <select value={p.status} disabled={saving === p.id} onChange={(e) => updateProblem(p, e.target.value as Problem['status'])}>
                  <option value="NOT_STARTED">Not started</option>
                  <option value="REVIEW">Review</option>
                  <option value="SOLVED">Solved</option>
                </select>
              </div>
            ))}
          </div>
        )}
      </section>
      <footer>DSA persistence: Next.js → Spring Boot → H2</footer>
    </main>
  );
}
