import http from './http'

export const login = (data: { username: string; password: string }) =>
  http.post('/auth/login', data)

export const me = () => http.get('/auth/me')

export const listUsers = () => http.get('/admin/users')
export const createUser = (data: any) => http.post('/admin/users', data)
export const listHandlers = () => http.get('/users/handlers')

export const resetUserPassword = (id: number, password: string) =>
  http.post(`/admin/users/${id}/password`, { password })

export const changeMyPassword = (oldPassword: string, newPassword: string) =>
  http.post('/auth/password', { oldPassword, newPassword })
