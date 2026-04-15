package spring.ai.alibaba.graph;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;

import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

public class GraphTest {

    public static void main(String[] args) throws GraphStateException {
        StateGraph stateGraph = new StateGraph("create-todo-subgraph", () -> {
            return Map.of(
                    "input", new ReplaceStrategy(),
                    "outPut", new ReplaceStrategy(),
                    "append", new AppendStrategy()
            );
        });

        stateGraph.addNode("node1", node_async(state -> {
            Map<String, Object> data = state.data();
            Object input;
            if ((input = data.get("input")).equals("你好")) {
                return Map.of("output", "hello");
            }
            return Map.of("input", input, "output", null);
        }));

        stateGraph.addNode("node2", node_async(state -> {
            Map<String, Object> data = state.data();
            Object input;
            if ((input = data.get("input")).equals("你好")) {
                return Map.of("output", "hello2");
            }
            return Map.of("imput", null, "task_2", "hello");
        }));
        stateGraph.addNode("node3", node_async(state -> {
            return Map.of("111", "1111");
        }));
        stateGraph.addEdge(StateGraph.START, "node1");
        stateGraph.addEdge("node1", "node2");
        stateGraph.addEdge("node2", "node3");
        stateGraph.addEdge("node3", StateGraph.END);
        CompiledGraph compile = stateGraph.compile();
        System.out.println(compile.invoke(Map.of("input", "你好")).get());
    }
}
