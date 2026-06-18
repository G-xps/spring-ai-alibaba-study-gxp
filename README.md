# spring-ai-alibaba-study-gxp

> 记录本人学习 **Spring Cloud AI Alibaba** 的学习历程，包括测试用例
> **注意**：这个仓库是基础试验，更多高级应用在 [spring-ai-alibaba-projects-gxp](https://github.com/G-xps/spring-ai-alibaba-projects-gxp.git)

## 目录结构

```
├── doc/                         # 学习文档与学习计划
├── sca-study-1/                 # 第一～二阶段：基础入门 + 核心 API（ChatClient / ChatModel / Prompt / Structured Output）
├── sca-study-2/                 # 第三阶段：多模态（文生图 + 文本嵌入）
├── sca-study-3/                 # 第四阶段：高级应用模式（Function Calling / Chat Memory / Vector Store）
├── sca-study-4/                 # 第五阶段：RAG 与综合实战
├── sca-study-5/                 # 第六阶段：AI Agent 与生产化
├── README.md                    # 本文件
└── LICENSE
```

各模块详情见下对应 `README.md`。


> 学习计划已制定，详见 [doc/learning-plan.md](doc/learning-plan.md)，代码逐步搭建中

## 学习内容

本仓库涵盖以下模块的实践与笔记（按学习顺序）：

| # | 阶段 | 核心内容 |
|---|------|---------|
| 一 | 基础入门 | 环境搭建、核心概念、Hello World |
| 二 | 核心 API | ChatClient、ChatModel、Prompt、Structured Output |
| 三 | 多模态 | ImageModel、AudioModel、EmbeddingModel |
| 四 | 高级模式 | Function Calling、Chat Memory、Vector Store |
| 五 | RAG 实战 | 文档检索、RAG 流程、Advisors |
| 六 | Agent 与生产化 | 多工具协同、综合项目 |

（详细学习计划与每模块的实践任务请见 [doc/learning-plan.md](doc/learning-plan.md)）

## 技术栈

| 项目 | 说明                              |
|---|---------------------------------|
| 框架 | Spring AI Alibaba `1.0.0-M6.1`  |
| 基础 | Spring Boot 3.3.x + Spring AI   |
| 语言 | Java 17+                        |
| AI 服务 | 阿里云通义千问（百炼平台）                   |
| 构建工具 | Maven（需配置 Spring Milestones 仓库） |

## 使用方式

```bash
# 1. 设置阿里云百炼 API-KEY
set QWEN_API_KEY=your_api_key

# 2. 运行指定模块（如第四阶段）
mvn spring-boot:run -pl sca-study-3 -am

# 3. 运行测试
mvn test -pl sca-study-3 -am
```


