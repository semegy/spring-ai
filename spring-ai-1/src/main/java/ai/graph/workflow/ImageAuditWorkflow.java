package ai.graph.workflow;

import ai.po.ImageAuditResult;
import ai.utils.mock.MockData;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.StateSnapshot;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static ai.utils.mock.MockDataWrapper.MOCK;
import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;

/**
 * 图像审核工作流
 * 基于 Spring AI Alibaba Graph 构建
 */
@Component
public class ImageAuditWorkflow {

    private ChatClient chatClient;

    private StateGraph stateGraph;
    private CompiledGraph compiledGraph;
    String judgePrompt = """
            [系统角色]
            你是由头部互联网平台内容安全团队训练的AI图像审核专家，负责依据《网络信息内容生态治理规定》《互联网信息服务算法推荐管理规定》及平台社区规范，对输入图像进行多维度合规性评估。
            
            [审核维度与定义]
            1. 色情低俗：暴露敏感部位、性暗示姿势/服饰、挑逗性表情、软色情构图、恋物癖倾向
            2. 暴力恐怖：血腥伤口、武器、虐待/斗殴/自残、恐怖惊悚元素、精神污染符号
            3. 政治敏感：国家领导人/敏感历史人物、违禁旗帜/标语/地图、煽动性言论、地域歧视
            4. 违禁违法：毒品/赌博/走私物品、危险行为演示、违禁品展示、非法集会暗示
            5. 广告营销：二维码/联系方式/促销文案/品牌Logo/引流话术/虚假宣传
            6. 侵权版权：直接搬运受版权保护的原画/摄影/知名IP未授权商用、水印残留
            7. 公序良俗：低俗恶搞、违背社会公德、不良价值观导向、未成年人不适内容
            
            [输出格式]
            必须严格以JSON格式返回，包含以下字段：
            {
              "overallStatus": "pass/pass/review/limiting/reject",
              "overallScore": number,
              "dimensions": {
                "pornographyVulgarity": {"score": number, "reason": "string"},
                "violenceTerror": {"score": number, "reason": "string"},
                "politicalSensitivity": {"score": number, "reason": "string"},
                "illegalContraband": {"score": number, "reason": "string"},
                "adMarketing": {"score": number, "reason": "string"},
                "copyrightIp": {"score": number, "reason": "string"},
                "publicMorals": {"score": number, "reason": "string"}
              },
              "riskTags": ["array of tags if any"],
              "recommendation": "string"
            }
            
            [评分规则]
            - 各个审核维度，与整体均采用0-100分制，分数代表“违规置信度”（0=完全安全，100=高度违规）
            
            [违规规则]
            overallStatus：
              1: AI生成特征（如乱码文字、结构轻微失真、非写实光影）不视为违规，但需标注
              2：dimensions中任意元素的分数≥40 overallStatus=limiting，后续规则不需要再走
              3：overallScore≤15为安全 overallStatus=pass
              4：overallScore在16-30之间为低风险，overallStatus=pass
              5：overallScore在31-39之间为中风险，overallStatus=reject
              6：overallScore在40-70之间为中高风险，overallStatus=limiting
              7：overallScore≥71为中高风险，overallStatus=reject
            
            [注意事项]
            - 严格基于视觉事实，不主观臆断或过度解读
            - 二次元/动漫/游戏风格按成人向标准评估，不因画风降低尺度
            - 若图片含多主体或复杂背景，按最高风险维度判定
            - 输出仅含JSON，无额外解释
            """;


    private MemorySaver memorySaver = new MemorySaver();

    // 定义状态键
    private static final String IMAGE_URL = "image_url";
    private static final String VISUAL_ANALYSIS = "visual_analysis";
    private static final String JUDGMENT_NEED_HUMAN_REVIEW = "judgment_need_human_review";
    private static final String WAIT_REVIEW = "review";
    private static final String LIMIT = "limiting";
    private static final String REJECT = "reject";
    private static final String PASS = "pass";
    private static final String OUTPUT = "output";
    private static final String REVIEW_RESULT = "review_result";

    @Autowired
    public ImageAuditWorkflow(@Qualifier("qianwen") ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultOptions(DashScopeChatOptions.builder().model("qwen-vl-max").withMultiModel(true).build())
                .build();
    }

