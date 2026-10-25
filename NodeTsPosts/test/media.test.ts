import { test, before, after } from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs/promises'
import os from 'node:os'
import path from 'node:path'
import type { AddressInfo } from 'node:net'
import type { Server } from 'node:http'
import sharp from 'sharp'
import { createApp } from '../app.ts'

// Regresión de seguridad del servicio de media (auditoría SEC-15..SEC-18, tarea 6.2).
let server: Server
let base: string
let uploadDir: string
let parentDir: string

before(async () => {
  parentDir = await fs.mkdtemp(path.join(os.tmpdir(), 'media-test-'))
  uploadDir = path.join(parentDir, 'upload')
  await fs.mkdir(uploadDir)
  server = createApp(uploadDir).listen(0)
  await new Promise((r) => server.once('listening', r))
  base = `http://127.0.0.1:${(server.address() as AddressInfo).port}`
})

after(async () => {
  server.close()
  await fs.rm(parentDir, { recursive: true, force: true })
})

async function upload(content: Buffer | Uint8Array, name: string, type = 'image/png', extra?: (f: FormData) => void) {
  const form = new FormData()
  form.append('miArchivo', new Blob([content], { type }), name)
  extra?.(form)
  return fetch(`${base}/subirImagen`, { method: 'POST', body: form })
}

const png = () =>
  sharp({ create: { width: 8, height: 8, channels: 3, background: '#3366ff' } })
    .png()
    .toBuffer()

async function stored(): Promise<string[]> {
  return (await fs.readdir(uploadDir)).sort()
}

test('acepta un PNG válido, lo guarda como UUID.png y la respuesta no filtra rutas del servidor', async () => {
  const res = await upload(await png(), 'foto.png')
  assert.equal(res.status, 201)
  const body = await res.json()
  assert.match(body.data.filename, /^[0-9a-f-]{36}\.png$/)
  const text = JSON.stringify(body)
  assert.ok(!text.includes(uploadDir) && !text.includes('path') && !text.includes('destination'))
  assert.ok((await stored()).includes(body.data.filename))
})

test('HTML disfrazado de imagen -> 415 y no se almacena', async () => {
  const before = await stored()
  const res = await upload(Buffer.from('<html><script>alert(1)</script></html>'), 'ataque.png')
  assert.equal(res.status, 415)
  assert.deepEqual(await stored(), before)
})

test('SVG -> 415', async () => {
  const svg = '<svg xmlns="http://www.w3.org/2000/svg"><script>alert(1)</script></svg>'
  const res = await upload(Buffer.from(svg), 'logo.svg', 'image/svg+xml')
  assert.equal(res.status, 415)
})

test('archivo de 12 MB -> 413 sin restos en disco', async () => {
  const before = await stored()
  const big = Buffer.alloc(12 * 1024 * 1024)
  ;(await png()).copy(big) // cabecera PNG real; el resto, relleno
  const res = await upload(big, 'grande.png')
  assert.equal(res.status, 413)
  assert.deepEqual(await stored(), before)
})

test('más de un archivo por petición -> 400', async () => {
  const img = await png()
  const res = await upload(img, 'a.png', 'image/png', (f) => f.append('miArchivo', new Blob([img]), 'b.png'))
  assert.equal(res.status, 400)
})

test('nombre con path traversal se ignora: se guarda dentro de UPLOAD_DIR con nombre UUID', async () => {
  const res = await upload(await png(), '../../index.ts')
  assert.equal(res.status, 201)
  const { data } = await res.json()
  assert.match(data.filename, /^[0-9a-f-]{36}\.png$/)
  assert.deepEqual((await fs.readdir(parentDir)).sort(), ['upload'])
})

test('elimina EXIF/GPS de un JPEG', async () => {
  const jpeg = await sharp({ create: { width: 16, height: 16, channels: 3, background: '#ff0000' } })
    .jpeg()
    .withExif({
      IFD0: { Copyright: 'secreto', Artist: 'ana' },
      IFD3: { GPSLatitudeRef: 'N', GPSLatitude: '40/1 25/1 0/1' },
    })
    .toBuffer()
  assert.ok((await sharp(jpeg).metadata()).exif, 'la imagen de prueba debe llevar EXIF')
  const res = await upload(jpeg, 'gps.jpg', 'image/jpeg')
  assert.equal(res.status, 201)
  const { data } = await res.json()
  const served = await fetch(`${base}/uploads/${data.filename}`)
  const buf = Buffer.from(await served.arrayBuffer())
  assert.equal((await sharp(buf).metadata()).exif, undefined)
  assert.ok(!buf.includes(Buffer.from('secreto')))
})

test('servido con Content-Type image/* y nosniff; sin listado de directorio ni dotfiles', async () => {
  const res = await upload(await png(), 'x.png')
  const { data } = await res.json()
  const img = await fetch(`${base}/uploads/${data.filename}`)
  assert.equal(img.status, 200)
  assert.match(img.headers.get('content-type') ?? '', /^image\//)
  assert.equal(img.headers.get('x-content-type-options'), 'nosniff')

  await fs.writeFile(path.join(uploadDir, '.secret'), 'x')
  for (const p of [
    '/uploads/',
    '/uploads',
    '/uploads/.secret',
    '/uploads/..%2f..%2fpackage.json',
    '/uploads/%2e%2e/app.ts',
    '/uploads/%E0%A4%A',
  ]) {
    const r = await fetch(`${base}${p}`)
    assert.equal(r.status, 404, p)
    assert.ok(!(await r.text()).includes(data.filename), p)
  }
})
