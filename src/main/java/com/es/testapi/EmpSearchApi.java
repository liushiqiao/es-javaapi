package com.es.testapi;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


public class EmpSearchApi {

    TransportClient transportClient;

    @Before
    public void getClient() throws UnknownHostException {
        Settings elasticsearch = Settings.builder().put("cluster.name", "elasticsearch").build();
        transportClient = new PreBuiltTransportClient(elasticsearch)
                .addTransportAddress(new InetSocketTransportAddress(
                        InetAddress.getByName("192.168.5.129"), 9300));
        System.out.println("连接成功" + transportClient.toString());
    }

    @Test
    public void initData() throws IOException {

        String positions[] = {"software", "manager", "finance"};
        String countrys[] = {"china", "japanese", "usa", "uk", "india", "russis"};
        IndexRequestBuilder indexRequestBuilder = transportClient.prepareIndex("company", "employee");
        System.out.println("es初始化数据");
        for (int i = 2; i <= 1000; i++) {
            indexRequestBuilder.setId("" + i).setSource(
                    XContentFactory.jsonBuilder().startObject()
                            .field("name", UUID.randomUUID().toString().substring(0, 5) + "_" + i)
                            .field("age", 20 + (int) (Math.random() * 75))
                            .field("position", "technique" + " " + positions[i % 3])
                            .field("country", countrys[i % 6])
                            .field("join_date", randomDate("2017-05-04", "2020-04-11"))
                            .field("salary", 10000 + (int) (Math.random() * 10000))
                            .endObject()
            ).get();
        }
        System.out.println("es初始化数据结束");
    }

    @Test
    public void EmpCompxSearch() {
        SearchRequestBuilder requestBuilder = transportClient.prepareSearch("company");
        requestBuilder.setExplain(false);
        requestBuilder.setTypes("employee");

        //分页
        requestBuilder.setFrom(0).setSize(60);

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        SearchResponse searchResponse = requestBuilder.setQuery(QueryBuilders.matchQuery("position", "technique"))
                .setPostFilter(QueryBuilders.rangeQuery("age").from(30).to(40))
                .setSize(1).setFrom(0).get();
        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            System.out.println(hit.getSourceAsString());
        }

    }


    @After
    public void destory() {
        if (transportClient != null) {
            transportClient.close();
        }
    }

    /**
     * 获取随机日期
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @return
     */
    public static String randomDate(String beginDate, String endDate) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd");
            Date start = format.parse(beginDate);
            Date end = format.parse(endDate);

            if (start.getTime() >= end.getTime()) {
                return null;
            }

            long date = random(start.getTime(), end.getTime());

            return format.format(new Date(date));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static long random(long begin, long end) {
        long rtn = begin + (long) (Math.random() * (end - begin));
        if (rtn == begin || rtn == end) {
            return random(begin, end);
        }
        return rtn;
    }

    public static void main(String[] args) {
        String date = randomDate("2017-05-04", "2020-04-11");
        System.out.println(date);
    }
}