    @PostConstruct
    public void initGraph() throws GraphStateException {

        KeyStrategyFactory keyStrategyFactory = new KeyStrategyFactoryBuilder()
                .addStrategy(IMAGE_URL)
                .addStrategy(REVIEW_RESULT)
                .addStrategy(OUTPUT)
                .build();
        // 1. 初始化状态图，定义状态 Schema
        // 这里使用简单的 Map 作为状态存储
        this.stateGraph = new StateGraph(keyStrategyFactory);
        this.stateGraph.addNode(VISUAL_ANALYSIS, this::visualAnalysisNode);
        this.stateGraph.addNode(JUDGMENT_NEED_HUMAN_REVIEW, this::complianceJudgeNode);
        this.stateGraph.addNode(WAIT_REVIEW, this::waitReviewNode);
        this.stateGraph.addNode(REJECT, this::reject);
        this.stateGraph.addNode(LIMIT, this::limit);
        this.stateGraph.addNode(PASS, this::pass);
        this.stateGraph.addNode(OUTPUT, this::output);

        // 3. 定义边 (流程控制)
        this.stateGraph.addEdge(StateGraph.START, VISUAL_ANALYSIS);
        this.stateGraph.addEdge(VISUAL_ANALYSIS, JUDGMENT_NEED_HUMAN_REVIEW);
        // 人工审核
        this.stateGraph.addConditionalEdges(JUDGMENT_NEED_HUMAN_REVIEW, edge_async(state -> {
            Object value = state.value(REVIEW_RESULT).get();
            if (value.equals(WAIT_REVIEW)) return WAIT_REVIEW;
            else if (value.equals(REJECT)) return REJECT;
            else if (value.equals(LIMIT)) return LIMIT;
            else return PASS;
        }), Map.of(REJECT, REJECT, WAIT_REVIEW, WAIT_REVIEW, LIMIT, LIMIT, PASS, PASS));
        this.stateGraph.addConditionalEdges(WAIT_REVIEW, edge_async(state -> {
            Object value = state.value(REVIEW_RESULT).get();
            if (value.equals(PASS)) return PASS;
            if (value.equals(LIMIT)) return LIMIT;
            else return REJECT;
        }), Map.of(REJECT, REJECT, LIMIT, LIMIT, PASS, PASS));
        this.stateGraph.addEdge(REJECT, OUTPUT);
        this.stateGraph.addEdge(PASS, OUTPUT);
        this.stateGraph.addEdge(LIMIT, OUTPUT);
        // 规则判定 -> 出口
        this.stateGraph.addEdge(OUTPUT, StateGraph.END);
        // 4. 编译图
        this.compiledGraph = stateGraph.compile((CompileConfig.builder()
                .saverConfig(SaverConfig.builder().register(memorySaver).build())
                .interruptBefore(WAIT_REVIEW)
                .build()));

    }

    private CompletableFuture<Map<String, Object>> pass(OverAllState state, RunnableConfig runnableConfig) {
        System.out.println("合规：放行");
        return CompletableFuture.completedFuture(Map.of(REVIEW_RESULT, PASS));
    }

    private CompletableFuture<Map<String, Object>> output(OverAllState state, RunnableConfig runnableConfig) {
        ImageAuditResult imageAuditResult = (ImageAuditResult) state.value(OUTPUT).get();
        System.out.println("审核结束：" + state.value(OUTPUT));
        imageAuditResult.setOverallStatus(state.value(REVIEW_RESULT).get().toString());
        return CompletableFuture.completedFuture(Map.of(OUTPUT, state.value(OUTPUT)));
    }

    private CompletableFuture<Map<String, Object>> limit(OverAllState state, RunnableConfig runnableConfig) {
        System.out.println("涉嫌违规：限流");
        return CompletableFuture.completedFuture(Map.of(REVIEW_RESULT, LIMIT));
    }

    private CompletableFuture<Map<String, Object>> reject(OverAllState state, RunnableConfig runnableConfig) {
        System.out.println("判定违规：下架");
        return CompletableFuture.completedFuture(Map.of(REVIEW_RESULT, REJECT));
    }

    private CompletableFuture<Map<String, Object>> waitReviewNode(OverAllState state, RunnableConfig runnableConfig) {
        if (state.value(REVIEW_RESULT).get().equals(PASS)) {
            return CompletableFuture.completedFuture(Map.of(REVIEW_RESULT, PASS));
        } else if (state.value(REVIEW_RESULT).get().equals(LIMIT)) {
            return CompletableFuture.completedFuture(Map.of(REVIEW_RESULT, LIMIT));
        } else {
            return CompletableFuture.completedFuture(Map.of(REVIEW_RESULT, REJECT));
        }

    }

    private CompletableFuture<Map<String, Object>> complianceJudgeNode(OverAllState state) {
        return CompletableFuture.supplyAsync(
                () -> {
                    switch (((ImageAuditResult) state.value(OUTPUT).get()).getOverallStatus()) {
                        case WAIT_REVIEW:
                            return Map.of(REVIEW_RESULT, WAIT_REVIEW);
                        case REJECT:
                            return Map.of(REVIEW_RESULT, REJECT);
                        case LIMIT:
                            return Map.of(REVIEW_RESULT, LIMIT);
                        default:
                            return Map.of(REVIEW_RESULT, PASS);
                    }
                });
    }

    private CompletableFuture<Map<String, Object>> visualAnalysisNode(OverAllState state) {
        Resource resource = (Resource) state.value(IMAGE_URL).get();
        // 构建提示词：注入你提供的 [系统角色] 和 [评分规则]
        Prompt prompt = new Prompt(new SystemMessage(judgePrompt), UserMessage.builder()
                .text("帮我审核这张图片")
                .media(Media.builder().mimeType(MimeType.valueOf("image/*")).data(resource).build()).build()
        );

        ImageAuditResult response = MOCK
                .wrapper(() -> chatClient.prompt(prompt).call().entity(ImageAuditResult.class))
                .mock(MockData.IMAGE_AUDIT_PASS);
        return CompletableFuture.completedFuture(Map.of(OUTPUT, response));

    }

    public Mono<Object> start(String workFlowId, Resource resource) {
        Map<String, Object> imageUrl = Map.of(IMAGE_URL, resource);
        return compiledGraph.stream(imageUrl, RunnableConfig.builder().threadId(workFlowId).build()).last().map(o -> o.state().value(OUTPUT).get());
    }


    public Mono<Object> reStart(String review, String workFlowId) throws Exception {
        RunnableConfig build = RunnableConfig.builder().threadId(workFlowId).build();
        StateSnapshot stateSnapshot = compiledGraph.stateOf(build).orElse(null);
        if (stateSnapshot != null) {
            compiledGraph.updateState(build, Map.of(REVIEW_RESULT, review), null);
            return compiledGraph.stream(null, build.withResume()).last().map(o -> o.state().value(OUTPUT).get());
        }
        return null;
    }
}
