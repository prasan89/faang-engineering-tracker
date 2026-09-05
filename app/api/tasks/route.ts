import { NextResponse } from 'next/server';

const API_URL = process.env.BACKEND_URL ?? 'http://localhost:8080';

export async function GET() {
  try {
    const response = await fetch(`${API_URL}/api/tasks`, { cache: 'no-store' });
    if (!response.ok) return NextResponse.json({ error: 'Backend unavailable' }, { status: 502 });
    return NextResponse.json(await response.json());
  } catch {
    return NextResponse.json({ error: 'Backend unavailable' }, { status: 502 });
  }
}

export async function POST(request: Request) {
  try {
    const body = await request.json();
    if (typeof body.taskId !== 'string' || typeof body.completed !== 'boolean') {
      return NextResponse.json({ error: 'taskId and completed are required' }, { status: 400 });
    }
    const response = await fetch(`${API_URL}/api/tasks`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
    const data = await response.json();
    return NextResponse.json(data, { status: response.status });
  } catch {
    return NextResponse.json({ error: 'Backend unavailable' }, { status: 502 });
  }
}
