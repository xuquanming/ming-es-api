package com.company;

import com.alibaba.fastjson.JSON;
import com.company.pojo.User;
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
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class MingEsApiApplicationTests {

	@Autowired
	@Qualifier("restHighLevelClient")
	private RestHighLevelClient client;

	@Test
	void contextLoads() {
	}

	//测试索引的创建
	@Test
	void testCreateIndex() throws IOException {
		//1.创建索引的请求
		CreateIndexRequest request = new CreateIndexRequest("ming_index");
		//2客户端执行请求，请求后获得响应
		CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
		System.out.println(response);
	}

	//测试索引是否存在
	@Test
	void testExistIndex() throws IOException {
		//1.创建索引的请求
		GetIndexRequest request = new GetIndexRequest("ming_index");
		//2客户端执行请求，请求后获得响应
		boolean exist =  client.indices().exists(request, RequestOptions.DEFAULT);
		System.out.println("测试索引是否存在-----"+exist);
	}

	//删除索引
	@Test
	void testDeleteIndex() throws IOException {
		DeleteIndexRequest request = new DeleteIndexRequest("ming_index");
		AcknowledgedResponse delete = client.indices().delete(request,RequestOptions.DEFAULT);
		System.out.println("删除索引--------"+delete.isAcknowledged());
	}



	//测试添加文档
	@Test
	void testAddDocument() throws IOException {
		User user = new User("ming",27);
		IndexRequest request = new IndexRequest("ming_index");
		request.id("1");
		//设置超时时间
		request.timeout("1s");
		//将数据放到json字符串
		request.source(JSON.toJSONString(user), XContentType.JSON);
		//发送请求
		IndexResponse response = client.index(request,RequestOptions.DEFAULT);
		System.out.println("添加文档-------"+response.toString());
		System.out.println("添加文档-------"+response.status());
//        结果
//        添加文档-------IndexResponse[index=lisen_index,type=_doc,id=1,version=1,result=created,seqNo=0,primaryTerm=1,shards={"total":2,"successful":1,"failed":0}]
//        添加文档-------CREATED
	}


	//测试文档是否存在
	@Test
	void testExistDocument() throws IOException {
		//测试文档的 没有index
		GetRequest request= new GetRequest("ming_index","1");
		//没有indices()了
		boolean exist = client.exists(request, RequestOptions.DEFAULT);
		System.out.println("测试文档是否存在-----"+exist);
	}


	//测试获取文档
	@Test
	void testGetDocument() throws IOException {
		GetRequest request= new GetRequest("ming_index","1");
		GetResponse response = client.get(request, RequestOptions.DEFAULT);
		System.out.println("测试获取文档-----"+response.getSourceAsString());
		System.out.println("测试获取文档-----"+response);

//        结果
//        测试获取文档-----{"age":27,"name":"ming"}
//        测试获取文档-----{"_index":"ming_index","_type":"_doc","_id":"1","_version":1,"_seq_no":0,"_primary_term":1,"found":true,"_source":{"age":27,"name":"lisen"}}

	}


	//测试修改文档
	@Test
	void testUpdateDocument() throws IOException {
		User user = new User("李逍遥", 55);
		//修改是id为1的
		UpdateRequest request= new UpdateRequest("ming_index","1");
		request.timeout("1s");
		request.doc(JSON.toJSONString(user),XContentType.JSON);

		UpdateResponse response = client.update(request, RequestOptions.DEFAULT);
		System.out.println("测试修改文档-----"+response);
		System.out.println("测试修改文档-----"+response.status());

//        结果
//        测试修改文档-----UpdateResponse[index=ming_index,type=_doc,id=1,version=2,seqNo=1,primaryTerm=1,result=updated,shards=ShardInfo{total=2, successful=1, failures=[]}]
//        测试修改文档-----OK

//        被删除的
//        测试获取文档-----null
//        测试获取文档-----{"_index":"ming_index","_type":"_doc","_id":"1","found":false}
	}

	//测试删除文档
	@Test
	void testDeleteDocument() throws IOException {
		DeleteRequest request= new DeleteRequest("ming_index","1");
		request.timeout("1s");
		DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
		System.out.println("测试删除文档------"+response.status());
	}

	//测试批量添加文档
	@Test
	void testBulkAddDocument() throws IOException {
		ArrayList<User> userlist=new ArrayList<User>();
		userlist.add(new User("cyx1",5));
		userlist.add(new User("cyx2",6));
		userlist.add(new User("cyx3",40));
		userlist.add(new User("cyx4",25));
		userlist.add(new User("cyx5",15));
		userlist.add(new User("cyx6",35));

		//批量操作的Request
		BulkRequest request = new BulkRequest();
		request.timeout("1s");

		//批量处理请求
		for (int i = 0; i < userlist.size(); i++) {
			request.add(
					new IndexRequest("ming_index")
							.id(""+(i+1))
							.source(JSON.toJSONString(userlist.get(i)),XContentType.JSON)
			);
		}
		BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);
		//response.hasFailures()是否是失败的
		System.out.println("测试批量添加文档-----"+response.hasFailures());

//        结果:false为成功 true为失败
//        测试批量添加文档-----false
	}


	//测试查询文档
	//SearchRequest  搜索请求
	//SearchSourceBuilder  条件构造
	//HighlightBuilder   构建高亮
	//TermQueryBuilder   精确查询
	//MatchAllQueryBuilder
	//xxx QueryBuilder  对应查询的指令
	@Test
	void testSearchDocument() throws IOException {
		SearchRequest request = new SearchRequest("ming_index");
		//构建搜索条件
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		//设置了高亮
		sourceBuilder.highlighter();
		//term name为a的
		TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "a");
		sourceBuilder.query(termQueryBuilder);
		sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

		request.source(sourceBuilder);
		SearchResponse response = client.search(request, RequestOptions.DEFAULT);

		System.out.println("测试查询文档-----"+JSON.toJSONString(response.getHits()));
		System.out.println("=====================");
		for (SearchHit documentFields : response.getHits().getHits()) {
			System.out.println("测试查询文档--遍历参数--"+documentFields.getSourceAsMap());
		}

//        测试查询文档-----{"fragment":true,"hits":[{"fields":{},"fragment":false,"highlightFields":{},"id":"1","matchedQueries":[],"primaryTerm":0,"rawSortValues":[],"score":1.8413742,"seqNo":-2,"sortValues":[],"sourceAsMap":{"name":"cyx1","age":5},"sourceAsString":"{\"age\":5,\"name\":\"cyx1\"}","sourceRef":{"fragment":true},"type":"_doc","version":-1}],"maxScore":1.8413742,"totalHits":{"relation":"EQUAL_TO","value":1}}
//        =====================
//        测试查询文档--遍历参数--{name=a, age=5}
	}
}
