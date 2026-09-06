import { NextResponse } from 'next/server';
const backend = process.env.BACKEND_URL ?? 'http://localhost:8080';
export async function DELETE(_:Request,{params}:{params:Promise<{id:string}>}){try{const {id}=await params;const r=await fetch(`${backend}/api/learning/goals/${id}`,{method:'DELETE'});const data=await r.json().catch(()=>({}));return NextResponse.json(data,{status:r.status});}catch{return NextResponse.json({error:'Backend unavailable'},{status:502});}}
