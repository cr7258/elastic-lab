package com.elasticlab.config;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chengzw
 * @description 连接 Elasticsearch
 * @since 2022/8/1
 */

@Configuration
public class ElasticsearchClientConfig {
    private final ElasticsearchConfig elasticsearchConfig;

    public ElasticsearchClientConfig(ElasticsearchConfig elasticsearchConfig) {
        this.elasticsearchConfig = elasticsearchConfig;
    }

    @Bean
    public RestClient restClient() throws Exception {

        // 拆分地址
        List<HttpHost> hostLists = new ArrayList<>();
        String[] hostArray = elasticsearchConfig.getAddress().split(",");
        for (String temp : hostArray) {
            String host = temp.split(":")[0];
            String port = temp.split(":")[1];
            hostLists.add(new HttpHost(host, Integer.parseInt(port), elasticsearchConfig.getSchema()));
        }

        // 转换成 HttpHost 数组
        HttpHost[] httpHost = hostLists.toArray(new HttpHost[]{});

        // 设置用户名和密码
        final CredentialsProvider credentialsProvider =
                new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(elasticsearchConfig.getUsername(), elasticsearchConfig.getPassword()));

        // 我们部署的 Elasticsearch 使用的是自签名的 CA，需要设置信任的 CA 证书
        String filePath = new File("src/main/resources/http_ca.crt").getAbsolutePath();
        Path caCertificatePath = Paths.get(filePath);

        CertificateFactory factory = null;
        factory = CertificateFactory.getInstance("X.509");
        InputStream is = Files.newInputStream(caCertificatePath);
        Certificate trustedCa;
        trustedCa = factory.generateCertificate(is);

        KeyStore trustStore = KeyStore.getInstance("pkcs12");
        trustStore.load(null, null);
        trustStore.setCertificateEntry("ca", trustedCa);
        SSLContextBuilder sslContextBuilder = SSLContexts.custom()
                .loadTrustMaterial(trustStore, null);
        final SSLContext sslContext = sslContextBuilder.build();

        // 构造 HTTPS 客户端请求访问
        RestClientBuilder builder = RestClient.builder(httpHost)
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                        .setSSLContext(sslContext)
                        .setDefaultCredentialsProvider(credentialsProvider)
                        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)   // 不验证 SSL 证书主机名
                        .setMaxConnTotal(elasticsearchConfig.getMaxConnectNum())  // 异步连接数配置
                        .setMaxConnPerRoute(elasticsearchConfig.getMaxConnectPerRoute()));


        // 异步连接延时配置
        builder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(elasticsearchConfig.getConnectTimeout());
            requestConfigBuilder.setSocketTimeout(elasticsearchConfig.getSocketTimeout());
            requestConfigBuilder.setConnectionRequestTimeout(elasticsearchConfig.getConnectionRequestTimeout());
            return requestConfigBuilder;
        });

        return builder.build();
    }

    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        return new RestClientTransport(
                restClient, new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }

    @Bean
    public ElasticsearchAsyncClient elasticsearchAsyncClient(ElasticsearchTransport transport) {
        return new ElasticsearchAsyncClient(transport);
    }
}
