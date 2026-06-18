package com.example.ai.controller;

import org.springframework.ai.image.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 第三阶段：多模态 — ImageModel 文生图演示。
 * <p>
 * 使用通义万相（或 dall-e-3）模型，将文本描述生成图片。
 */
@RestController
@RequestMapping("/ai/image")
public class ImageModelController {

    private final ImageModel imageModel;

    public ImageModelController(ImageModel imageModel) {
        this.imageModel = imageModel;
    }

    /**
     * 文生图：返回生成的图片 URL
     * GET /ai/image/generate?input=一只可爱的猫咪，卡通风格
     */
    @GetMapping("/generate")
    public String generate(@RequestParam(defaultValue = "一只可爱的猫咪，卡通风格") String input) {
        ImageResponse response = imageModel.call(
                new ImagePrompt(input, ImageOptionsBuilder.builder()
                        .model("wanx-v1")
                        .height(1024)
                        .width(1024)
                        .build())
        );
        return response.getResult().getOutput().getUrl();
    }

    /**
     * 文生图：返回完整响应信息
     * GET /ai/image/response?input=中国山水画风格的高山流水
     */
    @GetMapping("/response")
    public String response(@RequestParam(defaultValue = "中国山水画风格的高山流水") String input) {
        ImageResponse response = imageModel.call(
                new ImagePrompt(input, ImageOptionsBuilder.builder()
                        .model("wanx-v1")
                        .height(1024)
                        .width(1024)
                        .build())
        );
        ImageGeneration result = response.getResult();
        return """
                图片 URL：%s
                描述：%s
                """.formatted(
                result.getOutput().getUrl(),
                result.getOutput().getB64Json()
        );
    }
}
