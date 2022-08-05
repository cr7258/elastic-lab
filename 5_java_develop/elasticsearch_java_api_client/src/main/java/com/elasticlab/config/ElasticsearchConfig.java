package com.elasticlab.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author chengzw
 * @description Elasticsearch 配置类
 * @since 2022/8/1
 */

@Data
@Configuration
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticsearchConfig {
    /**
     * 协议
     */
    private String schema;

    /**
     * 集群地址，如果有多个用“,”隔开
     */
    private String address;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;


}
