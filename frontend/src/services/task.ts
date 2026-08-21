import request from '@/utils/request';
import type { ApiResponse, Task, TaskExecutionLog, PageParams, PageResult } from '@/types';

// 获取任务列表
export const getTaskList = (params?: PageParams & { taskName?: string; taskType?: string; status?: string }) => {
  return request.get<ApiResponse<PageResult<Task>>>('/task/page', { params });
};

// 获取任务详情
export const getTaskDetail = (id: number) => {
  return request.get<ApiResponse<Task>>(`/task/${id}`);
};

// 创建任务
export const createTask = (data: Partial<Task>) => {
  return request.post<ApiResponse<Task>>('/task', data);
};

// 更新任务
export const updateTask = (id: number, data: Partial<Task>) => {
  return request.put<ApiResponse<Task>>(`/task/${id}`, data);
};

// 删除任务
export const deleteTask = (id: number) => {
  return request.delete<ApiResponse<void>>(`/task/${id}`);
};

// 启用任务
export const enableTask = (id: number) => {
  return request.post<ApiResponse<void>>(`/task/${id}/enable`);
};

// 禁用任务
export const disableTask = (id: number) => {
  return request.post<ApiResponse<void>>(`/task/${id}/disable`);
};

// 立即执行任务
export const executeTask = (id: number) => {
  return request.post<ApiResponse<void>>(`/task/${id}/execute`);
};

// 获取任务执行日志
export const getTaskExecutionLogs = (params?: PageParams & { taskId?: number; status?: string }) => {
  return request.get<ApiResponse<PageResult<TaskExecutionLog>>>('/task/logs', { params });
};

// 获取任务统计
export const getTaskStatistics = (taskId: number) => {
  return request.get<ApiResponse<any>>(`/task/${taskId}/statistics`);
};
