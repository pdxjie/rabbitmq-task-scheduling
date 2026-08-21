import React from 'react';
import { Layout } from 'antd';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import Header from './Header';

const { Content } = Layout;

const DashboardLayout: React.FC = () => {
  return (
    <Layout className="min-h-screen">
      <Sidebar />
      <Layout style={{ marginLeft: 240 }}>
        <Header />
        <Content className="m-6 p-6 bg-bg-secondary rounded-lg">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default DashboardLayout;
