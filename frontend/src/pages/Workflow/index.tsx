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
  message,
  Popconfirm,
  Drawer,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  ThunderboltOutlined,
  ApartmentOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import {
  getWorkflowList,
  createWorkflow,
  updateWorkflow,
  deleteWorkflow,
  enableWorkflow,
  disableWorkflow,
  executeWorkflow,
} from '@/services/workflow';
import { getAllClusters } from '@/services/cluster';
import { getTaskList } from '@/services/task';
import type { Workflow, Cluster, Task } from '@/types';
import dayjs from 'dayjs';

const { Option } = Select;
const { TextArea } = Input;

const WorkflowManagement: React.FC = () => {
  const [workflows, setWorkflows] = useState<Workflow[]>([]);
  const [clusters, setClusters] = useState<Cluster[]>([]);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [canvasDrawerVisible, setCanvasDrawerVisible] = useState(false);
  const [editingWorkflow, setEditingWorkflow] = useState<Workflow | null>(null);
  const [selectedWorkflow, setSelectedWorkflow] = useState<Workflow | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    loadWorkflows();
    loadClusters();
    loadTasks();
  }, []);

  const loadWorkflows = async () => {
    try {
      setLoading(true);
      const response = await getWorkflowList({ current: 1, size: 100 });
      if (response.data?.records) {
        setWorkflows(response.data.records);
      }
    } catch (error) {
      console.error('获取工作流列表失败:', error);
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

  const loadTasks = async () => {
    try {
      const response = await getTaskList({ current: 1, size: 100 });
      if (response.data?.records) {
        setTasks(response.data.records);
      }
    } catch (error) {
      console.error('获取任务列表失败:', error);
    }
  };

  const handleCreate = () => {
    setEditingWorkflow(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (workflow: Workflow) => {
    setEditingWorkflow(workflow);
    form.setFieldsValue({
      ...workflow,
      dagDefinition: workflow.dagDefinition,
    });
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteWorkflow(id);
      message.success('删除成功');
      loadWorkflows();
    } catch (error) {
      message.error('删除失败');
    }
  };

  const handleToggleStatus = async (workflow: Workflow) => {
    try {
      if (workflow.status === 'ENABLED') {
        await disableWorkflow(workflow.id);
        message.success('已禁用');
      } else {
        await enableWorkflow(workflow.id);
        message.success('已启用');
      }
      loadWorkflows();
    } catch (error) {
      message.error('操作失败');
    }
  };

  const handleExecute = async (id: number) => {
    try {
      await executeWorkflow(id);
      message.success('工作流已提交执行');
    } catch (error) {
      message.error('执行失败');
    }
  };

  const handleOpenCanvas = (workflow: Workflow) => {
    setSelectedWorkflow(workflow);
    setCanvasDrawerVisible(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();

      // 简化的 DAG 定义（示例）
      const dagDefinition = values.dagDefinition || JSON.stringify({
        nodes: [],
        edges: [],
      });

      const workflowData = {
        workflowName: values.workflowName,
        description: values.description,
        dagDefinition: dagDefinition,
        status: 'ENABLED',
      };

      if (editingWorkflow) {
        await updateWorkflow(editingWorkflow.id, workflowData);
        message.success('更新成功');
      } else {
        await createWorkflow(values.clusterId, workflowData);
        message.success('创建成功');
      }
      setModalVisible(false);
      loadWorkflows();
    } catch (error: any) {
      if (!error.errorFields) {
        message.error(editingWorkflow ? '更新失败' : '创建失败');
      }
    }
  };

  const columns: ColumnsType<Workflow> = [
    {
      title: '工作流名称',
      dataIndex: 'workflowName',
      key: 'workflowName',
      width: 200,
      fixed: 'left',
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      width: 250,
      ellipsis: true,
    },
    {
      title: '所属集群',
      dataIndex: 'clusterId',
      key: 'clusterId',
      width: 150,
      render: (clusterId: number) => {
        const cluster = clusters.find((c) => c.id === clusterId);
        return cluster?.clusterName || '-';
      },
    },
    {
      title: '版本',
      dataIndex: 'version',
      key: 'version',
      width: 80,
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
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (time: string) => dayjs(time).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '更新时间',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      width: 180,
      render: (time: string) => dayjs(time).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '操作',
      key: 'action',
      width: 320,
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
            icon={<ApartmentOutlined />}
            onClick={() => handleOpenCanvas(record)}
          >
            查看
          </Button>
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这个工作流吗？"
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
        <h1 className="text-3xl font-bold text-text-primary">工作流编排</h1>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
          新建工作流
        </Button>
      </div>

      {/* 工作流列表 */}
      <Card className="bg-bg-secondary border border-border-primary">
        <Table
          columns={columns}
          dataSource={workflows}
          rowKey="id"
          loading={loading}
          scroll={{ x: 1400 }}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
        />
      </Card>

      {/* 创建/编辑工作流弹窗 */}
      <Modal
        title={editingWorkflow ? '编辑工作流' : '新建工作流'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        width={700}
        footer={[
          <Button key="cancel" onClick={() => setModalVisible(false)}>
            取消
          </Button>,
          <Button key="submit" type="primary" onClick={handleSubmit}>
            {editingWorkflow ? '更新' : '创建'}
          </Button>,
        ]}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 24 }}>
          <Form.Item
            label="工作流名称"
            name="workflowName"
            rules={[{ required: true, message: '请输入工作流名称' }]}
          >
            <Input placeholder="例如: 订单处理流程" />
          </Form.Item>

          <Form.Item label="描述" name="description">
            <TextArea rows={3} placeholder="请输入工作流描述" />
          </Form.Item>

          {!editingWorkflow && (
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
          )}

          <Form.Item
            label="DAG 定义 (JSON)"
            name="dagDefinition"
            extra="高级选项：直接编辑 DAG 定义（nodes 和 edges）"
          >
            <TextArea
              rows={8}
              placeholder={`{\n  "nodes": [],\n  "edges": []\n}`}
            />
          </Form.Item>
        </Form>
      </Modal>

      {/* 工作流画布查看抽屉 */}
      <Drawer
        title={`工作流: ${selectedWorkflow?.workflowName || ''}`}
        placement="right"
        width={1200}
        onClose={() => setCanvasDrawerVisible(false)}
        open={canvasDrawerVisible}
      >
        <div className="h-full bg-bg-tertiary rounded border border-border-primary p-4">
          <div className="flex items-center justify-center h-full text-text-secondary">
            <div className="text-center space-y-4">
              <ApartmentOutlined style={{ fontSize: 48 }} />
              <div>
                <p className="text-lg">工作流可视化画布</p>
                <p className="text-sm">@antv/X6 集成 - 待实现</p>
                <p className="text-xs mt-4">当前 DAG 定义:</p>
                <pre className="text-left mt-2 p-4 bg-bg-secondary rounded text-xs max-w-2xl overflow-auto">
                  {selectedWorkflow?.dagDefinition || '{}'}
                </pre>
              </div>
            </div>
          </div>
        </div>
      </Drawer>
    </div>
  );
};

export default WorkflowManagement;
