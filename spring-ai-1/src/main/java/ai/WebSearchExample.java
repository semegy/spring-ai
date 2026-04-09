package ai;

import ai.tool.WebSearchTool;

import java.io.IOException;

public class WebSearchExample {
    
    public static void main(String[] args) {
        WebSearchTool searchTool = new WebSearchTool();
        
        try {
            System.out.println("=== 示例1: 百度搜索 ===");
            WebSearchTool.SearchResult result = searchTool.search(
                "Spring AI 教程", 
                WebSearchTool.SearchEngine.BAIDU, 
                5
            );
            System.out.println(result);
            
            System.out.println("\n=== 示例2: 获取网页内容 ===");
            String content = searchTool.fetchWebPageContent("https://spring.io/projects/spring-ai");
            System.out.println("网页内容前500字符:");
            System.out.println(content.substring(0, Math.min(500, content.length())));
            
            System.out.println("\n=== 示例3: 提取网页信息 ===");
            WebSearchTool.WebPageInfo info = searchTool.extractWebPageInfo("https://spring.io/projects/spring-ai");
            System.out.println(info);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
