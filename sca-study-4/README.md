# sca-study-4 — RAG 与综合实战

第五阶段：检索增强生成（RAG）完整流程。启动时自动从 knowledge 目录加载文档，经 TokenTextSplitter 分割后通过 EmbeddingModel 存入 VectorStore。使用 QuestionAnswerAdvisor 实现知识库问答，并与 MessageChatMemoryAdvisor 组合支持多轮对话。包含自定义 Advisor 示例（日志记录与引用溯源），演示 Spring AI Advisor 洋葱模型执行顺序。
