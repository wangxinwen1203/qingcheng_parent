package com.qingcheng.service.impl;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

public class RestClientFactory {

    public static RestHighLevelClient getRestHighLevelClient(String hostName,int port){
        HttpHost http=new HttpHost(hostName,port,"http");
        RestClientBuilder builder= RestClient.builder(http);//rest构建器
        RestHighLevelClient restHighLevelClient=new RestHighLevelClient(builder);//高级客户端对象
        return restHighLevelClient;
    }
}
