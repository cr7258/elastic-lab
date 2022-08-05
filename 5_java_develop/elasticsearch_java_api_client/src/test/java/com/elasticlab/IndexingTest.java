package com.elasticlab;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.JsonpMapper;
import com.elasticlab.pojo.Product;
import jakarta.json.spi.JsonProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chengzw
 * @description 写入数据
 * @since 2022/8/1
 */

@SpringBootTest(classes = ElasticsearchApplication.class)
@RunWith(SpringRunner.class)
public class IndexingTest {
    @Autowired
    private ElasticsearchClient esClient;
    @Autowired
    private ElasticsearchAsyncClient esAsyncClient;
    private static final Logger log = LoggerFactory.getLogger(ElasticsearchApplication.class);

    /**
     * 写入对象
     *
     * @throws IOException
     */
    @Test
    public void indexObject() throws IOException {
        Product product = new Product("sn10001", "computer", 9999.99);
        IndexResponse response = esClient.index(i -> i
                .index("products")
                .id(product.getId())
                .document(product)
        );
        log.info("Indexed Response: " + response);
    }

    /**
     * 异步写入
     *
     * @throws InterruptedException
     */
    @Test
    public void indexObjectAsync() throws InterruptedException {
        Product product = new Product("sn10002", "bike", 300.5);
        esAsyncClient.index(i -> i
                .index("products")
                .id(product.getId())
                .document(product)
        ).whenComplete((response, exception) -> { // 异步写入完成
            if (exception != null) {
                log.error("Failed to index: ", exception);
            } else {
                log.info("Indexed Response: " + response);
            }
        });
        // 主线程等待
        Thread.sleep(5000);
    }

    /**
     * 写入原始 JSON 数据
     *
     * @throws IOException
     */
    @Test
    public void indexWithJson() throws IOException {
        Reader input = new StringReader(
                "{'id': 'sn10003', 'name': 'television', 'price': 5500.5}"
                        .replace('\'', '"'));

        IndexRequest<JsonData> request = IndexRequest.of(i -> i
                .index("products")
                .withJson(input)
        );

        IndexResponse response = esClient.index(request);
        log.info("Indexed Response: " + response);
    }

    /**
     * 批量写入对象
     * @throws IOException
     */
    @Test
    public void indexBulkObject() throws IOException {
        List<Product> products = new ArrayList<>();
        products.add(new Product("sn10004", "T-shirt", 100.5));
        products.add(new Product("sn10005", "phone", 8999.9));
        products.add(new Product("sn10006", "ipad", 6555.5));

        BulkRequest.Builder br = new BulkRequest.Builder();
        for (Product product : products) {
            br.operations(op -> op
                    .index(idx -> idx
                            .index("products")
                            .id(product.getId())
                            .document(product)
                    )
            );
        }

        BulkResponse response = esClient.bulk(br.build());
        if (response.errors()) {
            log.error("Bulk had errors");
            for (BulkResponseItem item: response.items()) {
                if (item.error() != null) {
                    log.error(item.error().reason());
                }
            }
        }else {
            log.info("Indexed Response: " + response);
        }
    }
}
