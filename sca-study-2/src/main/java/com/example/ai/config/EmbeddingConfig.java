package com.example.ai.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * EmbeddingModel 配置（硬编码方式 — 仅供学习参考）。
 * <p>
 * 注：实际项目中不推荐这种方式，因为参数写死在代码里，修改需重新编译。
 * 推荐优先使用 application.yml 兜底 + 调用时按需覆盖。
 */
// @Configuration  // 默认注释掉，避免与 yml 自动装配冲突。取消注释即可生效。
public class EmbeddingConfig {

    @Bean
    public EmbeddingModel embeddingModel(DashScopeApi dashScopeApi) {

        return new DashScopeEmbeddingModel(dashScopeApi);
    }
}
