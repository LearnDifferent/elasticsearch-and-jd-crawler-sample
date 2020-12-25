package com.example.escrawlerjd.service;

import com.example.escrawlerjd.pojo.JdGoods;
import com.example.escrawlerjd.utils.HtmlParserUtil;
import com.example.escrawlerjd.utils.PageUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class EsService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 从京东获取数据，并放入 Elasticsearch 中
     *
     * @param keyword
     * @return 成功的话，就返回 true
     * @throws IOException
     */
    public Boolean putJdGoodsIntoEs(String keyword) throws IOException {
        List<JdGoods> goods = HtmlParserUtil.parseJdGoodsByKeyword(keyword);
        BulkRequest bulkRequest = new BulkRequest();
        // timeout 设置为 1 min
        bulkRequest.timeout("1m");
        for (JdGoods g : goods) {
            // index 的名称为 goods
            bulkRequest.add(
                    new IndexRequest("goods")
                            .source(new ObjectMapper().writeValueAsString(g), XContentType.JSON)
            );
        }
        BulkResponse response = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        //成功返回 true
        return !response.hasFailures();
    }

    /**
     * 根据关键词从 ES 中搜索，并进行分页和高亮
     *
     * @param keyword
     * @param currentPage 页数（从 1 开始）
     * @param pageSize    页面容量
     * @return 返回一个 Map，包括所有结果的 List（msg），总记录数（totalCount），总页数（totalPage）
     */
    public Map<String, Object> limitSearch(String keyword, int currentPage, int pageSize) throws IOException {
        if (currentPage < 1) {
            // 让页数从 1 开始
            currentPage = 1;
        }

        // Page 的起始 index
        int start = PageUtil.getFromIndex(currentPage, pageSize);

        // 如果使用了 Vue，记得在前端的标签内加上 v-html="result.desc"
        // 这里的 result 是异步获得的结果的变量名（可以在前端自定义），desc 是需要高亮的字段
        // 可以设置 .requireFieldMatch(false)，不过默认就是为 false，所以这里忽略
        SearchRequest request = new SearchRequest("goods")
                        .source(new SearchSourceBuilder()
                        .query(QueryBuilders.matchQuery("desc", keyword))
                        .timeout(new TimeValue(2, TimeUnit.MINUTES))
                        .highlighter(new HighlightBuilder()
                                .field("desc")
                                .preTags("<span style='color:red'>")
                                .postTags("</span>")
                        )
                        .from(start)
                        .size(pageSize));

        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();

        // 这个 List 用来存放所有的搜索结果
        List<Map<String, Object>> msg = new ArrayList<>();

        for (SearchHit hit : hits) {
            // 将（每条）搜索结果中的 desc 字段高亮
            Map<String, Object> highlightedHit = hitHighlighter(hit, "desc");
            // 添加该条搜索结果
            msg.add(highlightedHit);
        }

        // 获取所有结果的总记录数
        long totalCount = hits.getTotalHits().value;

        // 计算一共有多少页
        int totalPage = PageUtil.getAllPages((int) totalCount, pageSize);

        // 这是需要返回的 Map
        Map<String, Object> map = new HashMap<>();
        map.put("msg", msg);
        map.put("totalCount", totalCount);
        map.put("totalPage", totalPage);

        return map;
    }

    /**
     * 用于将搜索结果转化为高亮后的结果
     *
     * @param hit   搜索的结果
     * @param field 需要高亮的字段
     * @return 高亮后的搜索结果
     */
    private Map<String, Object> hitHighlighter(SearchHit hit, String field) {
        // 获取原来的搜索结果
        Map<String, Object> source = hit.getSourceAsMap();
        // 根据名字（这里是 desc）获取该结果的高亮字段
        HighlightField highlighted = hit.getHighlightFields().get(field);
        // 判断高亮字段是否有内容
        if (highlighted != null) {
            // 获取高亮字段的内容（文字类型的数组）
            // 注意：.getFragments() 和 .fragments() 是一样的
            Text[] texts = highlighted.fragments();

            // 用于获取高亮字段的内容（拼接为字符串）
            StringBuilder sb = new StringBuilder();
            for (Text text : texts) {
                sb.append(text);
            }

            // 将原结果的该字段，替换（覆盖）为高亮字段
            source.put(field, sb.toString());
        }
        return source;
    }
}
