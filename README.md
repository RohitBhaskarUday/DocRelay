## DocRelay 
DocRelay is a lightweight, self-hosted file-sharing service written in pure Java (no Spring) with a Next.js front-end. Upload a file through the UI, receive a one-time download link (port‐based), and share it with anyone on your network.

---

## ✨ Key Features

- **Drag-and-drop uploads** (UI on port 3000)
- **Port-based one-shot download links** returned as JSON
- Zero external services – runs entirely on your own server/VM
- CORS-enabled API (`http://localhost:9000`)
- Single binary JAR & static React build for easy deployment

---

## Tech Stack

| Layer    | Tech                                          |
| -------- | --------------------------------------------- |
| Backend  | Java 17 ▸ `com.sun.net.httpserver.HttpServer` |
| Frontend | Next.js 14 + React 18                         |
| Build    | Maven 3.8 · npm 18                            |
| Other    | Apache Commons IO & FileUpload                |

---

## Quick Start (Local Dev)

```bash
# 1 Clone
git clone https://github.com/<your-user>/DocRelay.git
cd DocRelay

# 2 Backend
mvn clean package            # builds target/docrelay-<ver>.jar
java -jar target/docrelay-*.jar
# → API on http://localhost:9000

# 3 Frontend (new terminal)
cd ui
npm install                  # first time only
npm run dev                  # http://localhost:3000
