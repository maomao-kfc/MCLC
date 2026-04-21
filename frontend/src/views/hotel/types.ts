// 酒店类型定义

export interface Hotel {
  id?: number;
  name: string;
  phone?: string;
  address?: string;
  description?: string;
  status: 0 | 1;
  createTime?: string;
  updateTime?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}
