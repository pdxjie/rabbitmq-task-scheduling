import React, { useState, useEffect } from 'react';
import { Card, Tabs, Form, Input, Button, Switch, Select, message, Space, Divider } from 'antd';
import { SaveOutlined, UserOutlined, BellOutlined, LockOutlined, GlobalOutlined } from '@ant-design/icons';
import { getUserProfile, updateProfile, updatePassword } from '@/services/user';

const { Option } = Select;
const { TextArea } = Input;

const Settings: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [profileForm] = Form.useForm();
  const [notificationForm] = Form.useForm();
  const [securityForm] = Form.useForm();

  useEffect(() => {
    loadUserProfile();
  }, []);

  const loadUserProfile = async () => {
    try {
      const response = await getUserProfile();
      if (response.data.code === 200) {
        const profile = response.data.data;
        profileForm.setFieldsValue({
          username: profile.username,
          nickname: profile.nickname,
          email: profile.email,
          phone: profile.phone,
          department: profile.department,
        });
      }
    } catch (error) {
      console.error('加载用户信息失败', error);
    }
  };

  const handleSaveProfile = async (values: any) => {
    setLoading(true);
    try {
      const response = await updateProfile({
        nickname: values.nickname,
        email: values.email,
        phone: values.phone,
      });
      if (response.data.code === 200) {
        message.success('个人信息已保存');
      } else {
        message.error(response.data.message || '保存失败');
      }
    } catch (error) {
      message.error('保存失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSaveNotification = async (values: any) => {
    setLoading(true);
    try {
      // 通知设置暂时保存到 localStorage
      localStorage.setItem('notificationSettings', JSON.stringify(values));
      message.success('通知设置已保存');
    } catch (error) {
      message.error('保存失败');
    } finally {
      setLoading(false);
    }
  };

  const handleChangePassword = async (values: any) => {
    if (values.newPassword !== values.confirmPassword) {
      message.error('两次输入的密码不一致');
      return;
    }
    setLoading(true);
    try {
      const response = await updatePassword({
        oldPassword: values.currentPassword,
        newPassword: values.newPassword,
      });
      if (response.data.code === 200) {
        message.success('密码修改成功');
        securityForm.resetFields();
      } else {
        message.error(response.data.message || '密码修改失败');
      }
    } catch (error) {
      message.error('密码修改失败');
    } finally {
      setLoading(false);
    }
  };

  const tabItems = [
    {
      key: 'profile',
      label: (
        <span>
          <UserOutlined /> 个人信息
        </span>
      ),
      children: (
        <Card>
          <Form
            form={profileForm}
            layout="vertical"
            onFinish={handleSaveProfile}
          >
            <Form.Item label="用户名" name="username">
              <Input placeholder="请输入用户名" disabled />
            </Form.Item>
            <Form.Item label="昵称" name="nickname" rules={[{ required: true }]}>
              <Input placeholder="请输入昵称" />
            </Form.Item>
            <Form.Item label="邮箱" name="email" rules={[{ required: true, type: 'email' }]}>
              <Input placeholder="请输入邮箱" />
            </Form.Item>
            <Form.Item label="手机号" name="phone">
              <Input placeholder="请输入手机号" />
            </Form.Item>
            <Form.Item label="部门" name="department">
              <Input placeholder="请输入部门" disabled />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" icon={<SaveOutlined />} loading={loading}>
                保存
              </Button>
            </Form.Item>
          </Form>
        </Card>
      ),
    },
    {
      key: 'notification',
      label: (
        <span>
          <BellOutlined /> 通知设置
        </span>
      ),
      children: (
        <Card>
          <Form
            form={notificationForm}
            layout="vertical"
            onFinish={handleSaveNotification}
            initialValues={{
              taskFail: true,
              taskSuccess: false,
              clusterAlert: true,
              systemUpdate: true,
              emailNotification: true,
              webhookNotification: false,
              notificationHour: '09:00-18:00',
            }}
          >
            <Divider orientation="left">任务通知</Divider>
            <Form.Item label="任务失败通知" name="taskFail" valuePropName="checked">
              <Switch />
            </Form.Item>
            <Form.Item label="任务成功通知" name="taskSuccess" valuePropName="checked">
              <Switch />
            </Form.Item>

            <Divider orientation="left">系统通知</Divider>
            <Form.Item label="集群告警通知" name="clusterAlert" valuePropName="checked">
              <Switch />
            </Form.Item>
            <Form.Item label="系统更新通知" name="systemUpdate" valuePropName="checked">
              <Switch />
            </Form.Item>

            <Divider orientation="left">通知方式</Divider>
            <Form.Item label="邮件通知" name="emailNotification" valuePropName="checked">
              <Switch />
            </Form.Item>
            <Form.Item label="Webhook 通知" name="webhookNotification" valuePropName="checked">
              <Switch />
            </Form.Item>

            <Form.Item label="通知时间段" name="notificationHour">
              <Select>
                <Option value="00:00-23:59">全天</Option>
                <Option value="09:00-18:00">工作时间 (9:00-18:00)</Option>
                <Option value="09:00-21:00">9:00-21:00</Option>
              </Select>
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit" icon={<SaveOutlined />} loading={loading}>
                保存
              </Button>
            </Form.Item>
          </Form>
        </Card>
      ),
    },
    {
      key: 'security',
      label: (
        <span>
          <LockOutlined /> 安全设置
        </span>
      ),
      children: (
        <Card>
          <Form form={securityForm} layout="vertical" onFinish={handleChangePassword}>
            <Form.Item
              label="当前密码"
              name="currentPassword"
              rules={[{ required: true, message: '请输入当前密码' }]}
            >
              <Input.Password placeholder="请输入当前密码" />
            </Form.Item>
            <Form.Item
              label="新密码"
              name="newPassword"
              rules={[
                { required: true, message: '请输入新密码' },
                { min: 6, message: '密码至少6位' },
              ]}
            >
              <Input.Password placeholder="请输入新密码（至少6位）" />
            </Form.Item>
            <Form.Item
              label="确认新密码"
              name="confirmPassword"
              rules={[{ required: true, message: '请确认新密码' }]}
            >
              <Input.Password placeholder="请再次输入新密码" />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" icon={<SaveOutlined />} loading={loading}>
                修改密码
              </Button>
            </Form.Item>
          </Form>

          <Divider />

          <div className="mt-6">
            <h3 className="text-base font-medium mb-4">登录设备</h3>
            <Space direction="vertical" className="w-full">
              <Card size="small">
                <div className="flex justify-between items-center">
                  <div>
                    <div className="font-medium">Chrome 浏览器 - MacOS</div>
                    <div className="text-sm text-text-tertiary">上次登录: 2026-08-21 15:00</div>
                    <div className="text-sm text-text-tertiary">IP: 192.168.1.100</div>
                  </div>
                  <span className="text-green-500">当前设备</span>
                </div>
              </Card>
            </Space>
          </div>
        </Card>
      ),
    },
    {
      key: 'system',
      label: (
        <span>
          <GlobalOutlined /> 系统设置
        </span>
      ),
      children: (
        <Card>
          <Form layout="vertical" initialValues={{ theme: 'dark', language: 'zh-CN', timezone: 'Asia/Shanghai' }}>
            <Form.Item label="主题" name="theme">
              <Select>
                <Option value="dark">暗色主题</Option>
                <Option value="light">亮色主题</Option>
                <Option value="auto">跟随系统</Option>
              </Select>
            </Form.Item>
            <Form.Item label="语言" name="language">
              <Select>
                <Option value="zh-CN">简体中文</Option>
                <Option value="en-US">English</Option>
              </Select>
            </Form.Item>
            <Form.Item label="时区" name="timezone">
              <Select>
                <Option value="Asia/Shanghai">中国标准时间 (GMT+8)</Option>
                <Option value="America/New_York">美国东部时间 (GMT-5)</Option>
                <Option value="Europe/London">英国时间 (GMT+0)</Option>
              </Select>
            </Form.Item>
            <Form.Item>
              <Button
                type="primary"
                icon={<SaveOutlined />}
                onClick={() => message.success('系统设置已保存')}
              >
                保存
              </Button>
            </Form.Item>
          </Form>
        </Card>
      ),
    },
  ];

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-text-primary">系统设置</h1>
        <p className="text-text-secondary mt-2">管理个人信息、通知和系统偏好设置</p>
      </div>

      <Tabs items={tabItems} />
    </div>
  );
};

export default Settings;
