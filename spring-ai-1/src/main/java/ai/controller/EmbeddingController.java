package ai.controller;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@RestController
public class EmbeddingController {

    // 本地测试向量文本库，测试用
    private static final List<String> VECTORS;

    static {
        VECTORS = List.of(
                "我喜欢去旅游",
                "我是一名程序员",
                "我非常喜欢学习",
                "让子弹飞这部电影很值得看",
                "垃圾正确使用可以变废为宝"
        );
    }
    @Autowired
    EmbeddingModel embeddingModel;

    @GetMapping("embedding")
    public Map<String, float[]> embedding(@RequestParam(value = "message", defaultValue = "你是谁") String message) {
        float[] embed = embeddingModel.embed(message);
        return Map.of(message, embed);
    }


    // 相似搜索
    @GetMapping("search")
    public Map<String, Serializable> similaritySearch(@RequestParam(value = "message", defaultValue = "你是谁") String messages) {
        float[] embed = embeddingModel.embed(messages);
        // 最大相似值
        double maxSimilarity = -1;
        int maxIndex = -1;
        for (int i = 0; i < VECTORS.size(); i++) {
            double similarity = continuousSimilarity(embed, embeddingModel.embed(VECTORS.get(i)));
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                maxIndex = i;
            }
        }
        // 返回相似数据
        return Map.of("message", messages, "similarity", maxSimilarity, "similar", VECTORS.get(maxIndex));
    }

    private double continuousSimilarity(float[] embed, float[] vector) {
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < embed.length; i++) {
            dot += embed[i] * vector[i];
            na += embed[i] * embed[i];
            nb += vector[i] * vector[i];
        }
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }
}
