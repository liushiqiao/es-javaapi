课程大纲

强调一下，我们的es讲课的风格

1、es这门技术有点特殊，跟比如其他的像纯java的课程，比如分布式课程，或者大数据类的课程，比如hadoop，spark，storm等。不太一样

2、es非常重要的一个api，是它的restful api，你自己思考一下，掌握这个es的restful api，可以让你执行一些核心的运维管理的操作，比如说创建索引，维护索引，执行各种refresh、flush、optimize操作，查看集群的健康状况，比如还有其他的一些操作，就不在这里枚举了。或者说探查一些数据，可能用java api并不方便。

3、es的学习，首先，你必须学好restful api，然后才是你自己的熟悉语言的api，java api。

这个《核心知识篇（上半季）》，其实主要还是打基础，包括核心的原理，还有核心的操作，还有部分高级的技术和操作，大量的实验，大量的画图，最后初步讲解怎么使用java api

《核心知识篇（下半季）》，包括深度讲解搜索这块技术，还有聚合分析这块技术，包括数据建模，包括java api的复杂使用，有一个项目实战s

员工信息

姓名
年龄
职位
国家
入职日期
薪水

我是默认大家至少有java基础的，如果你java一点都不会，请先自己补一下

1、maven依赖

```text
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>transport</artifactId>
    <version>5.2.2</version>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-api</artifactId>
    <version>2.7</version>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
    <version>2.7</version>
</dependency>

log4j2.properties

appender.console.type = Console
appender.console.name = console
appender.console.layout.type = PatternLayout

rootLogger.level = info
rootLogger.appenderRef.console.ref = console

```


2、构建client

```text
Settings settings = Settings.builder()
        .put("cluster.name", "myClusterName").build();
TransportClient client = new PreBuiltTransportClient(settings);

TransportClient client = new PreBuiltTransportClient(Settings.EMPTY)
        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("host1"), 9300))
        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("host2"), 9300));

client.close();

```

3、创建document

```html
IndexResponse response = client.prepareIndex("index", "type", "1")
        .setSource(jsonBuilder()
                    .startObject()
                        .field("user", "kimchy")
                        .field("postDate", new Date())
                        .field("message", "trying out Elasticsearch")
                    .endObject()
                  )
        .get();
```

4、查询document

```text
GetResponse response = client.prepareGet("index", "type", "1").get();
```

5、修改document

```
client.prepareUpdate("index", "type", "1")
        .setDoc(jsonBuilder()               
            .startObject()
                .field("gender", "male")
            .endObject())
        .get();
```

6、删除document

```
DeleteResponse response = client.prepareDelete("index", "type", "1").get();
```

------------------------------------------------------------------------

课程大纲

```
SearchResponse response = client.prepareSearch("index1", "index2")
        .setTypes("type1", "type2")
        .setQuery(QueryBuilders.termQuery("multi", "test"))                 // Query
        .setPostFilter(QueryBuilders.rangeQuery("age").from(12).to(18))     // Filter
        .setFrom(0).setSize(60)
        .get();
```

需求：

（1）搜索职位中包含technique的员工
（2）同时要求age在30到40岁之间
（3）分页查询，查找第一页

```
GET /company/employee/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "position": "technique"
          }
        }
      ],
      "filter": {
        "range": {
          "age": {
            "gte": 30,
            "lte": 40
          }
        }
      }
    }
  },
  "from": 0,
  "size": 1
}
```

告诉大家，为什么刚才一边运行创建document，一边搜索什么都没搜索到？？？？

近实时！！！

默认是1秒以后，写入es的数据，才能被搜索到。很明显刚才，写入数据不到一秒，我门就在搜索。

---------------------------------------------------

课程大纲

```text
SearchResponse sr = node.client().prepareSearch()
    .addAggregation(
        AggregationBuilders.terms("by_country").field("country")
        .subAggregation(AggregationBuilders.dateHistogram("by_year")
            .field("dateOfBirth")
            .dateHistogramInterval(DateHistogramInterval.YEAR)
            .subAggregation(AggregationBuilders.avg("avg_children").field("children"))
        )
    )
    .execute().actionGet();
```

我们先给个需求：

（1）首先按照country国家来进行分组
（2）然后在每个country分组内，再按照入职年限进行分组
（3）最后计算每个分组内的平均薪资

```text
PUT /company
{
  "mappings": {
      "employee": {
        "properties": {
          "age": {
            "type": "long"
          },
          "country": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            },
            "fielddata": true
          },
          "join_date": {
            "type": "date"
          },
          "name": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "position": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "salary": {
            "type": "long"
          }
        }
      }
    }
}

GET /company/employee/_search
{
  "size": 0,
  "aggs": {
    "group_by_country": {
      "terms": {
        "field": "country"
      },
      "aggs": {
        "group_by_join_date": {
          "date_histogram": {
            "field": "join_date",
            "interval": "year"
          },
          "aggs": {
            "avg_salary": {
              "avg": {
                "field": "salary"
              }
            }
          }
        }
      }
    }
  }
}

Map<String, Aggregation> aggrMap = searchResponse.getAggregations().asMap();
		StringTerms groupByCountry = (StringTerms) aggrMap.get("group_by_country");
		Iterator<Bucket> groupByCountryBucketIterator = groupByCountry.getBuckets().iterator();
		
		while(groupByCountryBucketIterator.hasNext()) {
			Bucket groupByCountryBucket = groupByCountryBucketIterator.next();
			
			System.out.println(groupByCountryBucket.getKey() + "\t" + groupByCountryBucket.getDocCount()); 
			
			Histogram groupByJoinDate = (Histogram) groupByCountryBucket.getAggregations().asMap().get("group_by_join_date"); 
			Iterator<org.elasticsearch.search.aggregations.bucket.histogram.Histogram.Bucket> groupByJoinDateBucketIterator = groupByJoinDate.getBuckets().iterator();
			 
			while(groupByJoinDateBucketIterator.hasNext()) {
				org.elasticsearch.search.aggregations.bucket.histogram.Histogram.Bucket groupByJoinDateBucket = groupByJoinDateBucketIterator.next();
				
				System.out.println(groupByJoinDateBucket.getKey() + "\t" + groupByJoinDateBucket.getDocCount()); 
				
				Avg avgSalary = (Avg) groupByJoinDateBucket.getAggregations().asMap().get("avg_salary");
				System.out.println(avgSalary.getValue()); 
			}
		}
		
		client.close();
	}
```


