import React, { useState } from 'react';
import { Form, Input, Button, Card, message } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { login } from '@/services/user';

const Login: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (values: any) => {
    setLoading(true);
    try {
      const response = await login({
        username: values.username,
        password: values.password,
      });

      console.log('登录响应:', response);

      if (response.data.code === 200) {
        const userProfile = response.data.data;
        // 保存登录信息
        localStorage.setItem('token', 'mock-token-' + Date.now());
        localStorage.setItem('username', userProfile.username);

        message.success('登录成功！');
        navigate('/');
      } else {
        message.error(response.data.message || '登录失败');
      }
    } catch (error) {
      console.error('登录错误:', error);
      message.error('登录失败，请检查用户名和密码');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-bg-primary">
      <Card className="w-96" style={{ backgroundColor: '#161616' }}>
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-brand-primary mb-2">TaskFlow</h1>
          <p className="text-text-secondary">现代化智能任务调度平台</p>
        </div>

        <Form
          name="login"
          initialValues={{ username: 'admin', password: 'admin123' }}
          onFinish={handleLogin}
          size="large"
        >
          <Form.Item
            name="username"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="用户名"
              style={{ backgroundColor: '#262626' }}
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="密码"
              style={{ backgroundColor: '#262626' }}
            />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" className="w-full" loading={loading}>
              登录
            </Button>
          </Form.Item>
        </Form>

        <div className="text-center text-sm text-text-tertiary mt-4">
          <p>默认账号: admin / admin123</p>
        </div>
      </Card>
    </div>
  );
};

export default Login;
