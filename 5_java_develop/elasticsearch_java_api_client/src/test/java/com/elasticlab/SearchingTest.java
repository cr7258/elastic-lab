package com.elasticlab;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import com.elasticlab.pojo.Product;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

/**
 * @author chengzw
 * @description 查询数据
 * @since 2022/8/1
 */

@SpringBootTest(classes = ElasticsearchApplication.class)
@RunWith(SpringRunner.class)
public class SearchingTest {
    @Autowired
    private ElasticsearchClient esClient;
    private static final Logger log = LoggerFactory.getLogger(ElasticsearchApplication.class);

    /**
     * 根据 id 查找 product 对象
     * @throws IOException
     */
    @Test
    public void getProductById() throws IOException {
        GetResponse<Product> response = esClient.get(g -> g
                        .index("products")
                        .id("sn10001"),
                Product.class);
        if(response.found()) {
            Product product = response.source();
            log.info("Product name: " + product.getName());
        }else {
            log.info("Product not found");
        }
    }

    /**
     * 根据 id 查找原始 JSON
     * @throws IOException
     */
    @Test
    public void getJsonById() throws IOException {
        GetResponse<ObjectNode> response = esClient.get(g -> g
                        .index("products")
                        .id("sn10001"),
                ObjectNode.class
        );

        if (response.found()) {
            ObjectNode json = response.source();
            String name = json.get("name").asText();
            log.info("Product name " + name);
        } else {
            log.info("Product not found");
        }
    }

    /**
     * match 查询搜索文档
     * @throws IOException
     */
    @Test
    public void search() throws IOException {
        String searchText = "computer";
        SearchResponse<Product> response = esClient.search(s -> s
                        .index("products")
                        .query(q -> q
                                .match(t -> t
                                        .field("name")
                                        .query(searchText)
                                )
                        )
                , Product.class);
        TotalHits total = response.hits().total();
        boolean isExactResult = total.relation() == TotalHitsRelation.Eq;
        if (isExactResult) {
            log.info("There are " + total.value() + " results");
        } else {
            log.info("There are more than " + total.value() + " results");
        }

        List<Hit<Product>> hits = response.hits().hits();
        for (Hit<Product> hit: hits) {
            Product product = hit.source();
            log.info("Found product " + product.getId() + ", score " + hit.score());
        }
    }
}
