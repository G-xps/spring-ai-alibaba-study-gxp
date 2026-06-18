package com.example.ai.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

/**
 * 自定义 Advisor — 文档溯源型。
 * <p>
 * 在 RAG 检索后，将检索到的文档来源和片段信息注入到 Prompt 上下文中，
 * 让 AI 在回答时能标注引用来源。
 * <p>
 * 效果：回答末尾自动追加「参考资料：xxx.txt」
 */
public class SourceTracingAdvisor implements CallAroundAdvisor {

    private static final Logger log = LoggerFactory.getLogger(SourceTracingAdvisor.class);

    private final VectorStore vectorStore;

    public SourceTracingAdvisor(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public String getName() {
        return "SourceTracingAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        // 1. 手动检索知识库
        String query = advisedRequest.userText();
//        log.info("SourceTracingAdvisor 检索：{}", query);

        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(3)
                        .similarityThreshold(0.5)
                        .build());

        if (!docs.isEmpty()) {
            // 2. 提取引用来源
            StringBuilder sourceInfo = new StringBuilder("\n\n【参考资料】\n");
            for (int i = 0; i < docs.size(); i++) {
                Document doc = docs.get(i);
                String source = (String) doc.getMetadata().getOrDefault("source", "未知来源");
                sourceInfo.append("[%s] 来自《%s》\n".formatted(i + 1, source));
            }

            // 3. 将来源信息追加到用户问题末尾，让模型在回答时标注出处
            advisedRequest = AdvisedRequest
                    .from(advisedRequest)
                    .userText(advisedRequest.userText() + """

                            ---
                            请你在回答的最后，单独列出本次回答参考了哪些资料，格式如下：
                            【参考资料】
                            [1] 来自《xxx.txt》
                            [2] 来自《xxx.txt》
                            """ + sourceInfo.toString())
                    .build();
        }

        // 4. 继续执行后续 Advisor
        return chain.nextAroundCall(advisedRequest);
    }
}
