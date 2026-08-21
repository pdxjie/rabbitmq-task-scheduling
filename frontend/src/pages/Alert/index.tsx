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
  Tabs,
  InputNumber,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  BellOutlined,
  HistoryOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import {
  getAlertRuleList,
  createAlertRule,
  updateAlertRule,
  deleteAlertRule,
  enableAlertRule,
  disableAlertRule,
  testAlert,
  getAlertRecordList,
} from '@/services/alert';
import { getAllClusters } from '@/services/cluster';
import type { AlertRule, AlertRecord, Cluster } from '@/types';
import dayjs from 'dayjs';

const { Option } = Select;
const { TextArea } = Input;

const AlertManagement: React.FC = () => {
  const [activeTab, setActiveTab] = useState<string>('rules');
  const [rules, setRules] = useState<AlertRule[]>([]);
  const [records, setRecords] = useState<AlertRecord[]>([]);
  const [clusters, setClusters] = useState<Cluster[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingRule, setEditingRule] = useState<AlertRule | null>(null);
  const [form] = Form.useForm();
  const [ruleType, setRuleType] = useState<string>('TASK_FAIL');

  useEffect(() => {
    loadRules();
    loadClusters();
  }, []);

  useEffect(() => {
    if (activeTab === 'records') {
      loadRecords();
    }
  }, [activeTab]);

  const loadRules = async () => {
    try {
      setLoading(true);
      const response = await getAlertRuleList({ current: 1, size: 100 });
      // 后端返回的是数组，不是分页对象
      if (Array.isArray(response.data)) {
        setRules(response.data);
      } else if (response.data?.records) {
        setRules(response.data.records);
      }
    } catch (error) {
      console.error('获取告警规则失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadRecords = async () => {
    try {
      setLoading(true);
      const response = await getAlertRecordList({ current: 1, size: 100 });
      if (response.data?.records) {
        setRecords(response.data.records);
      }
    } catch (error) {
      console.error('获取告警记录失败:', error);
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

  const handleCreate = () => {
    setEditingRule(null);
    form.resetFields();
    setRuleType('TASK_FAIL');
    setModalVisible(true);
  };

  const handleEdit = (rule: AlertRule) => {
    setEditingRule(rule);
    setRuleType(rule.ruleType);

    // 解析 conditionJson
    let parsedCondition = {};
    try {
      parsedCondition = JSON.parse(rule.conditionJson);
    } catch (e) {
      console.error('解析条件失败:', e);
    }

    form.setFieldsValue({
      ...rule,
      ...parsedCondition,
      notificationChannels: rule.notificationChannels.split(','),
    });
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteAlertRule(id);
      message.success('删除成功');
      loadRules();
    } catch (error) {
      message.error('删除失败');
    }
  };

  const handleToggleStatus = async (rule: AlertRule) => {
    try {
      if (rule.status === 'ENABLED') {
        await disableAlertRule(rule.id);
        message.success('已禁用');
      } else {
        await enableAlertRule(rule.id);
        message.success('已启用');
      }
      loadRules();
    } catch (error) {
      message.error('操作失败');
    }
  };

  const handleTest = async (id: number) => {
    try {
      await testAlert(id);
      message.success('测试告警已发送');
    } catch (error) {
      message.error('发送失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();

      // 构建 conditionJson
      const conditionJson: any = {
        type: values.ruleType,
      };

      if (values.threshold) {
        conditionJson.threshold = values.threshold;
      }
      if (values.operator) {
        conditionJson.operator = values.operator;
      }

      const ruleData = {
        ruleName: values.ruleName,
        ruleType: values.ruleType,
        clusterId: values.clusterId,
        conditionJson: JSON.stringify(conditionJson),
        notificationChannels: values.notificationChannels.join(','),
        notificationUsers: values.notificationUsers,
        status: 'ENABLED',
      };

      if (editingRule) {
        await updateAlertRule(editingRule.id, ruleData);
        message.success('更新成功');
      } else {
        await createAlertRule(ruleData);
        message.success('创建成功');
      }
      setModalVisible(false);
      loadRules();
    } catch (error: any) {
      if (!error.errorFields) {
        message.error(editingRule ? '更新失败' : '创建失败');
      }
    }
  };

  const ruleColumns: ColumnsType<AlertRule> = [
    {
      title: '规则名称',
      dataIndex: 'ruleName',
      key: 'ruleName',
      width: 200,
    },
    {
      title: '规则类型',
      dataIndex: 'ruleType',
      key: 'ruleType',
      width: 150,
      render: (type: string) => {
        const typeMap: Record<string, { text: string; color: string }> = {
          TASK_FAIL: { text: '任务失败', color: 'red' },
          QUEUE_BACKLOG: { text: '队列积压', color: 'orange' },
          CONSUMER_OFFLINE: { text: '消费者离线', color: 'purple' },
          EXECUTION_TIME: { text: '执行超时', color: 'blue' },
        };
        const config = typeMap[type] || { text: type, color: 'default' };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '告警条件',
      dataIndex: 'conditionJson',
      key: 'conditionJson',
      width: 200,
      render: (json: string) => {
        try {
          const condition = JSON.parse(json);
          if (condition.threshold && condition.operator) {
            const operatorMap: Record<string, string> = {
              GT: '>',
              LT: '<',
              EQ: '=',
              GTE: '>=',
              LTE: '<=',
            };
            return (
              <span>
                {operatorMap[condition.operator] || condition.operator} {condition.threshold}
              </span>
            );
          }
          return '-';
        } catch {
          return '-';
        }
      },
    },
    {
      title: '通知渠道',
      dataIndex: 'notificationChannels',
      key: 'notificationChannels',
      width: 200,
      render: (channels: string) => {
        const channelMap: Record<string, string> = {
          EMAIL: '邮件',
          DINGTALK: '钉钉',
          WECHAT: '企业微信',
          SMS: '短信',
        };
        return channels
          .split(',')
          .map((ch) => (
            <Tag key={ch} color="blue">
              {channelMap[ch] || ch}
            </Tag>
          ));
      },
    },
    {
      title: '通知用户',
      dataIndex: 'notificationUsers',
      key: 'notificationUsers',
      width: 150,
      render: (users?: string) => users || '-',
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
      title: '操作',
      key: 'action',
      width: 250,
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
            icon={<BellOutlined />}
            onClick={() => handleTest(record.id)}
          >
            测试
          </Button>
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这个告警规则吗？"
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

  const recordColumns: ColumnsType<AlertRecord> = [
    {
      title: '告警时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (time: string) => dayjs(time).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '告警级别',
      dataIndex: 'level',
      key: 'level',
      width: 100,
      render: (level: string) => {
        const levelMap: Record<string, { text: string; color: string }> = {
          INFO: { text: '信息', color: 'blue' },
          WARNING: { text: '警告', color: 'orange' },
          ERROR: { text: '错误', color: 'red' },
          CRITICAL: { text: '严重', color: 'magenta' },
        };
        const config = levelMap[level] || { text: level, color: 'default' };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '规则名称',
      dataIndex: 'ruleName',
      key: 'ruleName',
      width: 200,
    },
    {
      title: '告警标题',
      dataIndex: 'title',
      key: 'title',
      width: 250,
    },
    {
      title: '告警内容',
      dataIndex: 'content',
      key: 'content',
      ellipsis: true,
    },
    {
      title: '发送状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const statusMap: Record<string, { text: string; color: string }> = {
          PENDING: { text: '待发送', color: 'default' },
          SENT: { text: '已发送', color: 'success' },
          FAILED: { text: '发送失败', color: 'error' },
        };
        const config = statusMap[status] || { text: status, color: 'default' };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
  ];

  return (
    <div className="space-y-6">
      {/* 标题 */}
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-text-primary">告警管理</h1>
        {activeTab === 'rules' && (
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
            新建告警规则
          </Button>
        )}
      </div>

      {/* Tab 切换 */}
      <Card className="bg-bg-secondary border border-border-primary">
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            {
              key: 'rules',
              label: (
                <span>
                  <BellOutlined /> 告警规则
                </span>
              ),
              children: (
                <Table
                  columns={ruleColumns}
                  dataSource={rules}
                  rowKey="id"
                  loading={loading}
                  scroll={{ x: 1400 }}
                  pagination={{
                    pageSize: 10,
                    showSizeChanger: true,
                    showTotal: (total) => `共 ${total} 条`,
                  }}
                />
              ),
            },
            {
              key: 'records',
              label: (
                <span>
                  <HistoryOutlined /> 告警记录
                </span>
              ),
              children: (
                <Table
                  columns={recordColumns}
                  dataSource={records}
                  rowKey="id"
                  loading={loading}
                  pagination={{
                    pageSize: 10,
                    showSizeChanger: true,
                    showTotal: (total) => `共 ${total} 条`,
                  }}
                />
              ),
            },
          ]}
        />
      </Card>

      {/* 创建/编辑告警规则弹窗 */}
      <Modal
        title={editingRule ? '编辑告警规则' : '新建告警规则'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        width={600}
        footer={[
          <Button key="cancel" onClick={() => setModalVisible(false)}>
            取消
          </Button>,
          <Button key="submit" type="primary" onClick={handleSubmit}>
            {editingRule ? '更新' : '创建'}
          </Button>,
        ]}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 24 }}>
          <Form.Item
            label="规则名称"
            name="ruleName"
            rules={[{ required: true, message: '请输入规则名称' }]}
          >
            <Input placeholder="例如: 任务失败告警" />
          </Form.Item>

          <Form.Item
            label="规则类型"
            name="ruleType"
            initialValue="TASK_FAIL"
            rules={[{ required: true, message: '请选择规则类型' }]}
          >
            <Select onChange={(value) => setRuleType(value)}>
              <Option value="TASK_FAIL">任务失败告警</Option>
              <Option value="QUEUE_BACKLOG">队列积压告警</Option>
              <Option value="CONSUMER_OFFLINE">消费者离线告警</Option>
              <Option value="EXECUTION_TIME">执行超时告警</Option>
            </Select>
          </Form.Item>

          {['QUEUE_BACKLOG', 'CONSUMER_OFFLINE'].includes(ruleType) && (
            <Form.Item label="所属集群" name="clusterId">
              <Select placeholder="请选择集群">
                {clusters.map((cluster) => (
                  <Option key={cluster.id} value={cluster.id}>
                    {cluster.clusterName}
                  </Option>
                ))}
              </Select>
            </Form.Item>
          )}

          {['QUEUE_BACKLOG', 'EXECUTION_TIME'].includes(ruleType) && (
            <>
              <div className="grid grid-cols-2 gap-4">
                <Form.Item
                  label="比较运算符"
                  name="operator"
                  rules={[{ required: true, message: '请选择运算符' }]}
                >
                  <Select>
                    <Option value="GT">大于 (&gt;)</Option>
                    <Option value="GTE">大于等于 (&gt;=)</Option>
                    <Option value="LT">小于 (&lt;)</Option>
                    <Option value="LTE">小于等于 (&lt;=)</Option>
                    <Option value="EQ">等于 (=)</Option>
                  </Select>
                </Form.Item>

                <Form.Item
                  label="阈值"
                  name="threshold"
                  rules={[{ required: true, message: '请输入阈值' }]}
                >
                  <InputNumber
                    min={0}
                    placeholder={ruleType === 'QUEUE_BACKLOG' ? '1000' : '300'}
                    style={{ width: '100%' }}
                  />
                </Form.Item>
              </div>
            </>
          )}

          <Form.Item
            label="通知渠道"
            name="notificationChannels"
            initialValue={['DINGTALK']}
            rules={[{ required: true, message: '请选择通知渠道' }]}
          >
            <Select mode="multiple" placeholder="请选择通知渠道">
              <Option value="EMAIL">邮件</Option>
              <Option value="DINGTALK">钉钉</Option>
              <Option value="WECHAT">企业微信</Option>
              <Option value="SMS">短信</Option>
            </Select>
          </Form.Item>

          <Form.Item label="通知用户" name="notificationUsers">
            <TextArea rows={2} placeholder="例如: admin@company.com,13800138000（多个用逗号分隔）" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default AlertManagement;
