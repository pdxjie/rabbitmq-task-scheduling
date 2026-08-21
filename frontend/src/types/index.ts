// API 响应类型
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
}

// 分页参数
export interface PageParams {
  current: number;
  size: number;
}

// 分页响应
export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

// 集群类型
export interface Cluster {
  id: number;
  clusterName: string;
  connectionType: 'DIRECT' | 'SSH_TUNNEL' | 'TLS';
  host: string;
  port: number;
  managementPort: number;
  vhost: string;
  username: string;
  status: 'CONNECTED' | 'DISCONNECTED' | 'ERROR';
  healthScore?: number;
  createdAt: string;
  updatedAt: string;
}

// 任务类型
export interface Task {
  id: number;
  taskName: string;
  taskType: 'MESSAGE' | 'HTTP' | 'SHELL' | 'CODE';
  triggerType: 'CRON' | 'DELAY' | 'IMMEDIATE';
  cronExpression?: string;
  delaySeconds?: number;
  clusterId: number;
  clusterName?: string;
  queueName?: string;
  taskContent: string;
  status: 'ENABLED' | 'DISABLED';
  nextExecutionTime?: string;
  createdAt: string;
  updatedAt: string;
}

// 任务执行日志
export interface TaskExecutionLog {
  id: number;
  taskId: number;
  taskName: string;
  traceId: string;
  status: 'SUCCESS' | 'FAILED' | 'RUNNING';
  executionTime: number;
  errorMessage?: string;
  createdAt: string;
}

// 工作流
export interface Workflow {
  id: number;
  workflowName: string;
  description?: string;
  clusterId: number;
  dagDefinition: string;
  status: 'ENABLED' | 'DISABLED';
  version: number;
  createdAt: string;
  updatedAt: string;
}

// 工作流节点
export interface WorkflowNode {
  id: string;
  type: 'TASK' | 'CONDITION' | 'PARALLEL' | 'AGGREGATE';
  name: string;
  taskId?: number;
  config?: Record<string, any>;
}

// 工作流边
export interface WorkflowEdge {
  source: string;
  target: string;
  label?: string;
}

// 监控指标
export interface SystemMetrics {
  totalTasks: number;
  enabledTasks: number;
  runningTasks: number;
  todayExecutions: number;
  todaySuccess: number;
  todayFailed: number;
  successRate: number;
  avgExecutionTime: number;
  totalWorkflows: number;
  runningWorkflows: number;
  totalClusters: number;
  activeClusters: number;
  jvmHeapUsed: number;
  jvmHeapMax: number;
  threadCount: number;
  cpuUsage: number;
  collectTime: string;
}

// 集群监控指标
export interface ClusterMetrics {
  clusterId: number;
  clusterName: string;
  nodeCount: number;
  queueCount: number;
  exchangeCount: number;
  connectionCount: number;
  channelCount: number;
  consumerCount: number;
  messageCount: number;
  messagesReady: number;
  messagesUnacknowledged: number;
  publishRate: number;
  deliverRate: number;
  ackRate: number;
  memoryUsed: number;
  diskUsed: number;
  fdUsed: number;
  uptime: number;
  status: string;
  collectTime: string;
}

// 队列监控指标
export interface QueueMetrics {
  queueName: string;
  vhost: string;
  messageCount: number;
  messagesReady: number;
  messagesUnacknowledged: number;
  consumerCount: number;
  memoryUsed: number;
  publishRate: number;
  deliverRate: number;
  ackRate: number;
  state: string;
  durable: boolean;
}

// 告警规则
export interface AlertRule {
  id: number;
  ruleName: string;
  ruleType: 'TASK_FAIL' | 'QUEUE_BACKLOG' | 'CONSUMER_OFFLINE' | 'EXECUTION_TIME';
  clusterId?: number;
  conditionJson: string;
  notificationChannels: string;
  notificationUsers?: string;
  status: 'ENABLED' | 'DISABLED';
  createdAt: string;
  updatedAt: string;
}

// 告警记录
export interface AlertRecord {
  id: number;
  ruleId: number;
  ruleName: string;
  level: 'INFO' | 'WARNING' | 'ERROR' | 'CRITICAL';
  title: string;
  content: string;
  status: 'PENDING' | 'SENT' | 'FAILED';
  createdAt: string;
}

// WebSocket 消息
export interface WebSocketMessage {
  type: 'system_metrics' | 'alert' | 'task_status';
  data: any;
  timestamp: number;
}
