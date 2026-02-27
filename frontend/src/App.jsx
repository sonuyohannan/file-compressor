import { useState } from 'react'

const API_URL = 'http://localhost:8080/api/compress'

export default function App() {
  const [file, setFile] = useState(null)
  const [quality, setQuality] = useState(0.7)
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')

  const submit = async (event) => {
    event.preventDefault()

    if (!file) {
      setMessage('Please choose a file first.')
      return
    }

    const formData = new FormData()
    formData.append('file', file)
    formData.append('quality', quality)

    try {
      setLoading(true)
      setMessage('Compressing...')
      const response = await fetch(API_URL, {
        method: 'POST',
        body: formData
      })

      if (!response.ok) {
        throw new Error('Compression failed.')
      }

      const blob = await response.blob()
      const disposition = response.headers.get('content-disposition')
      const suggested = disposition?.match(/filename="?([^\"]+)"?/)?.[1] ?? `compressed-${file.name}`

      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = suggested
      document.body.appendChild(link)
      link.click()
      link.remove()
      URL.revokeObjectURL(url)

      setMessage('Compression complete. Download started.')
    } catch (error) {
      setMessage(error.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="container">
      <h1>Universal File Compressor</h1>
      <p>Compress PDFs, Word docs, images, and other files using Spring Boot + React.</p>

      <form onSubmit={submit} className="card">
        <label>
          Choose file
          <input type="file" onChange={(e) => setFile(e.target.files?.[0] ?? null)} />
        </label>

        <label>
          Image quality ({Math.round(quality * 100)}%)
          <input
            type="range"
            min="0.1"
            max="1"
            step="0.05"
            value={quality}
            onChange={(e) => setQuality(Number(e.target.value))}
          />
          <small>Used for image compression. Non-images are delivered as .gz.</small>
        </label>

        <button type="submit" disabled={loading}>
          {loading ? 'Compressing...' : 'Compress File'}
        </button>
      </form>

      {message && <p className="message">{message}</p>}
    </main>
  )
}
