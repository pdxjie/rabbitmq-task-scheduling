import request from '@/utils/request';
import type { ApiResponse, SystemMetrics, ClusterMetrics, QueueMetrics } from '@/types';

// 获取系统监控指标
export const getSystemMetrics = () => {
  return request.get<ApiResponse<SystemMetrics>>('/monitor/system');
};

// 获取集群监控指标
export const getClusterMetrics = (clusterId: number) => {
  return request.get<ApiResponse<ClusterMetrics>>(`/monitor/cluster/${clusterId}`);
};

// 获取所有集群监控指标
export const getAllClusterMetrics = () => {
  return request.get<ApiResponse<ClusterMetrics[]>>('/monitor/cluster/all');
};

// 获取队列监控指标
export const getQueueMetrics = (clusterId: number) => {
  return request.get<ApiResponse<QueueMetrics[]>>(`/monitor/cluster/${clusterId}/queues`);
};

// 刷新监控数据
export const refreshMonitorData = () => {
  return request.post<ApiResponse<void>>('/monitor/refresh');
};
