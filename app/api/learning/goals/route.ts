import { NextResponse } from 'next/server';
const backend = process.env.BACKEND_URL ?? 'http://localhost:8080';
export async function GET(){try{const r=await fetch(`${backend}/api/learning/goals`,{cache:'no-store'});return NextResponse.json(await r.json(),{status:r.status});}catch{return NextResponse.json({error:'Backend unavailable'},{status:502});}}
export async function POST(req:Request){try{const r=await fetch(`${backend}/api/learning/goals`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(await req.json())});const data=await r.json().catch(()=>({}));return NextResponse.json(data,{status:r.status});}catch{return NextResponse.json({error:'Backend unavailable'},{status:502});}}
