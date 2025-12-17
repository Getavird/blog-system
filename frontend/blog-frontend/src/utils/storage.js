// 本地存储封装
export const storage = {
  // 设置
  set(key, value) {
    localStorage.setItem(key, JSON.stringify(value))
  },
  
  // 获取
  get(key) {
    const item = localStorage.getItem(key)
    return item ? JSON.parse(item) : null
  },
  
  // 删除
  remove(key) {
    localStorage.removeItem(key)
  },
  
  // 清空
  clear() {
    localStorage.clear()
  }
}