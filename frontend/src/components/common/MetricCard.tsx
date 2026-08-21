import React from 'react';
import { Card, Statistic } from 'antd';
import { ArrowUpOutlined, ArrowDownOutlined } from '@ant-design/icons';

interface MetricCardProps {
  title: string;
  value: number | string;
  change?: number;
  trend?: 'up' | 'down';
  icon?: React.ReactNode;
  unit?: string;
  loading?: boolean;
}

const MetricCard: React.FC<MetricCardProps> = ({
  title,
  value,
  change,
  trend,
  icon,
  unit,
  loading = false,
}) => {
  // 数字格式化函数（不使用CountUp，避免依赖问题）
  const formatter = (val: number) => val.toLocaleString();

  return (
    <Card
      loading={loading}
      className="bg-bg-secondary border border-border-primary hover:border-brand-primary transition-all duration-200"
      styles={{ body: { padding: '24px' } }}
    >
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <p className="text-text-secondary text-sm mb-2">{title}</p>
          <Statistic
            value={typeof value === 'number' ? value : 0}
            formatter={typeof value === 'number' ? formatter : undefined}
            suffix={unit}
            styles={{ value: { color: '#ffffff', fontSize: 28, fontWeight: 600 } }}
          />
          {change !== undefined && (
            <div className={`flex items-center mt-2 text-sm ${trend === 'up' ? 'text-green-500' : 'text-red-500'}`}>
              {trend === 'up' ? <ArrowUpOutlined /> : <ArrowDownOutlined />}
              <span className="ml-1">{Math.abs(change)}%</span>
            </div>
          )}
        </div>
        {icon && (
          <div className="text-4xl opacity-20">
            {icon}
          </div>
        )}
      </div>
    </Card>
  );
};

export default MetricCard;
