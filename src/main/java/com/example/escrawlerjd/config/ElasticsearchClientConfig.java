package com.example.escrawlerjd.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchClientConfig {

    @Value("${elasticsearch.host}")
    private String host;

    @Value("${elasticsearch.port}")
    private Integer port;

    @Value("${elasticsearch.isHttps}")
    private Boolean isHttps;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        RestClientBuilder builder = RestClient.builder(
                new HttpHost(host, port, isHttps ? "https" : "http"));
        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;
    }
}
