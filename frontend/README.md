# TaskFlow 前端项目

## 🎉 项目状态

前端基础框架已搭建完成，可以访问 http://localhost:3000 查看效果。

---

## 📦 技术栈

### 核心框架
- **React 18.3** - UI 框架
- **TypeScript 5.5** - 类型安全
- **Vite 8.2** - 构建工具

### UI 组件库
- **Ant Design 5.20** - 企业级 UI 组件
- **Tailwind CSS** - 原子化 CSS
- **Framer Motion** - 动画库

### 状态管理
- **Zustand** - 轻量级状态管理

### 路由
- **React Router 6** - 路由管理

### 数据可视化
- **ECharts** - 图表库（待集成）
- **@antv/X6** - 工作流编辑器（待集成）

### 工具库
- **Axios** - HTTP 客户端
- **dayjs** - 日期处理
- **react-countup** - 数字滚动动画

---

## 📁 项目结构

```
frontend/
├── public/                 # 静态资源
├── src/
│   ├── components/         # 组件
│   │   ├── layout/        # 布局组件
│   │   │   ├── DashboardLayout.tsx
│   │   │   ├── Sidebar.tsx
│   │   │   └── Header.tsx
│   │   ├── common/        # 通用组件
│   │   │   └── MetricCard.tsx
│   │   └── business/      # 业务组件
│   ├── pages/             # 页面
│   │   └── Dashboard/     # 概览页
│   ├── services/          # API 服务
│   │   ├── cluster.ts
│   │   ├── task.ts
│   │   └── monitor.ts
│   ├── stores/            # 状态管理
│   │   └── monitor.ts
│   ├── hooks/             # 自定义 Hooks
│   │   └── useWebSocket.ts
│   ├── utils/             # 工具函数
│   │   └── request.ts
│   ├── types/             # TypeScript 类型
│   │   └── index.ts
│   ├── App.tsx            # 应用入口
│   ├── main.tsx           # React 渲染入口
│   └── index.css          # 全局样式
├── package.json
├── vite.config.ts         # Vite 配置
├── tailwind.config.js     # Tailwind 配置
└── tsconfig.app.json      # TypeScript 配置
```

---

## ✅ 已完成功能

### 1. 基础框架
- ✅ Vite + React + TypeScript 项目搭建
- ✅ Tailwind CSS 集成
- ✅ Ant Design 深色主题配置
- ✅ 路径别名配置 (`@/` 指向 `src/`)
- ✅ API 代理配置（`/api` → `http://localhost:8080`）
- ✅ WebSocket 代理配置（`/ws` → `ws://localhost:8080`）

### 2. 布局系统
- ✅ DashboardLayout - 主布局
- ✅ Sidebar - 侧边栏导航（7个菜单项）
- ✅ Header - 顶部导航栏（搜索、通知、用户菜单）

### 3. 路由配置
- ✅ React Router 集成
- ✅ 7个页面路由（概览、集群、任务、工作流、监控、告警、设置）

### 4. API 服务层
- ✅ Axios 封装（请求/响应拦截器）
- ✅ 集群管理 API
- ✅ 任务管理 API
- ✅ 监控 API

### 5. 状态管理
- ✅ Zustand 集成
- ✅ 监控数据状态管理

### 6. 自定义 Hooks
- ✅ useWebSocket - WebSocket 连接管理（支持自动重连）

### 7. 通用组件
- ✅ MetricCard - 统计卡片（支持数字滚动动画、趋势显示）

### 8. 页面开发
- ✅ Dashboard 概览页（基础版）
  - 4个统计卡片
  - 实时 WebSocket 数据更新
  - 图表区域占位
  - 热点任务/告警列表占位
  - 集群状态展示占位

---

## 🎨 设计规范

### 色彩系统（深色主题）

```css
/* 背景色 */
--bg-primary: #0a0a0a      /* 主背景 */
--bg-secondary: #161616    /* 卡片背景 */
--bg-tertiary: #262626     /* 悬停背景 */

/* 文字色 */
--text-primary: #ffffff    /* 主文字 */
--text-secondary: #a1a1a1  /* 次要文字 */
--text-tertiary: #6b6b6b   /* 辅助文字 */

/* 边框色 */
--border-primary: #2a2a2a  /* 边框 */
--border-secondary: #404040 /* 高亮边框 */

/* 品牌色 */
--brand-primary: #3b82f6   /* 主品牌色 - 蓝色 */
```

### 状态色

- 成功: `#10b981` (绿色)
- 警告: `#f59e0b` (橙色)
- 错误: `#ef4444` (红色)
- 信息: `#3b82f6` (蓝色)

---

## 🚀 快速开始

### 1. 安装依赖

