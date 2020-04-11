package com.es.testapi;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class EmpCrudApi {

    TransportClient transportClient;

    @Before
    public void getClient() throws UnknownHostException {
        Settings elasticsearch = Settings.builder().put("cluster.name", "elasticsearch").build();
        transportClient = new PreBuiltTransportClient(elasticsearch)
                .addTransportAddress(new InetSocketTransportAddress(
                        InetAddress.getByName("192.168.5.129"), 9300));
        System.out.println("连接成功" + transportClient.toString());
    }

    /**
     * 添加索引
     *
     * @throws IOException
     */
    @Test
    public void createEmp() throws IOException {
        IndexResponse indexResponse = transportClient.prepareIndex("company", "employee", "1")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("name", "jack")
                        .field("age", "27")
                        .field("position", "technique")
                        .field("country", "china")
                        .field("join_date", "2020-04-11")
                        .field("salary", "100000")
                        .endObject())
                .get();

    }

    /**
     * 查询员工信息
     */
    @Test
    public void queryDocment() {
        GetResponse response = transportClient.prepareGet("company", "employee", "1").get();
        System.out.println(response.getSourceAsString());
    }

    /**
     * 更新员工信息
     *
     * @throws IOException
     */
    @Test
    public void updateEmp() throws IOException {
        UpdateResponse position = transportClient.prepareUpdate("company", "employee", "1")
                .setDoc(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("position", "technique  manager")
                        .endObject()
                ).get();
        System.out.println(position.getResult());
    }

    @Test
    public void delEmp() {
        DeleteResponse deleteResponse = transportClient.prepareDelete("company", "employee", "1").get();
        System.out.println(deleteResponse.getResult());
    }

    @After
    public void destory() {
        if (transportClient != null) {
            transportClient.close();
        }
    }

}
