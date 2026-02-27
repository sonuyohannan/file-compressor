# File Compressor (Spring Boot + React)

This project is a starter full-stack app for compressing many file types:
- **Images** (`jpg`, `jpeg`, `png`, `bmp`, `webp`) are re-encoded using adjustable quality.
- **PDF, DOC, DOCX and all other file types** are compressed as **GZIP** (`.gz`) for universal support.

## Project structure

- `backend/` - Spring Boot API (`POST /api/compress`)
- `frontend/` - React + Vite web UI

## Run backend

```bash
cd backend
mvn spring-boot:run
```

Backend runs on `http://localhost:8080`.

## Run frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:5173`.

## API usage

`POST /api/compress` multipart form data:
- `file`: uploaded file
- `quality`: image quality between `0.1` and `1.0` (default `0.7`)

Returns downloadable bytes with `Content-Disposition: attachment`.
