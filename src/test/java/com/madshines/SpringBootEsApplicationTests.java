package com.madshines;

import com.alibaba.fastjson.JSON;
import com.madshines.pojo.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.query.QuerySearchRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.*;

@SpringBootTest
class SpringBootEsApplicationTests {
    /*
    * 自动注入rest client*/
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    /*
    * 判断客户端是否ioc容器中*/
//    @Autowired
//    private ApplicationContext applicationContext;
//    @Test
//    void testApplicationContext(){
//        boolean restClient = applicationContext.containsBean("restHighLevelClient");
//        System.out.println(restClient);
//    }
    /*
    索引练习
    */
    //新建索引
    @Test
    void testCreateIndex() throws IOException {
        CreateIndexRequest createIndexRequest=new CreateIndexRequest("29index");
        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        boolean acknowledged = createIndexResponse.isAcknowledged();
        System.out.println(acknowledged);
    }
    //删除索引
    @Test
    void testDeleteIndex() throws IOException {
        DeleteIndexRequest deleteIndexRequest=new DeleteIndexRequest("29index");
        AcknowledgedResponse delete = restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
        System.out.println(delete.isAcknowledged());
    }
    //获取索引
    @Test
    void testGetIndex() throws IOException {
        GetIndexRequest getIndexRequest=new GetIndexRequest("29index");
        GetIndexResponse getIndexResponse = restHighLevelClient.indices().get(getIndexRequest, RequestOptions.DEFAULT);
         Map<String, Settings> settings = getIndexResponse.getSettings();
        System.out.println(settings);
    }

    /**
      * @param: 文档练习
      * @return:
      * @author: madshines
      * @date: 5-29
     */
    /*
    * 创建文档*/
    @Test
    void testCreateDocument() throws IOException {
        User user=new User(1,"张一");
        IndexRequest indexRequest=new IndexRequest("29index");
        indexRequest.source(JSON.toJSONString(user), XContentType.JSON);
        indexRequest.timeout(TimeValue.timeValueSeconds(1));
        indexRequest.id("1");//自动生成id
        IndexResponse index = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println(index.status());
    }
    /*
    * 获取文档内容*/
    @Test
    void testGetDocument() throws IOException {
        GetRequest getRequest=new GetRequest("29index","1");
        GetResponse response = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        System.out.println(response.getSourceAsString());
        System.out.println(response);
    }
    /*
    * 文档更新*/
    @Test
    void testUpdateDocument() throws IOException {
        User user = new User(2, "张二");
        UpdateRequest updateRequest = new UpdateRequest("29index", "1");
        updateRequest.doc(JSON.toJSONString(user), XContentType.JSON);
        UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        RestStatus status = update.status();
        System.out.println(status);
    }
    /*
    * 文档删除*/
    @Test
    void testDeleteDocument() throws IOException {
        DeleteRequest deleteRequest=new DeleteRequest("29index","2");
        DeleteResponse delete = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println(delete.status());
    }
    /*
    * 批量新增*/
    @Test
    void testPutBulk() throws IOException {
        BulkRequest bulkRequest=new BulkRequest();
        IndexRequest indexRequest=new IndexRequest("29index");
        bulkRequest.timeout(TimeValue.timeValueSeconds(1));

        HashMap<Integer, User> hashMap = new HashMap<>();
        hashMap.put(1,new User(1,"张一"));
        hashMap.put(2,new User(2,"张二"));
        hashMap.put(3,new User(3,"张三"));
        hashMap.put(4,new User(4,"张四"));
        hashMap.put(5,new User(5,"张五"));
        hashMap.put(6,new User(6,"张六"));
        hashMap.put(7,new User(7,"李二"));
        hashMap.put(8,new User(8,"李三"));

        Set<Map.Entry<Integer, User>> entries = hashMap.entrySet();
        for (Map.Entry<Integer, User> entry : entries) {
            bulkRequest.add(indexRequest.id("" + entry.getKey()));
            System.out.println(entry.getKey());
            bulkRequest.add(indexRequest.source(JSON.toJSONString(entry.getValue()), XContentType.JSON));
            System.out.println(entry.getValue());
            BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            System.out.println(bulk.status());
        }
    }
    /*
    * 批量删除*/
    @Test
    void testDeleteBulk() throws IOException {
        BulkRequest bulkRequest=new BulkRequest();
        for (int i = 1; i < 9; i++) {
            bulkRequest.add(new DeleteRequest("29index",""+i));
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(bulk.status());
    }
    /*
    * 查询*/
    @Test
    void testSearch() throws IOException {
        SearchRequest searchRequest=new SearchRequest("29index");
        SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder();
        /*
        * 模糊查询：searchSourceBuilder.query(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("name", "张")));
        * 查询全部：searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        * 精确查询：searchSourceBuilder.query(QueryBuilders.termQuery("name.keyword","李二"));
        * */
        //searchSourceBuilder.query(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("name","李二")));
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        for (SearchHit hit : searchResponse.getHits()) {
            System.out.println(hit.getSourceAsString());
        }
    }
}
