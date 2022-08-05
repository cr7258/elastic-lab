package com.elasticlab;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.HistogramBucket;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchResponse;
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
 * @description 聚合查询
 * @since 2022/8/3
 */

@SpringBootTest(classes = ElasticsearchApplication.class)
@RunWith(SpringRunner.class)
public class AggregationsTest {
    @Autowired
    private ElasticsearchClient esClient;
    private static final Logger log = LoggerFactory.getLogger(ElasticsearchApplication.class);

    /**
     * 计算每个价格区间的商品数
     *
     * @throws IOException
     */
    @Test
    public void priceHistogram() throws IOException {
        SearchResponse<Void> response = esClient.search(b -> b
                .index("products")
                .size(0)
                .aggregations("price-histogram", a -> a
                        .histogram(h -> h
                                .field("price")
                                .interval(1000.0) // 每 1000 元作为一个价格区间
                        )
                ), Void.class);
        List<HistogramBucket> buckets = response.aggregations()
                .get("price-histogram")
                .histogram()
                .buckets().array();
        for (HistogramBucket bucket : buckets) {
            log.info("There are " + bucket.docCount() + " products under " + bucket.key());
        }
    }


    /**
     * 计算每种商品的数量
     *
     * @throws IOException
     */
    @Test
    public void productTerm() throws IOException {
        SearchResponse<Void> response = esClient.search(b -> b
                .index("products")
                .size(0)
                .aggregations("product-term", a -> a
                        .terms(t -> t
                                .field("name.keyword"))
                ), Void.class);
        List<StringTermsBucket> buckets = response.aggregations()
                .get("product-term")
                .sterms()
                .buckets().array();
        for(StringTermsBucket bucket: buckets){
            log.info("There are " + bucket.docCount() + " " + bucket.key());
        }
    }
}
