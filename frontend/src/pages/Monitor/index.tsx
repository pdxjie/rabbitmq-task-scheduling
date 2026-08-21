import React, { useEffect, useState } from 'react';
import { Row, Col, Card, Statistic, Tag, Progress, Spin } from 'antd';
import {
  CheckCircleOutlined,
  ClockCircleOutlined,
  RocketOutlined,
  ThunderboltOutlined,
  DatabaseOutlined,
  CloudServerOutlined,
} from '@ant-design/icons';
import { getSystemMetrics, getAllClusterMetrics } from '@/services/monitor';
import { useWebSocket } from '@/hooks/useWebSocket';
import type { SystemMetrics, ClusterMetrics, WebSocketMessage } from '@/types';
import dayjs from 'dayjs';

const Monitor: React.FC = () => {
  const [systemMetrics, setSystemMetrics] = useState<SystemMetrics | null>(null);
  const [clusterMetrics, setClusterMetrics] = useState<ClusterMetrics[]>([]);
  const [loading, setLoading] = useState(true);
  const [lastUpdateTime, setLastUpdateTime] = useState<string>('');

  useEffect(() => {
    loadMetrics();
  }, []);

  const loadMetrics = async () => {
    try {
      setLoading(true);
      const [systemRes, clusterRes] = await Promise.all([
        getSystemMetrics(),
        getAllClusterMetrics(),
      ]);

      if (systemRes.data) {
        setSystemMetrics(systemRes.data);
        setLastUpdateTime(dayjs().format('YYYY-MM-DD HH:mm:ss'));
      }

      if (clusterRes.data) {
        setClusterMetrics(clusterRes.data);
      }
    } catch (error) {
      console.error('获取监控数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // WebSocket 实时更新
  useWebSocket(`monitor_${Date.now()}`, {
    onMessage: (message: WebSocketMessage) => {
      if (message.type === 'system_metrics') {
        setSystemMetrics(message.data as SystemMetrics);
        setLastUpdateTime(dayjs().format('YYYY-MM-DD HH:mm:ss'));
      }
    },
  });

  const formatter = (val: number) => val.toLocaleString();

  if (loading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* 标题 */}
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-text-primary">实时监控</h1>
        <div className="text-text-secondary">
          最后更新: <span className="text-brand-primary">{lastUpdateTime || '刚刚'}</span>
        </div>
      </div>

      {/* 系统监控指标卡片 */}
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card className="bg-bg-secondary border border-border-primary">
            <Statistic
              title={<span className="text-text-secondary">今日消息总量</span>}
              value={systemMetrics?.todayExecutions || 0}
              formatter={formatter}
              prefix={<RocketOutlined className="text-brand-primary" />}
              valueStyle={{ color: '#ffffff' }}
            />
          </Card>
        </Col>

        <Col xs={24} sm={12} lg={6}>
          <Card className="bg-bg-secondary border border-border-primary">
            <Statistic
              title={<span className="text-text-secondary">平均处理时长</span>}
              value={systemMetrics?.avgExecutionTime || 0}
              precision={2}
              suffix="ms"
              prefix={<ClockCircleOutlined className="text-brand-primary" />}
              valueStyle={{ color: '#ffffff' }}
            />
          </Card>
        </Col>

        <Col xs={24} sm={12} lg={6}>
          <Card className="bg-bg-secondary border border-border-primary">
            <Statistic
              title={<span className="text-text-secondary">系统可用性</span>}
              value={systemMetrics?.successRate || 0}
              precision={2}
              suffix="%"
              prefix={<CheckCircleOutlined className="text-brand-primary" />}
              valueStyle={{ color: '#10b981' }}
            />
          </Card>
        </Col>

        <Col xs={24} sm={12} lg={6}>
          <Card className="bg-bg-secondary border border-border-primary">
            <Statistic
              title={<span className="text-text-secondary">活跃集群</span>}
              value={systemMetrics?.activeClusters || 0}
              suffix={`/ ${systemMetrics?.totalClusters || 0}`}
              prefix={<CloudServerOutlined className="text-brand-primary" />}
              valueStyle={{ color: '#ffffff' }}
            />
          </Card>
        </Col>
      </Row>

      {/* JVM 监控 */}
      <Card
        title={<span className="text-text-primary">JVM 监控</span>}
        className="bg-bg-secondary border border-border-primary"
      >
        <Row gutter={[16, 16]}>
          <Col xs={24} md={8}>
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-text-secondary">堆内存使用</span>
                <span className="text-text-primary">
                  {systemMetrics?.jvmHeapUsed || 0}MB / {systemMetrics?.jvmHeapMax || 0}MB
                </span>
              </div>
              <Progress
                percent={
                  systemMetrics?.jvmHeapMax
                    ? Number(((systemMetrics.jvmHeapUsed / systemMetrics.jvmHeapMax) * 100).toFixed(2))
                    : 0
                }
                strokeColor="#3b82f6"
                trailColor="#262626"
              />
            </div>
          </Col>

          <Col xs={24} md={8}>
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-text-secondary">CPU 使用率</span>
                <span className="text-text-primary">{systemMetrics?.cpuUsage?.toFixed(2) || 0}%</span>
              </div>
              <Progress
                percent={systemMetrics?.cpuUsage || 0}
                strokeColor="#10b981"
                trailColor="#262626"
              />
            </div>
          </Col>

          <Col xs={24} md={8}>
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-text-secondary">线程数</span>
                <span className="text-text-primary">{systemMetrics?.threadCount || 0}</span>
              </div>
              <Progress
                percent={systemMetrics?.threadCount ? Math.min((systemMetrics.threadCount / 200) * 100, 100) : 0}
                strokeColor="#f59e0b"
                trailColor="#262626"
              />
            </div>
          </Col>
        </Row>
      </Card>

      {/* 集群监控 */}
      <Card
        title={<span className="text-text-primary">集群监控</span>}
        className="bg-bg-secondary border border-border-primary"
      >
        <Row gutter={[16, 16]}>
          {clusterMetrics.map((cluster) => (
            <Col xs={24} md={12} lg={8} key={cluster.clusterId}>
              <Card
                className="bg-bg-tertiary border border-border-primary"
                bodyStyle={{ padding: 16 }}
              >
                <div className="space-y-3">
                  {/* 集群名称和状态 */}
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <DatabaseOutlined className="text-brand-primary text-xl" />
                      <span className="text-text-primary font-medium">{cluster.clusterName}</span>
                    </div>
                    <Tag color={cluster.status === 'HEALTHY' ? 'success' : 'error'}>
                      {cluster.status === 'HEALTHY' ? '健康' : '异常'}
                    </Tag>
                  </div>

                  {/* 统计信息 */}
                  <div className="grid grid-cols-3 gap-2 text-center">
                    <div className="space-y-1">
                      <div className="text-2xl font-bold text-text-primary">{cluster.queueCount}</div>
                      <div className="text-xs text-text-secondary">队列数</div>
                    </div>
                    <div className="space-y-1">
                      <div className="text-2xl font-bold text-text-primary">{cluster.connectionCount}</div>
                      <div className="text-xs text-text-secondary">连接数</div>
                    </div>
                    <div className="space-y-1">
                      <div className="text-2xl font-bold text-text-primary">{cluster.consumerCount}</div>
                      <div className="text-xs text-text-secondary">消费者</div>
                    </div>
                  </div>

                  {/* 消息统计 */}
                  <div className="space-y-2">
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-text-secondary">消息总数</span>
                      <span className="text-text-primary font-medium">
                        {cluster.messageCount?.toLocaleString() || 0}
                      </span>
                    </div>
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-text-secondary">就绪消息</span>
                      <span className="text-green-500 font-medium">
                        {cluster.messagesReady?.toLocaleString() || 0}
                      </span>
                    </div>
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-text-secondary">未确认消息</span>
                      <span className="text-orange-500 font-medium">
                        {cluster.messagesUnacknowledged?.toLocaleString() || 0}
                      </span>
                    </div>
                  </div>

                  {/* 速率信息 */}
                  <div className="space-y-1 pt-2 border-t border-border-primary">
                    <div className="flex items-center justify-between text-xs">
                      <span className="text-text-secondary">发布速率</span>
                      <span className="text-text-primary">{cluster.publishRate?.toFixed(2) || 0} msg/s</span>
                    </div>
                    <div className="flex items-center justify-between text-xs">
                      <span className="text-text-secondary">消费速率</span>
                      <span className="text-text-primary">{cluster.deliverRate?.toFixed(2) || 0} msg/s</span>
                    </div>
                  </div>
                </div>
              </Card>
            </Col>
          ))}

          {clusterMetrics.length === 0 && (
            <Col span={24}>
              <div className="text-center py-8 text-text-secondary">暂无集群数据</div>
            </Col>
          )}
        </Row>
      </Card>

      {/* 实时吞吐量图表 */}
      <Card
        title={<span className="text-text-primary">实时吞吐量（消息/秒）</span>}
        className="bg-bg-secondary border border-border-primary"
      >
        <div className="h-64 flex items-center justify-center text-text-secondary">
          ECharts 图表区域 - 待集成
        </div>
      </Card>

      {/* 热点队列 Top 5 */}
      <Card
        title={<span className="text-text-primary">🔥 热点队列 Top 5</span>}
        className="bg-bg-secondary border border-border-primary"
      >
        <div className="space-y-3">
          <div className="text-text-secondary text-center py-4">暂无数据</div>
        </div>
      </Card>
    </div>
  );
};

export default Monitor;
