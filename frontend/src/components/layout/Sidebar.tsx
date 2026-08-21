import React from 'react';
import { Layout, Menu } from 'antd';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  DashboardOutlined,
  ClusterOutlined,
  ScheduleOutlined,
  ApartmentOutlined,
  MonitorOutlined,
  BellOutlined,
  SettingOutlined,
} from '@ant-design/icons';

const { Sider } = Layout;

const Sidebar: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const menuItems = [
    {
      key: '/',
      icon: <DashboardOutlined />,
      label: '概览',
    },
    {
      key: '/cluster',
      icon: <ClusterOutlined />,
      label: '集群管理',
    },
    {
      key: '/task',
      icon: <ScheduleOutlined />,
      label: '任务管理',
    },
    {
      key: '/workflow',
      icon: <ApartmentOutlined />,
      label: '工作流',
    },
    {
      key: '/monitor',
      icon: <MonitorOutlined />,
      label: '监控',
    },
    {
      key: '/alert',
      icon: <BellOutlined />,
      label: '告警',
    },
    {
      key: '/settings',
      icon: <SettingOutlined />,
      label: '设置',
    },
  ];

  const handleMenuClick = ({ key }: { key: string }) => {
    navigate(key);
  };

  return (
    <Sider
      width={240}
      className="bg-bg-secondary"
      style={{
        overflow: 'auto',
        height: '100vh',
        position: 'fixed',
        left: 0,
        top: 0,
        bottom: 0,
      }}
    >
      <div className="h-16 flex items-center justify-center border-b border-border-primary">
        <h1 className="text-2xl font-bold text-brand-primary">TaskFlow</h1>
      </div>
      <Menu
        mode="inline"
        selectedKeys={[location.pathname]}
        items={menuItems}
        onClick={handleMenuClick}
        className="bg-bg-secondary border-r-0"
        style={{ marginTop: 16 }}
      />
    </Sider>
  );
};

export default Sidebar;
