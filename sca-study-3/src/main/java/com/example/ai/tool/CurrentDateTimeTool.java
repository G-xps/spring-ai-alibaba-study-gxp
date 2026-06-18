package com.example.ai.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 使用 @Tool 注解定义工具（推荐的新方式）。
 * <p>
 * 通过 @Tool 注解标记方法，然后通过 .tools(Object...) 注册整个 Bean，
 * 框架会自动扫描 @Tool 注解并注册为可调用工具。
 */
@Component
public class CurrentDateTimeTool {

    @Tool(description = "获取当前日期")
    public String getCurrentDate(ToolContext toolContext) {
        System.out.println("CurrentDateTimeTool工具上下文：" + toolContext.getContext());
        return "当前日期：" + LocalDate.now();
    }

    @Tool(description = "获取当前时间")
    public String getCurrentTime(ToolContext toolContext) {
        System.out.println("CurrentDateTimeTool工具上下文：" + toolContext.getContext());
        return "当前时间：" + LocalTime.now().withNano(0);
    }
}
