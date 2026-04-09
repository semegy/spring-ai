package ai.controller;

import ai.tool.WebSearchTool;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
public class SearchController {

    private final WebSearchTool webSearchTool;

    public SearchController() {
        this.webSearchTool = new WebSearchTool();
    }

    @GetMapping("/web")
    public Map<String, Object> searchWeb(
            @RequestParam String query,
            @RequestParam(defaultValue = "BAIDU") String engine,
            @RequestParam(defaultValue = "10") int maxResults) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            WebSearchTool.SearchEngine searchEngine = WebSearchTool.SearchEngine.valueOf(engine.toUpperCase());
            WebSearchTool.SearchResult result = webSearchTool.search(query, searchEngine, maxResults);
            
            response.put("success", true);
            response.put("data", result);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    @GetMapping("/page")
    public Map<String, Object> getPageContent(@RequestParam String url) {
        Map<String, Object> response = new HashMap<>();
        try {
            String content = webSearchTool.fetchWebPageContent(url);
            
            response.put("success", true);
            response.put("url", url);
            response.put("content", content);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    @GetMapping("/info")
    public Map<String, Object> getPageInfo(@RequestParam String url) {
        Map<String, Object> response = new HashMap<>();
        try {
            WebSearchTool.WebPageInfo info = webSearchTool.extractWebPageInfo(url);
            
            response.put("success", true);
            response.put("data", info);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
}