```bash
cd frontend
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

访问: http://localhost:3000

### 3. 构建生产版本

```bash
npm run build
```

### 4. 预览生产版本

```bash
npm run preview
```

---

## 🔧 配置说明

### API 代理

前端开发服务器已配置 API 代理，所有 `/api` 开头的请求会自动转发到后端服务器：

```typescript
// vite.config.ts
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
  },
}
```

### WebSocket 代理

WebSocket 连接也已配置代理：

```typescript
'/ws': {
  target: 'ws://localhost:8080',
  ws: true,
}
```

### 路径别名

使用 `@/` 作为 `src/` 的别名：

```typescript
import MetricCard from '@/components/common/MetricCard';
import { getSystemMetrics } from '@/services/monitor';
import type { SystemMetrics } from '@/types';
```

---

## 📝 开发进度

### Phase 1: 基础框架 ✅ (已完成)
- ✅ 项目初始化
- ✅ 依赖安装
- ✅ 配置文件设置
- ✅ 目录结构创建
- ✅ 类型定义
- ✅ API 服务封装
- ✅ 布局组件
- ✅ 路由配置

### Phase 2: 核心页面 🚧 (开发中)
- ✅ 概览页（基础版）
- ⏳ 集群管理页
- ⏳ 任务管理页
- ⏳ 工作流编排页
- ⏳ 监控大屏页
- ⏳ 告警管理页

### Phase 3: 数据可视化 ⏳ (待开始)
- ⏳ ECharts 图表集成
- ⏳ 实时数据刷新
- ⏳ @antv/X6 工作流编辑器

### Phase 4: 交互优化 ⏳ (待开始)
- ⏳ 页面切换动画
- ⏳ 加载状态优化
- ⏳ 错误处理优化
- ⏳ 响应式适配

---

## 🎯 下一步计划

### 1. 完善概览页
- [ ] 集成 ECharts 实时折线图
- [ ] 实现热点任务列表（真实数据）
- [ ] 实现实时告警列表（真实数据）
- [ ] 完善集群状态卡片（真实数据）

### 2. 集群管理页
- [ ] 集群列表展示
- [ ] 连接新集群表单
- [ ] 集群连接测试
- [ ] 集群详情查看
- [ ] 集群编辑/删除

### 3. 任务管理页
- [ ] 任务列表（支持筛选、搜索、分页）
- [ ] 创建任务表单（Cron/延迟/立即执行）
- [ ] 任务详情查看
- [ ] 任务启用/禁用
- [ ] 立即执行任务
- [ ] 任务执行日志查看

### 4. 工作流编排页
- [ ] 集成 @antv/X6 画布
- [ ] 节点库（任务节点、条件节点、并行节点）
- [ ] 节点拖拽添加
- [ ] 连线绘制
- [ ] 节点属性配置面板
- [ ] DAG 验证
- [ ] 保存工作流

### 5. 监控大屏页
- [ ] 全屏监控布局
- [ ] 系统监控指标卡片
- [ ] 实时图表（吞吐量、延迟等）
- [ ] 集群状态监控
- [ ] 队列监控
- [ ] WebSocket 实时数据推送

### 6. 告警管理页
- [ ] 告警规则列表
- [ ] 创建告警规则
- [ ] 告警记录查询
- [ ] 告警通知渠道配置
- [ ] 测试告警发送

---

## 🐛 已知问题

1. ⚠️ Vite 配置警告：使用了 `__dirname`，建议改用 `import.meta.dirname`
2. ⏳ 图表组件尚未集成
3. ⏳ 工作流编辑器尚未集成
4. ⏳ 部分页面为占位符

---

## 📚 相关文档

- [后端 API 文档](../API_GUIDE.md)
- [前端设计方案](../FRONTEND_DESIGN.md)
- [监控告警指南](../MONITOR_ALERT_GUIDE.md)
- [工作流指南](../WORKFLOW_GUIDE.md)

---

## 🤝 开发规范

### 组件命名
- 使用 PascalCase（如 `MetricCard.tsx`）
- 一个文件一个组件
- 使用 `.tsx` 扩展名

### 类型定义
- 统一在 `src/types/index.ts` 定义共享类型
- 组件内部类型可定义在组件文件中
- 使用 `interface` 而非 `type`

### 样式编写
- 优先使用 Tailwind CSS 原子类
- 使用 Ant Design 组件的 `className` 属性
- 避免内联样式（除非动态计算）

### API 调用
- 统一通过 `src/services` 目录中的服务函数
- 使用 `async/await` 语法
- 统一错误处理

---

**项目状态**: 基础框架完成，核心页面开发中  
**最后更新**: 2026-08-21  
**开发进度**: 30%
