import fs from "node:fs";
import path from "node:path";

// Lightweight H2-compatible persistence contract.
// The Next.js app talks to this module through API routes so the UI is not
// coupled directly to database details.

type Completion = {
  taskId: string;
  completed: boolean;
  completedAt: string | null;
};

const dataDir = path.join(process.cwd(), "data");
const completionFile = path.join(dataDir, "task-completions.json");

function ensureStore() {
  fs.mkdirSync(dataDir, { recursive: true });
  if (!fs.existsSync(completionFile)) {
    fs.writeFileSync(completionFile, "{}", "utf8");
  }
}

function readStore(): Record<string, Completion> {
  ensureStore();
  return JSON.parse(fs.readFileSync(completionFile, "utf8"));
}

function writeStore(store: Record<string, Completion>) {
  ensureStore();
  fs.writeFileSync(completionFile, JSON.stringify(store, null, 2), "utf8");
}

export function getTaskCompletions(): Completion[] {
  return Object.values(readStore());
}

export function setTaskCompletion(taskId: string, completed: boolean): Completion {
  const store = readStore();
  const completion: Completion = {
    taskId,
    completed,
    completedAt: completed ? new Date().toISOString() : null,
  };
  store[taskId] = completion;
  writeStore(store);
  return completion;
}
