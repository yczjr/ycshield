import http from './http'

export const createEvent = (data: any) => http.post('/events', data)
export const pageEvents = (data: any) => http.post('/events/page', data)
export const getEvent = (id: number | string) => http.get('/events/' + id)
export const claimEvent = (id: number) => http.post(`/events/${id}/claim`)
export const analyzeEvent = (id: number, data: any) => http.post(`/events/${id}/analyze`, data)
export const assignEvent = (id: number, data: any) => http.post(`/events/${id}/assign`, data)
export const disposeEvent = (id: number, data: any) => http.post(`/events/${id}/dispose`, data)
export const submitReview = (id: number) => http.post(`/events/${id}/submit-review`)
export const reviewEvent = (id: number, data: any) => http.post(`/events/${id}/review`, data)
export const closeEvent = (id: number, data: any) => http.post(`/events/${id}/close`, data)
export const finishEvent = (id: number, data: any) => http.post(`/events/${id}/finish`, data)
export const blockIpEvent = (id: number, data: any) => http.post(`/events/${id}/block-ip`, data)
export const checkDuplicate = (data: any) => http.post('/events/check-duplicate', data)
export const statOverview = () => http.get('/stat/overview')
export const statDashboard = () => http.get('/stat/dashboard')

export const uploadFile = (file: File, eventId: number, alarmId?: number) => {
  const fd = new FormData()
  fd.append('file', file)
  fd.append('eventId', String(eventId))
  if (alarmId) fd.append('alarmId', String(alarmId))
  return http.post('/files/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
}
