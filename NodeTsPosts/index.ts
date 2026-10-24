import path from 'node:path'
import fs from 'node:fs'
import { createApp } from './app.ts'

// Configuración por entorno (ver .env.example)
const PORT = Number(process.env.PORT ?? 3001)
const UPLOAD_DIR = path.resolve(process.env.UPLOAD_DIR ?? 'upload')

fs.mkdirSync(UPLOAD_DIR, { recursive: true })

createApp(UPLOAD_DIR).listen(PORT, () => {
    console.log(`media service on http://localhost:${PORT}`)
})
