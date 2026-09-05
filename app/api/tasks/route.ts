import { NextResponse } from "next/server";
import { getTaskCompletions, setTaskCompletion } from "../../../lib/h2";

export async function GET() {
  return NextResponse.json({ completions: getTaskCompletions() });
}

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const taskId = typeof body.taskId === "string" ? body.taskId.trim() : "";
    const completed = typeof body.completed === "boolean" ? body.completed : null;

    if (!taskId || completed === null) {
      return NextResponse.json(
        { error: "taskId and completed are required" },
        { status: 400 }
      );
    }

    return NextResponse.json(setTaskCompletion(taskId, completed));
  } catch {
    return NextResponse.json({ error: "Invalid request" }, { status: 400 });
  }
}
