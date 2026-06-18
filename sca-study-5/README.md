# sca-study-5 — AI Agent 与生产化

第六阶段：多工具协同 Agent 与综合实战。通过 `@Tool` 注解注册天气、航班搜索、下单购票三个工具，模型自动按需选择与多步推理。`FlightAssistantController` 结合 Chat Memory 实现完整的智能机票助手场景（查天气 → 搜航班 → 下单 → 查订单），支持同步与流式两种返回方式。
