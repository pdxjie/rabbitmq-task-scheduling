import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider, theme } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import DashboardLayout from '@/components/layout/DashboardLayout';
import Dashboard from '@/pages/Dashboard';
import ClusterManagement from '@/pages/Cluster';
import TaskManagement from '@/pages/Task';
import WorkflowManagement from '@/pages/Workflow';
import Monitor from '@/pages/Monitor';
import AlertManagement from '@/pages/Alert';
import Settings from '@/pages/Settings';
import Profile from '@/pages/Profile';
import Login from '@/pages/Login';

const App: React.FC = () => {
  return (
    <ConfigProvider
      locale={zhCN}
      theme={{
        algorithm: theme.darkAlgorithm,
        token: {
          colorPrimary: '#3b82f6',
          colorBgContainer: '#161616',
          colorBgLayout: '#0a0a0a',
          colorBorder: '#2a2a2a',
          colorText: '#ffffff',
          colorTextSecondary: '#a1a1a1',
          borderRadius: 8,
        },
      }}
    >
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/" element={<DashboardLayout />}>
            <Route index element={<Dashboard />} />
            <Route path="cluster" element={<ClusterManagement />} />
            <Route path="task" element={<TaskManagement />} />
            <Route path="workflow" element={<WorkflowManagement />} />
            <Route path="monitor" element={<Monitor />} />
            <Route path="alert" element={<AlertManagement />} />
            <Route path="profile" element={<Profile />} />
            <Route path="settings" element={<Settings />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </ConfigProvider>
  );
};

export default App;
