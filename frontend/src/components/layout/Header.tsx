import React, { useState } from 'react';
import { Layout, Input, Badge, Avatar, Dropdown, Modal, List, Empty, message } from 'antd';
import { SearchOutlined, BellOutlined, UserOutlined, SettingOutlined, LogoutOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import type { MenuProps } from 'antd';

const { Header: AntHeader } = Layout;

const Header: React.FC = () => {
  const navigate = useNavigate();
  const [notificationVisible, setNotificationVisible] = useState(false);

  // 模拟通知数据
  const notifications = [
    { id: 1, title: '任务执行失败', content: '任务 "数据同步" 执行失败', time: '5分钟前' },
    { id: 2, title: '集群连接异常', content: '集群 "生产环境" 连接异常', time: '1小时前' },
    { id: 3, title: '系统更新', content: 'TaskFlow 系统已更新至 v1.0.1', time: '2小时前' },
  ];

  const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
    switch (key) {
      case 'profile':
        navigate('/profile');
        break;
      case 'settings':
        navigate('/settings');
        break;
      case 'logout':
        Modal.confirm({
          title: '确认退出',
          content: '确定要退出登录吗？',
          okText: '确定',
          cancelText: '取消',
          onOk: () => {
            // 清除登录信息
            localStorage.removeItem('token');
            localStorage.removeItem('username');
            message.success('已退出登录');
            // 跳转到登录页
            navigate('/login');
          },
        });
        break;
    }
  };

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人中心',
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '设置',
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
    },
  ];

  return (
    <AntHeader
      className="bg-bg-secondary border-b border-border-primary flex items-center justify-between px-6"
      style={{ marginLeft: 240, height: 64 }}
    >
      <Input
        placeholder="搜索任务、集群..."
        prefix={<SearchOutlined />}
        className="w-96"
        style={{ backgroundColor: '#262626' }}
      />
      <div className="flex items-center gap-4">
        <Badge count={notifications.length} size="small">
          <BellOutlined
            className="text-xl cursor-pointer text-text-secondary hover:text-text-primary"
            onClick={() => setNotificationVisible(true)}
          />
        </Badge>
        <Dropdown menu={{ items: userMenuItems, onClick: handleMenuClick }} placement="bottomRight">
          <Avatar icon={<UserOutlined />} className="cursor-pointer bg-brand-primary" />
        </Dropdown>
      </div>

      {/* 通知弹窗 */}
      <Modal
        title="通知中心"
        open={notificationVisible}
        onCancel={() => setNotificationVisible(false)}
        footer={null}
        width={500}
      >
        {notifications.length > 0 ? (
          <List
            dataSource={notifications}
            renderItem={(item) => (
              <List.Item>
                <List.Item.Meta
                  title={item.title}
                  description={
                    <>
                      <div>{item.content}</div>
                      <div className="text-xs text-text-tertiary mt-1">{item.time}</div>
                    </>
                  }
                />
              </List.Item>
            )}
          />
        ) : (
          <Empty description="暂无通知" />
        )}
      </Modal>
    </AntHeader>
  );
};

export default Header;
