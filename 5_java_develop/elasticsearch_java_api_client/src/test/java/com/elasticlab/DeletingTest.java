package com.elasticlab;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteByQueryResponse;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

/**
 * @author chengzw
 * @description 删除数据
 * @since 2022/8/3
 */

@SpringBootTest(classes = ElasticsearchApplication.class)
@RunWith(SpringRunner.class)
public class DeletingTest {
    @Autowired
    private ElasticsearchClient esClient;
    private static final Logger log = LoggerFactory.getLogger(ElasticsearchApplication.class);

    /**
     * 根据文档 id 删除数据
     *
     * @throws IOException
     */
    @Test
    public void deleteById() throws IOException {
        DeleteResponse response = esClient.delete(d -> d
                .index("products")
                .id("sn10005"));
        log.info("Delete response: " + response.toString());
    }

    /**
     * 根据查询结果删除文档
     *
     * @throws IOException
     */
    @Test
    public void deleteByQuery() throws IOException {
        String searchText = "ipad";
        DeleteByQueryResponse response = esClient.deleteByQuery(d ->
                d.index("products")
                        .query(q -> q
                                .match(t -> t
                                        .field("name")
                                        .query(searchText))));
        log.info("Delete doc count: " + response.total());
    }
}
