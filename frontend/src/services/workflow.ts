import request from '@/utils/request';
import type { ApiResponse, Workflow, PageParams, PageResult } from '@/types';

// 获取工作流列表
export const getWorkflowList = (params?: PageParams & { clusterId?: number }) => {
  return request.get<ApiResponse<PageResult<Workflow>>>('/workflow/page', { params });
};

// 获取工作流详情
export const getWorkflowDetail = (id: number) => {
  return request.get<ApiResponse<Workflow>>(`/workflow/${id}`);
};

// 创建工作流
export const createWorkflow = (clusterId: number, data: Partial<Workflow>) => {
  return request.post<ApiResponse<Workflow>>(`/workflow?clusterId=${clusterId}`, data);
};

// 更新工作流
export const updateWorkflow = (id: number, data: Partial<Workflow>) => {
  return request.put<ApiResponse<Workflow>>(`/workflow/${id}`, data);
};

// 删除工作流
export const deleteWorkflow = (id: number) => {
  return request.delete<ApiResponse<void>>(`/workflow/${id}`);
};

// 启用工作流
export const enableWorkflow = (id: number) => {
  return request.post<ApiResponse<void>>(`/workflow/${id}/enable`);
};

// 禁用工作流
export const disableWorkflow = (id: number) => {
  return request.post<ApiResponse<void>>(`/workflow/${id}/disable`);
};

// 执行工作流
export const executeWorkflow = (id: number) => {
  return request.post<ApiResponse<void>>(`/workflow/${id}/execute`);
};

// 验证 DAG
export const validateDAG = (data: { nodes: any[]; edges: any[] }) => {
  return request.post<ApiResponse<{ valid: boolean; message?: string }>>('/workflow/validate-dag', data);
};
