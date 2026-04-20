HMCTS TASK MANAGER API DOCUMENTATION

=====================================
BASE URL
=====================================
http://localhost:8080/api

=====================================
ENDPOINTS (Things you can do)
=====================================

1. CREATE a task
   POST to: /tasks
   Example data to send:
   {
     "title": "My Task",
     "description": "Do something",
     "status": "pending",
     "dueDate": "2026-12-31T17:00:00"
   }

2. GET all tasks
   GET to: /tasks

3. GET one task
   GET to: /tasks/1

4. UPDATE task status
   PUT to: /tasks/1/status
   Send: "completed"

5. UPDATE entire task
   PUT to: /tasks/1
   Send same data as CREATE

6. DELETE task
   DELETE to: /tasks/1