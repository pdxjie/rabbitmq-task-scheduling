import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Avatar, Descriptions, Button, Tag, Space, Statistic } from 'antd';
import {
  UserOutlined,
  MailOutlined,
  PhoneOutlined,
  TeamOutlined,
  CalendarOutlined,
  EditOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { getUserProfile } from '@/services/user';
import type { UserProfile } from '@/services/user';

const Profile: React.FC = () => {
  const navigate = useNavigate();
  const [userInfo, setUserInfo] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadUserProfile();
  }, []);

  const loadUserProfile = async () => {
    try {
      const response = await getUserProfile();
      if (response.data.code === 200) {
        setUserInfo(response.data.data);
      }
    } catch (error) {
      console.error('加载用户信息失败', error);
    } finally {
      setLoading(false);
    }
  };

  // 模拟统计数据
  const stats = {
    totalTasks: 156,
    runningTasks: 12,
    successRate: 95.8,
    totalClusters: 3,
  };

  if (loading || !userInfo) {
    return <div>加载中...</div>;
  }

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-text-primary">个人中心</h1>
        <p className="text-text-secondary mt-2">查看个人信息和工作统计</p>
      </div>

      <Row gutter={[16, 16]}>
        {/* 个人信息卡片 */}
        <Col xs={24} lg={8}>
          <Card>
            <div className="text-center">
              <Avatar size={100} icon={<UserOutlined />} className="bg-brand-primary mb-4" />
              <h2 className="text-xl font-bold text-text-primary mb-2">{userInfo.nickname || userInfo.username}</h2>
              <Tag color="blue">{userInfo.role}</Tag>
              <div className="mt-6">
                <Button
                  type="primary"
                  icon={<EditOutlined />}
                  onClick={() => navigate('/settings')}
                  block
                >
                  编辑资料
                </Button>
              </div>
            </div>

            <div className="mt-6">
              <Space direction="vertical" className="w-full" size="middle">
                <div className="flex items-center">
                  <MailOutlined className="text-text-tertiary mr-2" />
                  <span className="text-text-secondary">{userInfo.email}</span>
                </div>
                <div className="flex items-center">
                  <PhoneOutlined className="text-text-tertiary mr-2" />
                  <span className="text-text-secondary">{userInfo.phone}</span>
                </div>
                <div className="flex items-center">
                  <TeamOutlined className="text-text-tertiary mr-2" />
                  <span className="text-text-secondary">{userInfo.department}</span>
                </div>
                <div className="flex items-center">
                  <CalendarOutlined className="text-text-tertiary mr-2" />
                  <span className="text-text-secondary">加入于 {userInfo.createdAt?.split(' ')[0]}</span>
                </div>
              </Space>
            </div>
          </Card>
        </Col>

        {/* 详细信息和统计 */}
        <Col xs={24} lg={16}>
          <Row gutter={[16, 16]}>
            {/* 工作统计 */}
            <Col span={24}>
              <Card title="工作统计">
                <Row gutter={16}>
                  <Col xs={12} sm={6}>
                    <Statistic title="创建任务数" value={stats.totalTasks} />
                  </Col>
                  <Col xs={12} sm={6}>
                    <Statistic title="运行中任务" value={stats.runningTasks} />
                  </Col>
                  <Col xs={12} sm={6}>
                    <Statistic
                      title="成功率"
                      value={stats.successRate}
                      precision={1}
                      suffix="%"
                      valueStyle={{ color: '#52c41a' }}
                    />
                  </Col>
                  <Col xs={12} sm={6}>
                    <Statistic title="管理集群数" value={stats.totalClusters} />
                  </Col>
                </Row>
              </Card>
            </Col>

            {/* 账号信息 */}
            <Col span={24}>
              <Card title="账号信息">
                <Descriptions column={1}>
                  <Descriptions.Item label="用户名">{userInfo.username}</Descriptions.Item>
                  <Descriptions.Item label="昵称">{userInfo.nickname}</Descriptions.Item>
                  <Descriptions.Item label="角色">{userInfo.role}</Descriptions.Item>
                  <Descriptions.Item label="所属部门">{userInfo.department}</Descriptions.Item>
                  <Descriptions.Item label="邮箱地址">{userInfo.email}</Descriptions.Item>
                  <Descriptions.Item label="手机号码">{userInfo.phone || '未设置'}</Descriptions.Item>
                  <Descriptions.Item label="注册时间">{userInfo.createdAt}</Descriptions.Item>
                  <Descriptions.Item label="最后登录">{userInfo.lastLoginTime || '首次登录'}</Descriptions.Item>
                </Descriptions>
              </Card>
            </Col>

            {/* 最近活动 */}
            <Col span={24}>
              <Card title="最近活动">
                <Space direction="vertical" className="w-full">
                  <div className="flex justify-between items-center py-2 border-b border-border-primary">
                    <div>
                      <div className="text-text-primary">创建了任务 "数据同步任务"</div>
                      <div className="text-sm text-text-tertiary">2小时前</div>
                    </div>
                  </div>
                  <div className="flex justify-between items-center py-2 border-b border-border-primary">
                    <div>
                      <div className="text-text-primary">修改了集群配置 "生产环境集群"</div>
                      <div className="text-sm text-text-tertiary">5小时前</div>
                    </div>
                  </div>
                  <div className="flex justify-between items-center py-2 border-b border-border-primary">
                    <div>
                      <div className="text-text-primary">创建了工作流 "数据处理流程"</div>
                      <div className="text-sm text-text-tertiary">昨天 18:30</div>
                    </div>
                  </div>
                  <div className="flex justify-between items-center py-2">
                    <div>
                      <div className="text-text-primary">更新了告警规则</div>
                      <div className="text-sm text-text-tertiary">昨天 15:20</div>
                    </div>
                  </div>
                </Space>
              </Card>
            </Col>
          </Row>
        </Col>
      </Row>
    </div>
  );
};

export default Profile;
