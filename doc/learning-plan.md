# Spring AI Alibaba 全面学习计划

> 基于官方文档整理的渐进式学习路线，从基础到高级，覆盖完整知识体系。

---

## 一、学习路线总览

```
第一阶段：基础入门
  ├── 环境搭建与快速开始
  ├── 核心概念理解
  └── 第一个 AI 应用

第二阶段：核心 API 精通
  ├── ChatClient（Fluent API）
  ├── ChatModel（对话模型）
  ├── Prompt 与 Prompt Template
  └── Structured Output（结构化输出）

第三阶段：多模态与扩展能力
  ├── ImageModel（文生图）
  ├── AudioModel（语音模型）
  └── EmbeddingModel（嵌入模型）

第四阶段：高级应用模式
  ├── Function Calling（函数调用 / 工具）
  ├── Chat Memory（对话记忆）
  └── Vector Store（向量存储）
  
第五阶段：RAG 与综合实战
  ├── Document Retriever（文档检索）
  ├── RAG 完整流程
  ├── Advisors 机制
  └── 综合项目：智能问答助手

第六阶段：AI Agent 与生产化
  ├── 多工具协同 Agent
  ├── 生产化最佳实践
  └── 综合项目：智能机票助手
```
---
## 第一阶段：基础入门

### 1.1 环境搭建与快速开始

**目标：** 搭好开发环境，跑通第一个 AI 对话应用。

| 项目 | 说明 |
|---|---|
| JDK | 要求 17+ |
| 框架 | Spring Boot 3.x + Spring AI Alibaba |
| 当前版本 | `1.0.0-M2.1` |
| API-KEY | 阿里云百炼平台获取 |
| Maven 仓库 | 需添加 Spring Milestones 仓库 |

**关键依赖：**

```xml
<dependency>
    <groupId>com.alibaba.cloud.ai</groupId>
    <artifactId>spring-ai-alibaba-starter</artifactId>
    <version>1.0.0-M2.1</version>
</dependency>
```

**实践任务：**
1. 克隆官方示例并运行 helloworld
2. 理解自动装配机制（spring-ai-alibaba-starter）
3. 配置 API-KEY 和环境变量
4. 编写第一个 ChatController

### 1.2 核心概念理解

需要掌握的核心概念：

- **Model** — AI 模型（文本、图像、音频、嵌入）
- **Prompt** — 输入提示，包含多个角色（System/User/Assistant/Tool）
- **Prompt Template** — 提示词模板引擎（基于 StringTemplate）
- **Token** — 计费与上下文窗口的基本单位
- **Embedding** — 文本的向量化表示
- **Structured Output** — 将模型输出映射为 POJO
- **Function Calling** — AI 模型调用外部函数/API
- **RAG** — 检索增强生成
- **Evaluation** — 评估 AI 回答质量

### 1.3 第一个 AI 应用

**实践任务：**
1. 使用 `ChatClient.Builder` 注入并构建客户端
2. 实现简单的对话接口（`/ai/chat`）
3. 配置全局 `DashScopeChatOptions`（model、temperature 等参数）
4. 尝试在每次调用中动态指定参数

---

## 第二阶段：核心 API 精通

### 2.1 ChatClient — Fluent API

**核心能力：**
- Fluent API 链式调用 `.prompt().user().call().content()`
- 自动装配 `ChatClient.Builder`
- 编程式创建 `ChatClient.create(chatModel)`

**响应处理：**

| 方法 | 返回 | 用途 |
|---|---|---|
| `.content()` | `String` | 纯文本回复 |
| `.chatResponse()` | `ChatResponse` | 含元数据的完整响应（token 用量等） |
| `.entity(Class)` | POJO | 映射为 Java Bean |
| `.entity(ParameterizedTypeReference)` | 泛型类型 | 如 `List<ActorFilms>` |

**实践任务：**
1. 用 content() 实现文本回复
2. 用 entity() 实现结构化输出映射
3. 流式响应（stream）调用
4. 设置默认 System Message

### 2.2 ChatModel — 对话模型

**核心能力：**
- 接收 `Prompt`（一组 Message）作为输入
- 返回 `ChatResponse`
- 支持同步 `call()` 和流式 `stream()`

**实践任务：**
1. 使用 `ChatModel.call(new Prompt(input))`
2. 多 Message 交互（System + User + Assistant）
3. 使用 `ChatOptions` 动态调整模型参数
4. 流式 API 实现打字机效果

