## DocRelay 
DocRelay is a lightweight, self-hosted file-sharing service written in pure Java (no Spring) with a Next.js front-end. Upload a file through the UI, receive a one-time download port number, and share it with anyone on your network to obtain the file.

---

## ✨ Key Features

- **Drag-and-drop uploads** (UI on port 3000)
- **Port-based one-shot download links** returned as JSON
- Zero external services – runs entirely on your own server/VM
- CORS-enabled API (`http://localhost:9000`)

---

## Tech Stack

| Layer    | Tech                                          |
| -------- | --------------------------------------------- |
| Backend  | Java 17 ▸ `com.sun.net.httpserver.HttpServer` |
| Frontend | Next.js                                       |
| Build    | Maven                                         |
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

```


## How it works
### File Upload:
User uploads a file through the UI
The file is sent to the Java backend
The backend assigns a unique port number (invite code)
The backend starts a file server on that port

### File Sharing:
The user shares the invite code with another user
The other user enters the invite code in their UI

### File Download:
The UI connects to the specified port
The file is transferred directly from the host to the recipient
