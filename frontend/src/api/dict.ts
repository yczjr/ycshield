import http from './http'

let cache: any = null

export const getAllDict = async (force = false) => {
  if (cache && !force) return cache
  cache = await http.get('/dict/all')
  return cache
}

export const getDict = (type: string) => http.get('/dict/' + type)
export const getAttackTypeTree = () => http.get('/dict/attack-type/tree')
export const saveDictItem = (data: any) => http.post('/dict/item', data)
export const deleteDictItem = (id: number) => http.delete('/dict/item/' + id)
export const clearDictCache = () => { cache = null }

export const nameOf = (dict: any, type: string, code: string) => {
  const list = dict?.[type] || []
  const hit = list.find((x: any) => x.code === code)
  return hit ? hit.name : code
}
