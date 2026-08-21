import React, { useEffect, useState } from 'react';
import { Card, Button, Table, Tag, Space, Modal, Form, Input, Select, message, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { getAllClusters, createCluster, updateCluster, deleteCluster, testClusterConnection } from '@/services/cluster';
import type { Cluster } from '@/types';

const { Option } = Select;

const ClusterManagement: React.FC = () => {
  const [clusters, setClusters] = useState<Cluster[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingCluster, setEditingCluster] = useState<Cluster | null>(null);
  const [testingConnection, setTestingConnection] = useState(false);
  const [form] = Form.useForm();

  useEffect(() => {
    loadClusters();
  }, []);

  const loadClusters = async () => {
    try {
      setLoading(true);
      const response = await getAllClusters();
      if (response.data) {
        setClusters(response.data);
      }
    } catch (error) {
      console.error('获取集群列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingCluster(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (cluster: Cluster) => {
    setEditingCluster(cluster);
    form.setFieldsValue(cluster);
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteCluster(id);
      message.success('删除成功');
      loadClusters();
    } catch (error) {
      message.error('删除失败');
    }
  };

  const handleTestConnection = async () => {
    try {
      const values = await form.validateFields();
      setTestingConnection(true);
      const response = await testClusterConnection(values);
      if (response.data?.success) {
        message.success('连接测试成功！');
      } else {
        message.error(response.data?.message || '连接测试失败');
      }
    } catch (error: any) {
      if (error.errorFields) {
        message.warning('请先填写完整信息');
      } else {
        message.error('连接测试失败');
      }
    } finally {
      setTestingConnection(false);
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingCluster) {
        await updateCluster(editingCluster.id, values);
        message.success('更新成功');
      } else {
        await createCluster(values);
        message.success('创建成功');
      }
      setModalVisible(false);
      loadClusters();
    } catch (error: any) {
      if (!error.errorFields) {
        message.error(editingCluster ? '更新失败' : '创建失败');
      }
    }
  };

  const columns: ColumnsType<Cluster> = [
    {
      title: '集群名称',
      dataIndex: 'clusterName',
      key: 'clusterName',
      width: 200,
    },
    {
      title: '连接方式',
      dataIndex: 'connectionType',
      key: 'connectionType',
      width: 120,
      render: (type: string) => {
        const typeMap: Record<string, { text: string; color: string }> = {
          DIRECT: { text: '直接连接', color: 'blue' },
          SSH_TUNNEL: { text: 'SSH隧道', color: 'purple' },
          TLS: { text: 'TLS加密', color: 'green' },
        };
        const config = typeMap[type] || { text: type, color: 'default' };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '主机地址',
      dataIndex: 'host',
      key: 'host',
      width: 150,
    },
    {
      title: '端口',
      dataIndex: 'port',
      key: 'port',
      width: 80,
    },
    {
      title: 'Virtual Host',
      dataIndex: 'vhost',
      key: 'vhost',
      width: 100,
    },
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
      width: 100,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const statusConfig: Record<string, { icon: React.ReactNode; color: string; text: string }> = {
          CONNECTED: { icon: <CheckCircleOutlined />, color: 'success', text: '已连接' },
          DISCONNECTED: { icon: <CloseCircleOutlined />, color: 'default', text: '未连接' },
          ERROR: { icon: <CloseCircleOutlined />, color: 'error', text: '错误' },
        };
        const config = statusConfig[status] || statusConfig.DISCONNECTED;
        return (
          <Tag icon={config.icon} color={config.color}>
            {config.text}
          </Tag>
        );
      },
    },
    {
      title: '健康度',
      dataIndex: 'healthScore',
      key: 'healthScore',
      width: 100,
      render: (score?: number) => {
        if (!score) return '-';
        const color = score >= 80 ? '#10b981' : score >= 60 ? '#f59e0b' : '#ef4444';
        return <span style={{ color }}>{score}分</span>;
      },
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这个集群吗？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      {/* 标题 */}
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-text-primary">集群管理</h1>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
          连接新集群
        </Button>
      </div>

      {/* 集群列表 */}
      <Card className="bg-bg-secondary border border-border-primary">
        <Table
          columns={columns}
          dataSource={clusters}
          rowKey="id"
          loading={loading}
          scroll={{ x: 1200 }}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
        />
      </Card>

      {/* 创建/编辑集群弹窗 */}
      <Modal
        title={editingCluster ? '编辑集群' : '连接新集群'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        width={600}
        footer={[
          <Button key="cancel" onClick={() => setModalVisible(false)}>
            取消
          </Button>,
          <Button key="test" loading={testingConnection} onClick={handleTestConnection}>
            测试连接
          </Button>,
          <Button key="submit" type="primary" onClick={handleSubmit}>
            {editingCluster ? '更新' : '保存连接'}
          </Button>,
        ]}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 24 }}>
          <Form.Item
            label="集群名称"
            name="clusterName"
            rules={[{ required: true, message: '请输入集群名称' }]}
          >
            <Input placeholder="例如: 生产环境主集群" />
          </Form.Item>

          <Form.Item
            label="连接方式"
            name="connectionType"
            initialValue="DIRECT"
            rules={[{ required: true, message: '请选择连接方式' }]}
          >
            <Select>
              <Option value="DIRECT">直接连接</Option>
              <Option value="SSH_TUNNEL">SSH隧道</Option>
              <Option value="TLS">TLS加密</Option>
            </Select>
          </Form.Item>

          <Form.Item
            label="主机地址"
            name="host"
            rules={[{ required: true, message: '请输入主机地址' }]}
          >
            <Input placeholder="例如: localhost 或 rabbitmq.example.com" />
          </Form.Item>

          <div className="grid grid-cols-2 gap-4">
            <Form.Item
              label="端口"
              name="port"
              initialValue={5672}
              rules={[{ required: true, message: '请输入端口' }]}
            >
              <Input type="number" placeholder="5672" />
            </Form.Item>

            <Form.Item
              label="管理端口"
              name="managementPort"
              initialValue={15672}
              rules={[{ required: true, message: '请输入管理端口' }]}
            >
              <Input type="number" placeholder="15672" />
            </Form.Item>
          </div>

          <Form.Item
            label="Virtual Host"
            name="vhost"
            initialValue="/"
            rules={[{ required: true, message: '请输入 Virtual Host' }]}
          >
            <Input placeholder="/" />
          </Form.Item>

          <Form.Item
            label="用户名"
            name="username"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input placeholder="guest" />
          </Form.Item>

          <Form.Item
            label="密码"
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password placeholder="请输入密码" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default ClusterManagement;
