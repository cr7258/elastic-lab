package com.elasticlab;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.InlineScript;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch.core.UpdateByQueryResponse;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import com.elasticlab.pojo.Product;
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
 * @description 删除文档
 * @since 2022/8/3
 */

@SpringBootTest(classes = ElasticsearchApplication.class)
@RunWith(SpringRunner.class)
public class UpdatingTest {
    @Autowired
    private ElasticsearchClient esClient;
    private static final Logger log = LoggerFactory.getLogger(ElasticsearchApplication.class);

    /**
     * 更新单条文档
     *
     * @throws IOException
     */
    @Test
    public void updateById() throws IOException {
        Product product = new Product();
        product.setPrice(7777.77);
        UpdateResponse<Product> response = esClient.update(u -> u
                .index("products")
                .id("sn10005")
                .doc(product), Product.class);
        log.info("Update response: " + response.toString());
    }

    /**
     * 根据查询结果批量更新文档
     *
     * @throws IOException
     */
    @Test
    public void updateByQuery() throws IOException {
        UpdateByQueryResponse response = esClient.updateByQuery(u -> u
                .index("products")
                .script(s -> s
                        .inline(InlineScript.of(i -> i.lang("painless").source("ctx._source.price += 1000"))))
                .query(q -> q
                        .match(m -> m
                                .field("name")
                                .query("T-shirt"))));
        log.info("Update doc count: " + response.total());
    }
}
