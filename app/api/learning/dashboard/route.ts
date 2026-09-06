import { NextResponse } from 'next/server';
const backend = process.env.BACKEND_URL ?? 'http://localhost:8080';
export async function GET(){try{const r=await fetch(`${backend}/api/learning/dashboard`,{cache:'no-store'});const data=await r.json().catch(()=>({}));return NextResponse.json(data,{status:r.status});}catch{return NextResponse.json({error:'Backend unavailable'},{status:502});}}
