/** Entrega ao navegador um arquivo já baixado, como se o usuário tivesse clicado num link. */
export function saveFile(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  // Libera a URL só depois que o navegador começou o download.
  setTimeout(() => URL.revokeObjectURL(url))
}
