import React, { useEffect, useState } from 'react';
import { Row, Col, Card, Spin } from 'antd';
import {
  CheckCircleOutlined,
  ClockCircleOutlined,
  RocketOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons';
import MetricCard from '@/components/common/MetricCard';
import { getSystemMetrics } from '@/services/monitor';
import { useMonitorStore } from '@/stores/monitor';
import { useWebSocket } from '@/hooks/useWebSocket';
import type { SystemMetrics, WebSocketMessage } from '@/types';

const Dashboard: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const { systemMetrics, setSystemMetrics } = useMonitorStore();

  // 加载初始数据
  useEffect(() => {
    loadSystemMetrics();
  }, []);

  const loadSystemMetrics = async () => {
    try {
      setLoading(true);
      const response = await getSystemMetrics();
      if (response.data) {
        setSystemMetrics(response.data);
      }
    } catch (error) {
      console.error('获取系统监控数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // WebSocket 实时更新
  useWebSocket(`client_${Date.now()}`, {
    onMessage: (message: WebSocketMessage) => {
      if (message.type === 'system_metrics') {
        setSystemMetrics(message.data as SystemMetrics);
      }
    },
  });

  if (loading) {
    return (
      <div className="flex items-center justify-center h-96">
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* 标题 */}
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-text-primary">概览</h1>
        <p className="text-text-secondary">最后更新: 刚刚</p>
      </div>

      {/* 统计卡片 */}
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <MetricCard
            title="任务总数"
            value={systemMetrics?.totalTasks || 0}
            icon={<CheckCircleOutlined />}
            change={12.5}
            trend="up"
          />
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <MetricCard
            title="今日执行"
            value={systemMetrics?.todayExecutions || 0}
            icon={<RocketOutlined />}
            change={8.2}
            trend="up"
          />
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <MetricCard
            title="成功率"
            value={systemMetrics?.successRate?.toFixed(1) || '0'}
            unit="%"
            icon={<ThunderboltOutlined />}
            change={0.2}
            trend="up"
          />
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <MetricCard
            title="运行中任务"
            value={systemMetrics?.runningTasks || 0}
            icon={<ClockCircleOutlined />}
          />
        </Col>
      </Row>

      {/* 实时吞吐量图表 */}
      <Card
        title="实时吞吐量"
        className="bg-bg-secondary border border-border-primary"
      >
        <div className="h-64 flex items-center justify-center text-text-secondary">
          图表区域 - 待集成 ECharts
        </div>
      </Card>

      {/* 底部信息卡片 */}
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card
            title="🔥 热点任务 Top 5"
            className="bg-bg-secondary border border-border-primary"
          >
            <div className="space-y-2">
              <p className="text-text-secondary">暂无数据</p>
            </div>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card
            title="⚠️ 实时告警"
            className="bg-bg-secondary border border-border-primary"
          >
            <div className="space-y-2">
              <p className="text-text-secondary">暂无告警</p>
            </div>
          </Card>
        </Col>
      </Row>

      {/* 集群状态 */}
      <Card
        title="🌐 集群状态"
        className="bg-bg-secondary border border-border-primary"
      >
        <Row gutter={[16, 16]}>
          <Col span={8}>
            <div className="p-4 bg-bg-tertiary rounded border border-border-primary">
              <div className="flex items-center gap-2 mb-2">
                <span className="w-2 h-2 bg-green-500 rounded-full"></span>
                <span className="text-text-primary font-medium">生产主集群</span>
              </div>
              <p className="text-text-secondary text-sm">健康度: 92</p>
              <p className="text-text-secondary text-sm">消息: 1.2K/s</p>
            </div>
          </Col>
        </Row>
      </Card>
    </div>
  );
};

export default Dashboard;
