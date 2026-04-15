package ai.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WebSearchTool implements ModelTool {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private static final String GOOGLE_SEARCH_URL = "https://www.google.com/search?q=";
    private static final String BING_SEARCH_URL = "https://www.bing.com/search?q=";
    private static final String BAIDU_SEARCH_URL = "https://www.baidu.com/s?wd=";

    public WebSearchTool() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public SearchResult search(String query, SearchEngine engine, int maxResults) throws IOException {
        String searchUrl = buildSearchUrl(query, engine);
        List<SearchResultItem> results = new ArrayList<>();

        try {
            String htmlContent = fetchPage(searchUrl);
            Document doc = Jsoup.parse(htmlContent);

            switch (engine) {
                case BAIDU:
                    results = parseBaiduResults(doc);
                    break;
                case BING:
                    results = parseBingResults(doc);
                    break;
                default:
                    results = parseGenericResults(doc);
            }

            if (results.size() > maxResults) {
                results = results.subList(0, maxResults);
            }

        } catch (IOException e) {
            throw new IOException("搜索失败: " + e.getMessage(), e);
        }

        return new SearchResult(query, engine.name(), results);
    }

    public String fetchWebPageContent(String url) throws IOException {
        String html = fetchPage(url);
        Document doc = Jsoup.parse(html);

        doc.select("script, style, nav, footer, header, noscript").remove();

        Element body = doc.body();
        if (body != null) {
            return body.text();
        }
        return "";
    }

    public WebPageInfo extractWebPageInfo(String url) throws IOException {
        String html = fetchPage(url);
        Document doc = Jsoup.parse(html, url);

        String title = doc.title();

        String description = "";
        Element metaDesc = doc.selectFirst("meta[name=description]");
        if (metaDesc != null) {
            description = metaDesc.attr("content");
        }

        if (description.isEmpty()) {
            Element body = doc.body();
            if (body != null) {
                description = body.text();
                if (description.length() > 500) {
                    description = description.substring(0, 500) + "...";
                }
            }
        }

        List<String> links = new ArrayList<>();
        Elements elements = doc.select("a[href]");
        for (Element link : elements) {
            String href = link.absUrl("href");
            if (!href.isEmpty()) {
                links.add(href);
            }
        }

        return new WebPageInfo(url, title, description, links);
    }

    private String fetchPage(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败，响应码: " + response.code());
            }
            String body = response.body() != null ? response.body().string() : "";
            if (body.isEmpty()) {
                throw new IOException("响应内容为空");
            }
            return body;
        }
    }

    private String buildSearchUrl(String query, SearchEngine engine) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            switch (engine) {
                case BAIDU:
                    return BAIDU_SEARCH_URL + encodedQuery;
                case BING:
                    return BING_SEARCH_URL + encodedQuery;
                case GOOGLE:
                default:
                    return GOOGLE_SEARCH_URL + encodedQuery;
            }
        } catch (Exception e) {
            throw new RuntimeException("查询编码失败", e);
        }
    }

    private List<SearchResultItem> parseBaiduResults(Document doc) {
        List<SearchResultItem> results = new ArrayList<>();
        Elements elements = doc.select(".result, .c-container");

        for (Element element : elements) {
            Element titleElem = element.selectFirst("h3, .t");
            Element linkElem = element.selectFirst("a");
            Element descElem = element.selectFirst(".c-abstract, .abstract");

            if (titleElem != null && linkElem != null) {
                String title = titleElem.text();
                String url = linkElem.absUrl("href");
                String description = descElem != null ? descElem.text() : "";

                results.add(new SearchResultItem(title, url, description));
            }
        }

        return results;
    }

    private List<SearchResultItem> parseBingResults(Document doc) {
        List<SearchResultItem> results = new ArrayList<>();
        Elements elements = doc.select(".b_algo");

        for (Element element : elements) {
            Element titleElem = element.selectFirst("h2 a");
            Element descElem = element.selectFirst(".b_caption p, .b_snippet");

            if (titleElem != null) {
                String title = titleElem.text();
                String url = titleElem.absUrl("href");
                String description = descElem != null ? descElem.text() : "";

                results.add(new SearchResultItem(title, url, description));
            }
        }

        return results;
    }

    private List<SearchResultItem> parseGenericResults(Document doc) {
        List<SearchResultItem> results = new ArrayList<>();
        Elements elements = doc.select("div.g, div.search-result, .result");

        for (Element element : elements) {
            Element titleElem = element.selectFirst("h3, h2, .title");
            Element linkElem = element.selectFirst("a");
            Element descElem = element.selectFirst(".st, .snippet, .description");

            if (titleElem != null && linkElem != null) {
                String title = titleElem.text();
                String url = linkElem.absUrl("href");
                String description = descElem != null ? descElem.text() : "";

                results.add(new SearchResultItem(title, url, description));
            }
        }

        if (results.isEmpty()) {
            Elements allLinks = doc.select("a[href]");
            int count = 0;
            for (Element link : allLinks) {
                if (count >= 10) break;

                String href = link.absUrl("href");
                String text = link.text();

                if (!href.isEmpty() && !text.isEmpty() &&
                        (href.startsWith("http://") || href.startsWith("https://"))) {
                    results.add(new SearchResultItem(text, href, ""));
                    count++;
                }
            }
        }

        return results;
    }

    public enum SearchEngine {
        GOOGLE, BING, BAIDU
    }

    public static class SearchResult {
        private String query;
        private String engine;
        private List<SearchResultItem> items;

        public SearchResult(String query, String engine, List<SearchResultItem> items) {
            this.query = query;
            this.engine = engine;
            this.items = items;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String getEngine() {
            return engine;
        }

        public void setEngine(String engine) {
            this.engine = engine;
        }

        public List<SearchResultItem> getItems() {
            return items;
        }

        public void setItems(List<SearchResultItem> items) {
            this.items = items;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("搜索结果 [引擎: ").append(engine).append(", 查询: ").append(query).append("]\n");
            sb.append("找到 ").append(items.size()).append(" 条结果:\n\n");

            for (int i = 0; i < items.size(); i++) {
                sb.append(i + 1).append(". ").append(items.get(i).toString()).append("\n\n");
            }

            return sb.toString();
        }
    }

    public static class SearchResultItem {
        private String title;
        private String url;
        private String description;

        public SearchResultItem(String title, String url, String description) {
            this.title = title;
            this.url = url;
            this.description = description;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return "标题: " + title + "\n链接: " + url + "\n摘要: " + description;
        }
    }

    public static class WebPageInfo {
        private String url;
        private String title;
        private String description;
        private List<String> links;

        public WebPageInfo(String url, String title, String description, List<String> links) {
            this.url = url;
            this.title = title;
            this.description = description;
            this.links = links;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getLinks() {
            return links;
        }

        public void setLinks(List<String> links) {
            this.links = links;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("网页信息:\n");
            sb.append("URL: ").append(url).append("\n");
            sb.append("标题: ").append(title).append("\n");
            sb.append("描述: ").append(description).append("\n");
            sb.append("链接数量: ").append(links.size()).append("\n");
            return sb.toString();
        }
    }
}
