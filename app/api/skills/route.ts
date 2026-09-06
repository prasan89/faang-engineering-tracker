import { NextResponse } from 'next/server';

const backend = process.env.BACKEND_URL ?? 'http://localhost:8080';

export async function GET() {
  try {
    const r = await fetch(`${backend}/api/skills`, { cache: 'no-store' });
    if (!r.ok) throw new Error();
    return NextResponse.json(await r.json());
  } catch {
    return NextResponse.json({ error: 'Backend unavailable' }, { status: 502 });
  }
}

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const r = await fetch(`${backend}/api/skills`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
    const data = await r.json().catch(() => ({}));
    return NextResponse.json(data, { status: r.status });
  } catch {
    return NextResponse.json({ error: 'Backend unavailable' }, { status: 502 });
  }
}
