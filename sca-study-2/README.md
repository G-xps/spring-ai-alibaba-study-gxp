# sca-study-2 — 多模态与扩展能力

## 概述

第三阶段学习模块，演示 Spring AI Alibaba 的多模态能力，包括文生图和文本嵌入。

## 模块内容

| 模块 | 接口 | 说明 |
|---|---|---|
| **ImageModel** | `/ai/image/generate` | 文生图，返回图片 URL |
|  | `/ai/image/response` | 文生图，返回图片 URL + 描述信息 |
| **EmbeddingModel** | `/ai/embedding/single` | 单条文本 → 向量，展示维度与全部值 |
|  | `/ai/embedding/batch` | 批量文本 → 向量，含 Token 用量统计 |
|  | `/ai/embedding/dimensions` | 查询当前模型的向量维度 |

## 模型

- **文生图**：通义万相（`wanx-v1`），1024×1024
- **嵌入**：通义千问 text-embedding-v3（由 starter 自动配置）

## 运行

```bash
# 需设置 API-KEY
export SPRING_AI_DASHSCOPE_API_KEY=your_api_key

mvn spring-boot:run
```

## 测试

```bash
mvn test
```
