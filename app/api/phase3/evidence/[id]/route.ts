import {NextRequest,NextResponse} from 'next/server';
const base=()=>process.env.BACKEND_URL||'http://localhost:8080';
export async function PATCH(req:NextRequest,{params}:{params:Promise<{id:string}>}){const{id}=await params;const r=await fetch(`${base()}/api/phase3/evidence/${id}`,{method:'PATCH',headers:{'Content-Type':'application/json'},body:await req.text()});return new NextResponse(await r.text(),{status:r.status,headers:{'content-type':'application/json'}})}
export async function DELETE(req:NextRequest,{params}:{params:Promise<{id:string}>}){const{id}=await params;const r=await fetch(`${base()}/api/phase3/evidence/${id}`,{method:'DELETE'});return new NextResponse(await r.text(),{status:r.status,headers:{'content-type':'application/json'}})}
