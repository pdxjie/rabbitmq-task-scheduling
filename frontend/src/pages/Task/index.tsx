import React, { useEffect, useState } from 'react';
import {
  Card,
  Button,
  Table,
  Tag,
  Space,
  Modal,
  Form,
  Input,
  Select,
  InputNumber,
  Switch,
  message,
  Popconfirm,
  Drawer,
  Tabs,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  ThunderboltOutlined,
  HistoryOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import {
  getTaskList,
  createTask,
  updateTask,
  deleteTask,
  enableTask,
  disableTask,
  executeTask,
  getTaskExecutionLogs,
} from '@/services/task';
import { getAllClusters } from '@/services/cluster';
import type { Task, TaskExecutionLog, Cluster } from '@/types';
import dayjs from 'dayjs';

const { Option } = Select;
const { TextArea } = Input;

const TaskManagement: React.FC = () => {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [clusters, setClusters] = useState<Cluster[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [logDrawerVisible, setLogDrawerVisible] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | null>(null);
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const [executionLogs, setExecutionLogs] = useState<TaskExecutionLog[]>([]);
  const [logsLoading, setLogsLoading] = useState(false);
  const [form] = Form.useForm();
  const [triggerType, setTriggerType] = useState<string>('CRON');

  useEffect(() => {
    loadTasks();
    loadClusters();
  }, []);

  const loadTasks = async () => {
    try {
      setLoading(true);
      const response = await getTaskList({ current: 1, size: 100 });
      if (response.data?.records) {
        setTasks(response.data.records);
      }
    } catch (error) {
      console.error('获取任务列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadClusters = async () => {
    try {
      const response = await getAllClusters();
      if (response.data) {
        setClusters(response.data);
      }
    } catch (error) {
      console.error('获取集群列表失败:', error);
    }
  };

  const loadExecutionLogs = async (taskId: number) => {
    try {
      setLogsLoading(true);
      const response = await getTaskExecutionLogs({ current: 1, size: 50, taskId });
      if (response.data?.records) {
        setExecutionLogs(response.data.records);
      }
    } catch (error) {
      console.error('获取执行日志失败:', error);
    } finally {
      setLogsLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingTask(null);
    form.resetFields();
    setTriggerType('CRON');
    setModalVisible(true);
  };

  const handleEdit = (task: Task) => {
    setEditingTask(task);
    setTriggerType(task.triggerType);
    form.setFieldsValue({
      ...task,
      taskContent: typeof task.taskContent === 'string' ? task.taskContent : JSON.stringify(task.taskContent, null, 2),
    });
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteTask(id);
      message.success('删除成功');
      loadTasks();
    } catch (error) {
      message.error('删除失败');
    }
  };

  const handleToggleStatus = async (task: Task) => {
    try {
      if (task.status === 'ENABLED') {
        await disableTask(task.id);
        message.success('已禁用');
      } else {
        await enableTask(task.id);
        message.success('已启用');
      }
      loadTasks();
    } catch (error) {
      message.error('操作失败');
    }
  };

  const handleExecute = async (id: number) => {
    try {
      await executeTask(id);
      message.success('任务已提交执行');
    } catch (error) {
      message.error('执行失败');
    }
  };

  const handleViewLogs = (task: Task) => {
    setSelectedTask(task);
    loadExecutionLogs(task.id);
    setLogDrawerVisible(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const taskData = {
        ...values,
        status: 'ENABLED',
      };

      if (editingTask) {
        await updateTask(editingTask.id, taskData);
        message.success('更新成功');
      } else {
        await createTask(taskData);
        message.success('创建成功');
      }
      setModalVisible(false);
      loadTasks();
    } catch (error: any) {
      if (!error.errorFields) {
        message.error(editingTask ? '更新失败' : '创建失败');
      }
    }
  };

  const columns: ColumnsType<Task> = [
    {
      title: '任务名称',
      dataIndex: 'taskName',
      key: 'taskName',
      width: 200,
      fixed: 'left',
    },
    {
      title: '任务类型',
      dataIndex: 'taskType',
      key: 'taskType',
      width: 100,
      render: (type: string) => {
        const typeMap: Record<string, { text: string; color: string }> = {
          MESSAGE: { text: '消息', color: 'blue' },
          HTTP: { text: 'HTTP', color: 'green' },
          SHELL: { text: 'Shell', color: 'orange' },
          CODE: { text: '代码', color: 'purple' },
        };
        const config = typeMap[type] || { text: type, color: 'default' };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '触发方式',
      dataIndex: 'triggerType',
      key: 'triggerType',
      width: 100,
      render: (type: string) => {
        const typeMap: Record<string, { text: string; color: string }> = {
          CRON: { text: 'Cron', color: 'cyan' },
          DELAY: { text: '延迟', color: 'orange' },
          IMMEDIATE: { text: '立即', color: 'red' },
        };
        const config = typeMap[type] || { text: type, color: 'default' };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: 'Cron表达式/延迟时间',
      key: 'schedule',
      width: 150,
      render: (_, record) => {
        if (record.triggerType === 'CRON') {
          return <code className="text-xs">{record.cronExpression}</code>;
        } else if (record.triggerType === 'DELAY') {
          return <span>{record.delaySeconds}秒</span>;
        }
        return '-';
      },
    },
    {
      title: '所属集群',
      dataIndex: 'clusterName',
      key: 'clusterName',
      width: 150,
    },
    {
      title: '队列名称',
      dataIndex: 'queueName',
      key: 'queueName',
      width: 150,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status: string) => (
        <Tag color={status === 'ENABLED' ? 'success' : 'default'}>
          {status === 'ENABLED' ? '启用' : '禁用'}
        </Tag>
      ),
    },
    {
      title: '下次执行时间',
      dataIndex: 'nextExecutionTime',
      key: 'nextExecutionTime',
      width: 180,
      render: (time?: string) => (time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'),
    },
    {
      title: '操作',
      key: 'action',
      width: 280,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={record.status === 'ENABLED' ? <PauseCircleOutlined /> : <PlayCircleOutlined />}
            onClick={() => handleToggleStatus(record)}
          >
            {record.status === 'ENABLED' ? '禁用' : '启用'}
          </Button>
          <Button
            type="link"
            size="small"
            icon={<ThunderboltOutlined />}
            onClick={() => handleExecute(record.id)}
          >
            执行
          </Button>
          <Button
            type="link"
            size="small"
            icon={<HistoryOutlined />}
            onClick={() => handleViewLogs(record)}
          >
            日志
          </Button>
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这个任务吗？"
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

  const logColumns: ColumnsType<TaskExecutionLog> = [
    {
      title: '执行时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (time: string) => dayjs(time).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const statusMap: Record<string, { text: string; color: string }> = {
          SUCCESS: { text: '成功', color: 'success' },
          FAILED: { text: '失败', color: 'error' },
          RUNNING: { text: '运行中', color: 'processing' },
        };
        const config = statusMap[status] || { text: status, color: 'default' };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '执行时长',
      dataIndex: 'executionTime',
      key: 'executionTime',
      width: 100,
      render: (time: number) => `${time}ms`,
    },
    {
      title: 'TraceId',
      dataIndex: 'traceId',
      key: 'traceId',
      width: 200,
      render: (traceId: string) => <code className="text-xs">{traceId}</code>,
    },
    {
      title: '错误信息',
      dataIndex: 'errorMessage',
      key: 'errorMessage',
      render: (msg?: string) => msg || '-',
    },
  ];

  return (
    <div className="space-y-6">
      {/* 标题 */}
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-text-primary">任务管理</h1>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
          新建任务
        </Button>
      </div>

      {/* 任务列表 */}
      <Card className="bg-bg-secondary border border-border-primary">
        <Table
          columns={columns}
          dataSource={tasks}
          rowKey="id"
          loading={loading}
          scroll={{ x: 1600 }}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
        />
      </Card>

      {/* 创建/编辑任务弹窗 */}
      <Modal
        title={editingTask ? '编辑任务' : '新建任务'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        width={700}
        footer={[
          <Button key="cancel" onClick={() => setModalVisible(false)}>
            取消
          </Button>,
          <Button key="submit" type="primary" onClick={handleSubmit}>
            {editingTask ? '更新' : '创建'}
          </Button>,
        ]}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 24 }}>
          <Form.Item
            label="任务名称"
            name="taskName"
            rules={[{ required: true, message: '请输入任务名称' }]}
          >
            <Input placeholder="例如: 订单处理任务" />
          </Form.Item>

          <div className="grid grid-cols-2 gap-4">
            <Form.Item
              label="任务类型"
              name="taskType"
              initialValue="MESSAGE"
              rules={[{ required: true, message: '请选择任务类型' }]}
            >
              <Select>
                <Option value="MESSAGE">发送消息</Option>
                <Option value="HTTP">HTTP请求</Option>
                <Option value="SHELL">Shell脚本</Option>
                <Option value="CODE">自定义代码</Option>
              </Select>
            </Form.Item>

            <Form.Item
              label="触发方式"
              name="triggerType"
              initialValue="CRON"
              rules={[{ required: true, message: '请选择触发方式' }]}
            >
              <Select onChange={(value) => setTriggerType(value)}>
                <Option value="CRON">定时执行(Cron)</Option>
                <Option value="DELAY">延迟执行</Option>
                <Option value="IMMEDIATE">立即执行</Option>
              </Select>
            </Form.Item>
          </div>

          {triggerType === 'CRON' && (
            <Form.Item
              label="Cron表达式"
              name="cronExpression"
              rules={[{ required: true, message: '请输入Cron表达式' }]}
              extra="例如: 0 * * * * ? (每分钟执行)"
            >
              <Input placeholder="0 * * * * ?" />
            </Form.Item>
          )}

          {triggerType === 'DELAY' && (
            <Form.Item
              label="延迟时间(秒)"
              name="delaySeconds"
              rules={[{ required: true, message: '请输入延迟时间' }]}
            >
              <InputNumber min={1} placeholder="30" style={{ width: '100%' }} />
            </Form.Item>
          )}

          <Form.Item
            label="所属集群"
            name="clusterId"
            rules={[{ required: true, message: '请选择集群' }]}
          >
            <Select placeholder="请选择集群">
              {clusters.map((cluster) => (
                <Option key={cluster.id} value={cluster.id}>
                  {cluster.clusterName}
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item label="队列名称" name="queueName">
            <Input placeholder="例如: order.queue" />
          </Form.Item>

          <Form.Item
            label="任务内容"
            name="taskContent"
            rules={[{ required: true, message: '请输入任务内容' }]}
            extra="JSON格式或普通文本"
          >
            <TextArea rows={6} placeholder='{"type": "order", "action": "process"}' />
          </Form.Item>
        </Form>
      </Modal>

      {/* 执行日志抽屉 */}
      <Drawer
        title={`任务执行日志 - ${selectedTask?.taskName || ''}`}
        placement="right"
        width={1000}
        onClose={() => setLogDrawerVisible(false)}
        open={logDrawerVisible}
      >
        <Table
          columns={logColumns}
          dataSource={executionLogs}
          rowKey="id"
          loading={logsLoading}
          pagination={{
            pageSize: 20,
            showTotal: (total) => `共 ${total} 条`,
          }}
        />
      </Drawer>
    </div>
  );
};

export default TaskManagement;
