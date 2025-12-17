// 本地存储封装
export const storage = {
  // 设置
  set(key: string, value: any): void {
    localStorage.setItem(key, JSON.stringify(value))
  },
  
  // 获取
  get(key: string): any {
    const item = localStorage.getItem(key)
    return item ? JSON.parse(item) : null
  },
  
  // 删除
  remove(key: string): void {
    localStorage.removeItem(key)
  },
  
  // 清空
  clear(): void {
    localStorage.clear()
  }
}