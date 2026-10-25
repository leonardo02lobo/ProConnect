import express, { type NextFunction, type Request, type Response } from 'express'
import multer from 'multer'
import path from 'node:path'
import fs from 'node:fs/promises'
import { randomUUID } from 'node:crypto'
import { fileTypeFromBuffer } from 'file-type'
import sharp from 'sharp'

/** Tamaño máximo por archivo (5 MB). */
export const MAX_FILE_SIZE = 5 * 1024 * 1024

/** Allowlist por contenido (magic bytes): MIME detectado -> extensión y formato de salida de sharp. */
const ALLOWED = {
  'image/jpeg': { ext: 'jpg', format: 'jpeg' },
  'image/png': { ext: 'png', format: 'png' },
  'image/webp': { ext: 'webp', format: 'webp' },
  'image/gif': { ext: 'gif', format: 'gif' },
} as const

type AllowedMime = keyof typeof ALLOWED

/** Nombres que este servicio genera: UUID v4 + extensión permitida. */
const STORED_NAME = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\.(jpg|png|webp|gif)$/

class HttpError extends Error {
  constructor(
    readonly status: number,
    message: string,
  ) {
    super(message)
  }
}

/**
 * Servicio de media endurecido (verify-web-security):
 * - subida en memoria con límite de 5 MB y un solo archivo (413 / 400),
 * - tipo validado por magic bytes con allowlist JPEG/PNG/WebP/GIF (415; SVG/HTML/ejecutables fuera),
 * - re-encode con sharp: elimina EXIF/GPS y neutraliza polyglots,
 * - nombre UUID.<ext> generado en el servidor dentro de UPLOAD_DIR (nada del nombre original),
 * - servido sin listado de directorios, sin dotfiles, con nosniff y Content-Type del tipo detectado.
 * La autenticación la exige el gateway en todo /upload/** (este servicio solo debe ser accesible a través de él).
 */
export function createApp(uploadDir: string) {
  const app = express()
  app.disable('x-powered-by')

  const upload = multer({
    storage: multer.memoryStorage(),
    limits: { fileSize: MAX_FILE_SIZE, files: 1, fields: 5, parts: 6 },
  })

  app.get('/health', (_req, res) => {
    res.send({ status: 'UP' })
  })

  app.post('/subirImagen', upload.single('miArchivo'), async (req: Request, res: Response, next: NextFunction) => {
    try {
      if (!req.file) {
        throw new HttpError(400, 'Falta el archivo (campo miArchivo)')
      }
      const detected = await fileTypeFromBuffer(req.file.buffer)
      if (!detected || !(detected.mime in ALLOWED)) {
        throw new HttpError(415, 'Tipo de archivo no permitido (solo JPEG, PNG, WebP o GIF)')
      }
      const { ext, format } = ALLOWED[detected.mime as AllowedMime]
      let output: Buffer
      try {
        output = await sharp(req.file.buffer, {
          animated: format === 'gif' || format === 'webp',
          limitInputPixels: 40_000_000,
        })
          .rotate() // aplica la orientación EXIF antes de descartar los metadatos
          .toFormat(format)
          .toBuffer() // sharp no copia metadatos salvo que se pida withMetadata()
      } catch {
        throw new HttpError(415, 'La imagen no se pudo procesar')
      }
      const filename = `${randomUUID()}.${ext}`
      await fs.writeFile(path.join(uploadDir, filename), output, { flag: 'wx' })
      res.status(201).send({
        message: 'Archivo subido exitosamente',
        data: { filename, mimetype: detected.mime, size: output.length },
      })
    } catch (err) {
      next(err)
    }
  })

  app.use(
    '/uploads',
    (req, res, next) => {
      // Solo nombres generados por el servicio; cualquier otra ruta (directorios, traversal, dotfiles) es 404.
      let name = ''
      try {
        name = decodeURIComponent(req.path.replace(/^\/+/, ''))
      } catch {
        name = ''
      }
      if (!STORED_NAME.test(name)) {
        res.status(404).send({ status: 404, error: 'Not Found' })
        return
      }
      next()
    },
    express.static(uploadDir, {
      index: false,
      dotfiles: 'deny',
      redirect: false,
      fallthrough: true,
      setHeaders: (res) => {
        res.setHeader('X-Content-Type-Options', 'nosniff')
        res.setHeader('Cache-Control', 'private, max-age=86400')
        res.setHeader('Content-Disposition', 'inline')
        res.setHeader('Content-Security-Policy', "default-src 'none'; img-src 'self'; sandbox")
        res.setHeader('Cross-Origin-Resource-Policy', 'same-site')
      },
    }),
  )

  app.use((_req, res) => {
    res.status(404).send({ status: 404, error: 'Not Found' })
  })

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  app.use((err: unknown, _req: Request, res: Response, _next: NextFunction) => {
    if (err instanceof multer.MulterError) {
      const status = err.code === 'LIMIT_FILE_SIZE' ? 413 : 400
      res.status(status).send({
        status,
        error: err.code,
        message: status === 413 ? 'Archivo demasiado grande (máx. 5 MB)' : 'Petición de subida inválida',
      })
      return
    }
    if (err instanceof HttpError) {
      res.status(err.status).send({ status: err.status, message: err.message })
      return
    }
    // Sin detalles internos (rutas, stack) en la respuesta.
    console.error('media: error inesperado', err instanceof Error ? err.message : err)
    res.status(500).send({ status: 500, message: 'Error interno' })
  })

  return app
}