### 2.3 Prompt 与 Prompt Template

**消息角色：**

| 角色 | 说明 |
|---|---|
| `SystemMessage` | 系统指令，设定 AI 行为 |
| `UserMessage` | 用户输入 |
| `AssistantMessage` | AI 回复 |
| `Tool/FunctionMessage` | 工具调用结果 |

**API 概览：**
- `PromptTemplate` → 创建 `Prompt` 对象
- `SystemPromptTemplate` → 创建系统消息
- 占位符替换：`Map<String, Object>`

**实践任务：**
1. 创建带占位符的 Prompt Template
2. System Prompt + User Prompt 组合
3. 通过 Map 动态注入变量
4. 多轮对话中的消息组装

### 2.4 Structured Output — 结构化输出

**核心能力：**
- 将模型输出映射为 POJO/Record
- 支持 `entity(Class)` 和 `entity(ParameterizedTypeReference)`

**实践任务：**
1. 定义输出实体（Record 或 POJO）
2. 用 ChatClient entity() 自动映射
3. 处理复杂嵌套结构
4. 配置输出格式约束

---

## 第三阶段：多模态与扩展能力

### 3.1 ImageModel — 文生图

**核心能力：**
- 接收 `ImagePrompt` 输入
- 返回 `ImageResponse`（图片 URL）
- 支持 `ImageOptions`（model、size、quality 等）

**实践任务：**
1. 注入 `ImageModel` 实现文生图
2. 配置不同的模型（如 dall-e-3）
3. 将生成的图片展示到前端

### 3.2 AudioModel — 语音模型

**支持的模型抽象：**
- `SpeechModel` — 文本生成语音（TTS）
- `DashScopeAudioTranscriptionModel` — 语音转文本

**实践任务：**
1. 实现文本转语音接口
2. 实现语音文件转录文本
3. 与对话流程集成

### 3.3 EmbeddingModel — 嵌入模型

**核心 API：**

| 方法 | 说明 |
|---|---|
| `embed(String text)` | 文本 → 向量 |
| `embed(Document document)` | Document → 向量 |
| `embed(List<String> texts)` | 批量文本 → 向量列表 |
| `embedForResponse(List<String>)` | 批量响应（含元数据） |
| `dimensions()` | 获取向量维度 |

**实践任务：**
1. 注入 `EmbeddingModel`
2. 将单条文本转为向量
3. 批量嵌入与相似度比较
4. 理解向量维度与语义空间

---

## 第四阶段：高级应用模式

### 4.1 Function Calling — 函数调用

**核心概念：**
- LLM 自身不能直接调用工具，而是表达调用意图
- 应用程序执行工具并将结果返回给模型
- 支持单个 Prompt 中定义多个函数

**实现方式：**
1. **定义函数** — `@Bean` 返回 `Function<T, R>`
2. **注册函数** — bean name 作为函数标识
3. **动态调用** — 通过 `ChatOptions` 指定

**实践任务：**
1. 实现天气查询 Function
2. 实现订单查询 Function（集成 Service 层）
3. 单 Prompt 中多函数调用
4. 函数调用 + 对话记忆结合

### 4.2 Chat Memory — 对话记忆

**核心能力：**
- 自动维护多轮对话历史
- 基于 `ChatMemory` 接口和 `InMemoryChatMemory`
- 通过 `MessageChatMemoryAdvisor` 集成

**关键参数：**

| 参数 | 说明 |
|---|---|
| `CHAT_MEMORY_CONVERSATION_ID_KEY` | 对话唯一标识 |
| `CHAT_MEMORY_RETRIEVE_SIZE_KEY` | 检索消息条数 |

**实践任务：**
1. 手动维护 Messages 列表实现多轮对话
2. 使用 `InMemoryChatMemory` + Advisor
3. 设置 conversationId 实现多会话隔离
4. 自定义 ChatMemory（如 Redis 存储）

### 4.3 Vector Store — 向量存储

**核心 API：**

| 方法 | 说明 |
|---|---|
| `add(List<Document>)` | 添加文档到向量库 |
| `delete(List<String>)` | 按 ID 删除 |
| `similaritySearch(SearchRequest)` | 相似性搜索 |

**SearchRequest 参数：**
| 参数 | 说明 |
|---|---|
| `query` | 查询文本 |
| `topK` | 返回最相似的 K 条 |
| `similarityThreshold` | 相似度阈值 |
| `filterExpression` | 元数据过滤（DSL 表达式） |

