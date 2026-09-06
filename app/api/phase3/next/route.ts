import {NextResponse} from 'next/server';
const base=()=>process.env.BACKEND_URL||'http://localhost:8080';
export async function GET(){const r=await fetch(`${base()}/api/phase3/next`,{cache:'no-store'});return new NextResponse(await r.text(),{status:r.status,headers:{'content-type':'application/json'}})}
