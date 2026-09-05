-- Starter data for local H2 development

MERGE INTO task_completion (task_id, completed, completed_at)
KEY(task_id)
VALUES ('w1-dsa', FALSE, NULL);

MERGE INTO task_completion (task_id, completed, completed_at)
KEY(task_id)
VALUES ('w1-java', FALSE, NULL);

MERGE INTO task_completion (task_id, completed, completed_at)
KEY(task_id)
VALUES ('w1-jvm', FALSE, NULL);

MERGE INTO task_completion (task_id, completed, completed_at)
KEY(task_id)
VALUES ('w1-system-design', FALSE, NULL);

MERGE INTO task_completion (task_id, completed, completed_at)
KEY(task_id)
VALUES ('w1-ai', FALSE, NULL);

MERGE INTO task_completion (task_id, completed, completed_at)
KEY(task_id)
VALUES ('w1-recognition', FALSE, NULL);
