import request from '@/utils/request';
import type { ApiResponse, Cluster, PageParams, PageResult } from '@/types';

// 获取集群列表
export const getClusterList = (params?: PageParams) => {
  return request.get<ApiResponse<PageResult<Cluster>>>('/cluster/page', { params });
};

// 获取所有集群
export const getAllClusters = () => {
  return request.get<ApiResponse<Cluster[]>>('/cluster/list');
};

// 获取集群详情
export const getClusterDetail = (id: number) => {
  return request.get<ApiResponse<Cluster>>(`/cluster/${id}`);
};

// 创建集群
export const createCluster = (data: Partial<Cluster>) => {
  return request.post<ApiResponse<Cluster>>('/cluster', data);
};

// 更新集群
export const updateCluster = (id: number, data: Partial<Cluster>) => {
  return request.put<ApiResponse<Cluster>>(`/cluster/${id}`, data);
};

// 删除集群
export const deleteCluster = (id: number) => {
  return request.delete<ApiResponse<void>>(`/cluster/${id}`);
};

// 测试集群连接
export const testClusterConnection = (data: Partial<Cluster>) => {
  return request.post<ApiResponse<{ success: boolean; message: string }>>('/cluster/test', data);
};
