package spring.ai.alibaba.graph;

import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.action.AsyncNodeActionWithConfig;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.StateSnapshot;
import reactor.core.Disposable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.alibaba.cloud.ai.graph.StateGraph.END;

public class ReviewGraphService {

    private final CompiledGraph compiledGraph;

    KeyStrategyFactory keyStrategyFactory = new KeyStrategyFactoryBuilder()
            .defaultStrategy(KeyStrategy.REPLACE)
            .addStrategy("ai_generator")
            .addStrategy("user_input1", KeyStrategy.APPEND)
            .addStrategy("human_reviewer")
            .addStrategy("success")
            .addStrategy("reject")
            .build();

    public ReviewGraphService() throws GraphStateException {

        var saver = new MemorySaver();
        SaverConfig build = SaverConfig.builder()
                .register(saver)
                .build();
        StateGraph stateGraph = new StateGraph(keyStrategyFactory);
        // 状态图添加执行节点
        stateGraph.addNode("ai_generator", this::aiGenerateContent)
                .addNode("user_input1", this::userInput)
                .addNode("human_reviewer", new HumanReviewer())
                .addNode("success", this::success)
                .addNode("reject", this::reject);


        // 添加执行逻辑节点
        stateGraph.addEdge(StateGraph.START, "ai_generator");
        stateGraph.addEdge("ai_generator", "human_reviewer");
        stateGraph.addConditionalEdges("human_reviewer", new AsyncEdgeAction() {
                    @Override
                    public CompletableFuture<String> apply(OverAllState state) {
                        if (state.value("user_input").equals("waiting")) {
                            return CompletableFuture.supplyAsync(() -> "waiting");
                        } else if (state.value("user_input").equals("reject")) {
                            return CompletableFuture.supplyAsync(() -> "reject");
                        } else {
                            return CompletableFuture.supplyAsync(() -> "success");
                        }
                    }
                },
                Map.of("waiting", "user_input1", "success", "success", "reject", "reject")
        );
        stateGraph.addEdge("user_input1", "human_reviewer");
        stateGraph.addEdge("reject", END);
        stateGraph.addEdge("success", END);


        this.compiledGraph = stateGraph.compile(CompileConfig.builder()
                .saverConfig(build).interruptBefore("human_reviewer").build());

    }

    private CompletableFuture<Map<String, Object>> userInput(OverAllState overAllState, RunnableConfig runnableConfig) {
        String value = overAllState.value("user_input", "waiting");
        return CompletableFuture.supplyAsync(() -> Map.of("user_input", value));
    }

    private CompletableFuture<Map<String, Object>> reject(OverAllState overAllState, RunnableConfig runnableConfig) {
        System.out.println(">>> [结束节点] 审核驳回");
        return CompletableFuture.completedFuture(overAllState.data());
    }

    private CompletableFuture<Map<String, Object>> success(OverAllState overAllState, RunnableConfig runnableConfig) {
        System.out.println(">>> [结束节点] 审核成功");
        return CompletableFuture.completedFuture(overAllState.data());
    }

    class HumanReviewer implements AsyncNodeActionWithConfig {
        @Override
        public CompletableFuture<Map<String, Object>> apply(OverAllState state, RunnableConfig config) {
            if ("approve".equals(state.value("user_input").get())) {
                return CompletableFuture.supplyAsync((() -> {
                    System.out.println(">>> [人工节点] 同意发表...");
                    return Map.of("human_reviewer", "success");
                }));
            } else if ("waiting".equals(state.value("user_input").get())) {
                return CompletableFuture.supplyAsync((() -> {
                    System.out.println(">>> [人工节点] 审核中...");
                    return Map.of("user_input", "waiting");
                }));
            } else {
                return CompletableFuture.supplyAsync((() -> {
                    System.out.println(">>> [人工节点] 拒绝发表...");
                    return Map.of("human_reviewer", "reject");
                }));
            }
        }
    }

    private CompletableFuture<Map<String, Object>> aiGenerateContent(OverAllState overAllState, RunnableConfig runnableConfig) {

        return CompletableFuture.supplyAsync(() -> {
            String content = """
                    
                    **唐·孟浩然**
                    
                    春眠不觉晓，
                    处处闻啼鸟。
                    夜来风雨声，
                    花落知多少。
                    """;
            return Map.of("ai_generator", content, "user_input", "waiting");
        });
    }

    public Disposable startProcess(String userInput) {
        RunnableConfig runnableConfig = RunnableConfig.builder().threadId("thread-1").build();
        Map<String, Object> context = new HashMap<>();
        context.put("user_input", userInput);
        // 从初始状态开始执行工作流
        return this.compiledGraph.stream(context, runnableConfig).doOnNext(event -> {
                    System.out.println("节点输出: " + event);
                })
                .doOnError(error -> System.err.println("流错误: " + error.getMessage()))
                .doOnComplete(() -> System.out.println("流完成")).last().map(output ->
                        output.state().value("human_reviewer", String.class).orElse("完成")
                ).subscribe();
    }


    public static void main(String[] args) throws Exception {
        ReviewGraphService reviewGraphService = new ReviewGraphService();
        Disposable overAllState = reviewGraphService.startProcess(null);
        RunnableConfig runnableConfig = RunnableConfig.builder().threadId("thread-1").build();
        StateSnapshot stateSnapshot = reviewGraphService.compiledGraph.stateOf(runnableConfig).get();
        String nextNode = stateSnapshot.next();
        // 如果流程已完成（无下一节点或到达 END），则当作新对话处理
        if (nextNode == null || nextNode.isEmpty() || END.equals(nextNode)) {
            reviewGraphService.startProcess(null);
        } else {
            resumeProcess(nextNode, runnableConfig, reviewGraphService);
        }

    }

    private static void resumeProcess(String nextNode, RunnableConfig runnableConfig, ReviewGraphService reviewGraphService) throws Exception {
        // 从中断点继续执行工作流
        System.out.println("从中断点继续执行工作流" + nextNode);
        runnableConfig.context().put(RunnableConfig.STATE_UPDATE_METADATA_KEY, Boolean.TRUE);
        reviewGraphService.compiledGraph.updateState(runnableConfig, Map.of("user_input", "approve"), null);

        reviewGraphService.compiledGraph.stream(null, runnableConfig.withResume())
                .doOnNext(nodeOutput -> {
                    System.out.println("节点输出" + nodeOutput);
                })
                .doOnComplete(() -> System.out.println("完成"))
                .doOnError(throwable -> throwable.printStackTrace())
                .last()
                .subscribe();
    }

}
