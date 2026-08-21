import request from '@/utils/request';
import type { ApiResponse, AlertRule, AlertRecord, PageParams, PageResult } from '@/types';

// 获取告警规则列表
export const getAlertRuleList = (params?: PageParams & { ruleType?: string; clusterId?: number }) => {
  return request.get<ApiResponse<PageResult<AlertRule>>>('/alert/rule/list', { params });
};

// 获取告警规则详情
export const getAlertRuleDetail = (id: number) => {
  return request.get<ApiResponse<AlertRule>>(`/alert/rule/${id}`);
};

// 创建告警规则
export const createAlertRule = (data: Partial<AlertRule>) => {
  return request.post<ApiResponse<AlertRule>>('/alert/rule', data);
};

// 更新告警规则
export const updateAlertRule = (id: number, data: Partial<AlertRule>) => {
  return request.put<ApiResponse<AlertRule>>(`/alert/rule/${id}`, data);
};

// 删除告警规则
export const deleteAlertRule = (id: number) => {
  return request.delete<ApiResponse<void>>(`/alert/rule/${id}`);
};

// 启用告警规则
export const enableAlertRule = (id: number) => {
  return request.post<ApiResponse<void>>(`/alert/rule/${id}/enable`);
};

// 禁用告警规则
export const disableAlertRule = (id: number) => {
  return request.post<ApiResponse<void>>(`/alert/rule/${id}/disable`);
};

// 测试告警发送
export const testAlert = (ruleId: number) => {
  return request.post<ApiResponse<void>>(`/alert/test`, null, { params: { ruleId } });
};

// 获取告警记录列表
export const getAlertRecordList = (params?: PageParams & { level?: string; status?: string }) => {
  return request.get<ApiResponse<PageResult<AlertRecord>>>('/alert/record/page', { params });
};
