import api from '../services/api'

/**
 * Opens a report in a new tab without popup-blocker issues.
 * The tab is opened SYNCHRONOUSLY before any await, then HTML is written into it.
 */
export async function openReport(relativeUrl) {
  // Open blank tab immediately (must be synchronous to avoid popup blocking)
  const tab = window.open('', '_blank')
  if (!tab) {
    alert('Allow popups for this site, then try again.')
    return
  }

  // Show loading state
  tab.document.write(`<html><body style="background:#0F172A;color:#E2E8F0;font-family:sans-serif;
    display:flex;align-items:center;justify-content:center;height:100vh;margin:0">
    <div style="text-align:center">
      <div style="width:40px;height:40px;border:3px solid #667D9D;border-top-color:transparent;
        border-radius:50%;animation:spin 1s linear infinite;margin:0 auto 16px"></div>
      <p>Loading report…</p>
      <style>@keyframes spin{to{transform:rotate(360deg)}}</style>
    </div></body></html>`)

  try {
    const res = await api.get(relativeUrl, { responseType: 'text' })
    tab.document.open()
    tab.document.write(res.data)
    tab.document.close()
  } catch (e) {
    tab.document.open()
    tab.document.write(`<!DOCTYPE html><html><body style="background:#0F172A;color:#E2E8F0;
      font-family:sans-serif;padding:40px">
      <h2 style="color:#EF4444">Could not load report</h2>
      <p>${e?.response?.status === 404 ? 'No data found for this session.' : e.message}</p>
      <p style="color:#64748B;font-size:13px">Make sure the backend is running and you have session data.</p>
    </body></html>`)
    tab.document.close()
  }
}