**实践任务：**
1. 集成 DashScope 向量存储（百炼知识库）
2. 文档嵌入与存储
3. 相似性搜索
4. 带过滤条件的搜索

---

## 第五阶段：RAG 与综合实战

### 5.1 RAG 完整流程

**ETL 管道：**
1. **读取** — DocumentReader 读取非结构化数据
2. **分割** — DocumentSplitter 按语义边界拆分
3. **嵌入** — EmbeddingModel 转为向量
4. **存储** — 存入 VectorStore

**检索流程：**
1. 用户提问
2. 问题向量化 → VectorStore 相似性搜索
3. 检索结果 + 原始问题 → 组装 Prompt
4. 发送给 ChatModel 生成回答

**实践任务：**
1. 实现完整的文档读取 → 分割 → 嵌入 → 存储流程
2. 使用 `QuestionAnswerAdvisor` 实现 RAG
3. 用本地文档作为知识库回答问题
4. 评估 RAG 效果

### 5.2 Advisors 机制

**常见 Advisor：**

| Advisor | 作用 |
|---|---|
| `MessageChatMemoryAdvisor` | 对话记忆 |
| `QuestionAnswerAdvisor` | RAG 检索 |

**实践任务：**
1. 组合多个 Advisor
2. 自定义 Advisor
3. 理解 Advisor 执行顺序

---

## 第六阶段：AI Agent 与生产化

### 6.1 多工具协同 Agent

**核心能力：**
- 多个 Function Calling 组合
- 工具选择与决策
- 多轮工具调用链

**实践任务：**
1. 设计 Agent 工具集（搜索 + 计算 + 查询）
2. 实现多步推理与工具调用
3. 构建简单 AI Agent

### 6.2 生产化最佳实践

**关键关注点：**
- API-KEY 安全管理
- 错误处理与重试
- Token 用量监控与成本控制
- 响应缓存策略
- 日志与可观测性

**实践任务：**
1. 设计异常处理机制
2. 实现 Token 用量日志
3. 简单的缓存策略

### 6.3 综合项目：智能机票助手

参照官方的"智能机票助手"实践项目，综合运用以下技术：
- ChatClient + ChatModel
- Function Calling（查航班、查价格、下单）
- Chat Memory（多轮对话上下文）
- Structured Output（结构化展示结果）

---

## 学习进度跟踪

| 阶段 | 模块 | 预计时间 | 状态 |
|---|---|---|---|
| 一 | 环境搭建与快速开始 | 1 天 |  |
| 一 | 核心概念理解 | 1 天 |  |
| 二 | ChatClient | 2 天 |  |
| 二 | ChatModel | 1 天 |  |
| 二 | Prompt 与 Prompt Template | 1 天 |  |
| 二 | Structured Output | 1 天 |  |
| 三 | ImageModel | 1 天 |  |
| 三 | AudioModel | 1 天 |  |
| 三 | EmbeddingModel | 1 天 |  |
| 四 | Function Calling | 2 天 |  |
| 四 | Chat Memory | 1 天 |  |
| 四 | Vector Store | 1 天 |  |
| 五 | RAG 完整流程 | 2 天 |  |
| 五 | Advisors | 1 天 |  |
| 六 | AI Agent 实战 | 2 天 |  |
| 六 | 综合项目 | 3 天 |  |

---

## 参考文档

- [Spring AI Alibaba 概述](https://sca.aliyun.com/en/docs/ai/overview/)
- [核心概念](https://sca.aliyun.com/en/docs/ai/concepts/)
- [快速开始](https://sca.aliyun.com/en/docs/ai/get-started/)
- [ChatClient](https://sca.aliyun.com/en/docs/ai/tutorials/chat-client/)
- [对话模型](https://sca.aliyun.com/en/docs/ai/tutorials/chat-model/)
- [嵌入模型](https://sca.aliyun.com/en/docs/ai/tutorials/embedding/)
- [函数调用](https://sca.aliyun.com/en/docs/ai/tutorials/function-calling/)
- [对话记忆](https://sca.aliyun.com/en/docs/ai/tutorials/memory/)
- [提示词](https://sca.aliyun.com/en/docs/ai/tutorials/prompt/)
- [向量存储](https://sca.aliyun.com/en/docs/ai/tutorials/vectorstore/)
- [结构化输出](https://sca.aliyun.com/en/docs/ai/tutorials/structured-output/)
