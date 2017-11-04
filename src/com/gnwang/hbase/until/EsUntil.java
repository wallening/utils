package com.gnwang.hbase.until;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder.Type;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
/**
 * index: es里的index相当于一个数据库。 
 * type: 相当于数据库里的一个表。 
 * id： 唯一，相当于主键。 
 * node:节点是es实例，一台机器可以运行多个实例，但是同一台机器上的实例在配置文件中要确保http和tcp端口不同（下面有讲）。 
 * cluster:代表一个集群，集群中有多个节点，其中有一个会被选为主节点，这个主节点是可以通过选举产生的，主从节点是对于集群内部来说的。 
 * shards：代表索引分片，es可以把一个完整的索引分成多个分片，这样的好处是可以把一个大的索引拆分成多个，分布到不同的节点上，构成分布式搜索。分片的数量只能在索引创建前指定，并且索引创建后不能更改。 
 * replicas:代表索引副本，es可以设置多个索引的副本，副本的作用一是提高系统的容错性，当个某个节点某个分片损坏或丢失时可以从副本中恢复。二是提高es的查询效率，es会自动对搜索请求进行负载均衡。
 * @author acer
 *
 */
public class EsUntil {
	private static EsUntil esUntil;
	private static TransportClient client;
	
	private EsUntil() {
	}
	
	public synchronized static EsUntil getInstance(String hosts, String clusterName) throws UnknownHostException {
		
		if (esUntil ==null) {
			esUntil = new EsUntil();
			client = getClient(hosts, clusterName);
		}
		
		return esUntil;
	}
	
	/**
	 * 
	 * @param hosts 集群IP地址,如127.0.0.1:1234,127.0.0.2:1234
	 * @param clusterName 集群名称 如 cluster1
	 * @return
	 * @throws UnknownHostException
	 */
	public static TransportClient getClient(String hosts, String clusterName) throws UnknownHostException {
		TransportClient rtn = null;
		Settings esSettings = Settings.builder()
                .put("cluster.name", clusterName) //设置ES实例的名称
                .put("client.transport.sniff", true) //自动嗅探整个集群的状态，把集群中其他ES节点的ip添加到本地的客户端列表中
                .build();
		rtn = new PreBuiltTransportClient(esSettings);//初始化client较老版本发生了变化，此方法有几个重载方法，初始化插件等。
        //此步骤添加IP，至少一个，其实一个就够了，因为添加了自动嗅探配置
		for (String host: hosts.split(",")) {
			String[] ipPort = host.split(":");
			String ip = ipPort[0];
			int port = Integer.parseInt(ipPort[1].trim());
			rtn.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ip), port));
		}
		return rtn;
	}
	
	/**
	 * 别名管理
	 * @param client
	 * @return
	 */
    private IndicesAdminClient getIndicesAdminClient(TransportClient client) {  
        return client.admin().indices();  
    }  
    
    public boolean isExistIndex(String indexName) {
    	IndicesExistsRequest req = new IndicesExistsRequest(indexName);
    	boolean rtn = client.admin().indices().exists(req).actionGet().isExists();
    	LogUntil.getWorkLog().debug("ok  索引: {} 是否存在: {}", indexName, rtn);
    	return rtn;
    }
     
    /**
     * 创建索引
     * @param indexName 索引名 相当于库名
     * @param indexAliasName 别名
     * @param shards 分片数，相当于分区
     * @param replicas 备份数
     * @return
     */
    public boolean createIndex(String indexName, String aliasName, int shards, int replicas) {  
        boolean rtn = false;
    	if (!isExistIndex(indexName)) {  
        	Settings settings = Settings.builder()
        			.put("number_of_shards", shards)
        			.put("number_of_replicas", replicas)
        			.build();
            CreateIndexResponse response = getIndicesAdminClient(client)  
                    .prepareCreate(indexName.toLowerCase())
                    .addAlias(new Alias(aliasName))
                    .setSettings(settings).execute().actionGet();
            rtn =  response.isAcknowledged();  
        }  
        LogUntil.getWorkLog().debug("ok  创建索引: {} 是否成功: {}", indexName, rtn);
        return rtn;  
    }
    
    public boolean deleteIndex( String indexName) {  
    	boolean rtn = false;
        if (isExistIndex(indexName)) {  
        	rtn = getIndicesAdminClient(client)  
                    .prepareDelete(indexName.toLowerCase()).get().isAcknowledged();  
        	LogUntil.getWorkLog().debug("ok  删除索引: {} 是否成功: {}", indexName, rtn);
        }  
        return rtn;   
  }  
    
    public String[] getIndices() {
    	String[] rtn = null;
    	rtn = getIndicesAdminClient(client).prepareGetIndex().get().indices();    	
    	LogUntil.getWorkLog().debug("ok  索引个数: {} ", rtn == null? 0: rtn.length);
    	return rtn;
    }
    
    public boolean isAliasExist(String... aliases) {  
    	boolean rtn = false;
        rtn = getIndicesAdminClient(client)  
                .prepareAliasesExist(aliases).get().isExists();  
        return rtn;
    }  

    public boolean addAliasIndex(String indexName, String aliasName) {  
    	 boolean rtn = false;
    	 rtn = getIndicesAdminClient(client)
			    	 .prepareAliases().addAlias(indexName.toLowerCase(), aliasName)
			    	 .get().isAcknowledged();
    	 LogUntil.getWorkLog().debug("ok  添加别名  索引: {} 别名: {} 是否成功: {}", indexName, aliasName, rtn);
        return rtn;  
    }
    
    public boolean deleteAliasIndex(String indexName, String aliasName) {  
    	 boolean rtn = false;
        if (isExistIndex(indexName)) {  
        	rtn = getIndicesAdminClient(client)  
                    .prepareAliases().removeAlias(indexName.toLowerCase(), aliasName).get().isAcknowledged();  
        	LogUntil.getWorkLog().debug("ok  删除别名  索引: {} 别名: {} 是否成功: {}", indexName, aliasName, rtn);
        }  
        return rtn;  
  
    }    
    
    public boolean CreateIndexMapping(String indexName, String typeName, Map<String, Map<String, String>> properties) {  
    	boolean rtn = false;
    	JSONObject propertiesJson = new JSONObject();
    	for (String key: properties.keySet()) {
    		Map<String, String> keyProperties = properties.get(key);
    		
    		JSONObject keyPropertiesJson = new JSONObject();
    		for (String proName: keyProperties.keySet()) {
    			keyPropertiesJson.put(proName, keyProperties.get(proName));
    		}
    		propertiesJson.put(key, keyPropertiesJson);
    	}
    	JSONObject mapping = new JSONObject();
    	mapping.put(typeName, propertiesJson);
    	rtn = CreateIndexMapping(indexName, typeName, mapping.toJSONString());
    	return rtn;
    }
    /** 
     * 创建自定义mapping的索引 前提是得先要创建索引 建立mapping (相当于建立表结构) 
     *  {"index_type":{"properties":{"content":{"type":"string"},"id":{"type":"long"},"posttime":{"type":"date","format":"dateOptionalTime"},"title":{"type":"string"},"example":{"type":"string","analyzer":"ik"}}}}  
     * index_name 已经创建好、尚未创建的索引名称
             index_type 索引的类型名称，同一个索引可以拥有多个不同type(已测试)；同时提醒：注意在mapping_json起始位置的类型名称和 setType("index_type")名字相同，否则会报错。
     * @param client 
     * @param indexName 
     * @param typeName 
     * @param mapping 
     * @return 
     * 
     */  
    public boolean CreateIndexMapping(String indexName, String typeName, String mapping) {  
//    	{
//    		"index_type": {
//    			"properties": {
//    				"content": {
//    					"type": "string"
//    				},
//    				"id": {
//    					"type": "long"
//    				},
//    				"posttime": {
//    					"type": "date",
//    					"format": "dateOptionalTime"
//    				},
//    				"title": {
//    					"type": "string"
//    				},
//    				"example": {
//    					"type": "string",
//    					"analyzer": "ik"
//    				}
//    			}
//    		}
//    	}
    	boolean rtn = false;
    	rtn = getIndicesAdminClient(client)
                .preparePutMapping(indexName.toLowerCase()).setType(typeName)  
                .setSource(mapping, XContentType.JSON).get().isAcknowledged();  
    	LogUntil.getWorkLog().debug("ok  创建mapping  索引: {} 类型: {} 是否成功: {}", indexName, typeName, rtn);
    	return rtn;
    }
    
    public void insertDoc(String indexName, String typeName, String key, Map<String, Object> builder) {
    	client.prepareIndex(indexName, typeName, key).setSource(builder).get();
    	LogUntil.getWorkLog().debug("ok  插入数据  索引: {} 类型: {} key: {}", indexName, typeName, key);
    }
    
    public void insertDoc(String indexName, String typeName, String key, XContentBuilder builder) {
//    	try {
//			builder = XContentFactory.jsonBuilder().startObject()
//					.field("field1", "value1")
//					.field("field2", "value2")
//					.endObject();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
    	long version = client.prepareIndex(indexName, typeName, key).setSource(builder).get().getVersion();
    	LogUntil.getWorkLog().debug("ok  插入数据  索引: {} 类型: {} key: {} version: {}", indexName, typeName, key, version);
    }
    
    public boolean insertDocs(String indexName, String typeName, Map<String, XContentBuilder> builders) {
    	boolean rtn = false;
    	BulkRequestBuilder bulk = client.prepareBulk();
    	for(String key: builders.keySet()) {
    		IndexRequestBuilder request = client.prepareIndex(indexName, typeName, key).setSource(builders.get(key));
    		bulk.add(request);
    	}
    	rtn = !bulk.get().hasFailures();
    	LogUntil.getWorkLog().debug("ok  批量插入数据  索引: {} 类型: {} 数目: {} ", indexName, typeName, builders.size());

    	return rtn;
    }

    
    public void deleteDoc(String indexName, String typeName, String key){  
    	long version = client.prepareDelete(indexName, typeName,key)  
                                               .execute()  
                                               .actionGet().getVersion();  
    	LogUntil.getWorkLog().debug("ok  删除数据  索引: {} 类型: {} key: {} version: {}", indexName, typeName, key, version);
    }  
    
    public void deleteDocs(String indexName, String typeName, String[] keys){  
    	BulkRequestBuilder bulk = client.prepareBulk();
    	for (String key: keys) {
    		DeleteRequest delReq = new DeleteRequest(indexName, typeName, key);
    		bulk.add(delReq);
    	}
    	bulk.get();
    	LogUntil.getWorkLog().debug("ok  批量删除数据  索引: {} 类型: {} 数目: {} ", indexName, typeName, keys.length);
    }  
    
    public String queryByKey(String indexName, String typeName, String key){  
        String rtn = client.prepareGet(indexName, typeName, key)  
                                     .execute()  
                                     .actionGet().getSourceAsString();  
    	LogUntil.getWorkLog().debug("ok  查询数据  索引: {} 类型: {} key: {} ", indexName, typeName, key);

       return rtn;
    }  
    
    public void queryByKeys(String indexName, String typeName, String key){  

    	MultiGetResponse  multiGetItemResponses  =  client.prepareMultiGet()
	        .add("twitter",  "tweet",  "1")         
	        .add("twitter",  "tweet",  "2",  "3",  "4")  
	        .add("another",  "type",  "foo")                  
	        .get();

    	for  (MultiGetItemResponse  itemResponse  :  multiGetItemResponses)  {  
	        org.elasticsearch.action.get.GetResponse  response  =  itemResponse.getResponse();
	        if  (response.isExists())  {                                            
	                String  json  =  response.getSourceAsString();
	                System.out.println(json);
	        }
    	}
    	LogUntil.getWorkLog().debug("ok  查询数据  索引: {} 类型: {} key: {} ", indexName, typeName, key);

    }  
    
    /**
     * QueryBuilders.termQuery("destIp", "114.114.114.114")
     */
    public SearchResponse search(String[] indices, String[] types, QueryBuilder queryBuilder, QueryBuilder poatFilter, AggregationBuilder aggregation, int offset, int size){  
        SearchResponse rtn = client.prepareSearch(indices)  
                                        .setTypes(types)  
                                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)  //精确查询
                                        .setQuery(queryBuilder)  
                                        .setPostFilter(poatFilter)   //QueryBuilders.rangeQuery("age").gt(10).lt(20)
                                        .addAggregation(aggregation)
                                        .setFrom(offset)  
                                        .setSize(size)
                                        .setExplain(true)  
                                        .execute().actionGet();  
        return rtn;
    }  
    
    public SearchResponse search(String[] indices, String[] types, QueryBuilder queryBuilder, AggregationBuilder aggregation, int offset, int size){  
        SearchResponse rtn = client.prepareSearch(indices)  
                                        .setTypes(types)  
                                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)  //精确查询
                                        .setQuery(queryBuilder)  
                                        .addAggregation(aggregation)
                                        .setFrom(offset)  
                                        .setSize(size)
                                        .setExplain(true)  
                                        .execute().actionGet();  
        
        return rtn;
    }  
    
    public SearchResponse search(String[] indices, String[] types, QueryBuilder queryBuilder, QueryBuilder poatFilter, int offset, int size){  
        SearchResponse rtn = client.prepareSearch(indices)  
                                        .setTypes(types)  
                                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)  //精确查询
                                        .setQuery(queryBuilder)  
                                        .setPostFilter(poatFilter)   //QueryBuilders.rangeQuery("age").gt(10).lt(20)
                                        .setFrom(offset)  
                                        .setSize(size)
                                        .setExplain(true)  
                                        .execute().actionGet();  
        return rtn;
    }  
    
    public void multiSearchQuery(String index, String type) {
    	 QueryBuilder qb1 = QueryBuilders.matchQuery("name", "张三");  
         QueryBuilder qb2 = QueryBuilders.matchQuery("name", "李四");  
         QueryBuilder qb3 = QueryBuilders.matchQuery("name", "车");  
         QueryBuilder qb4 = QueryBuilders.matchQuery("name", "货");  

         SearchRequestBuilder searchRB1 = client.prepareSearch(index)  
                 .setTypes(type).setQuery(qb1).setFrom(0).setSize(8);  
         SearchRequestBuilder searchRB2 = client.prepareSearch(index)  
                 .setTypes(type).setQuery(qb2).setFrom(0).setSize(9);  
         SearchRequestBuilder searchRB3 = client.prepareSearch(index)  
                 .setTypes(type).setQuery(qb3).setFrom(0).setSize(11);  
         SearchRequestBuilder searchRB4 = client.prepareSearch(index)  
                 .setTypes(type).setQuery(qb4).setFrom(0).setSize(12);  

         MultiSearchResponse response = client.prepareMultiSearch()  
                 .add(searchRB1)  
                 .add(searchRB2)  
                 .add(searchRB3)  
                 .add(searchRB4)  
                 .execute().actionGet();  
           
         for (MultiSearchResponse.Item item : response.getResponses()) {  
             SearchResponse searchResponse = item.getResponse();  
             SearchHits hits = searchResponse.getHits();  
             long count = hits.getTotalHits();
             LogUntil.getWorkLog().debug("ok  mulltiSearch  总数: {} ",count);
//             for (SearchHit hit : hits) {  
//                 String json = hit.getSourceAsString();  
//     	        
//             }  
         }
    }
             
    public String parsesearchresponse(SearchResponse searchresponse) {
    	JSONArray rtn = new JSONArray();
    	SearchHits hits = searchresponse.getHits();
        long count = hits.getTotalHits();
    	LogUntil.getWorkLog().debug("ok  查询数据  总数: {} ",count);
    	for (SearchHit hit : hits) {    
    	    String json = hit.getSourceAsString();    
    	    rtn.add(json);
    	}    
    	return rtn.toString();
    }
    public AggregationBuilder simpleAggregationBuilder() {
    	AggregationBuilder aggregation = null;
    	aggregation = AggregationBuilders.terms("agg1").field("field");
    	aggregation = AggregationBuilders.dateHistogram("agg2")
        .field("birth").dateHistogramInterval(DateHistogramInterval.YEAR);
    	return aggregation;
    }
    
    public QueryBuilder simpleDemo() {
    	QueryBuilder builder = null;
    	/*
    	 * 默认的standard analyzer分词规则：
    	 * 去掉大部分标点符号，并以此分割原词为多个词，把分分割后的词转为小写放入token组中
    	 * 对于not-analyzed的词，直接把原词放入token组中。
    	 * matchQuery的机制是：先检查字段类型是否是analyzed，如果是，则先分词，再去去匹配token；如果不是，则直接去匹配token
    	 * termQuery的机制是：直接去匹配token。
    	 */
    	String query = "我的宝马多少马力";
    	
    	//基本查询
    	//上面的查询匹配就会进行分词，比如"宝马多少马力"会被分词为"宝马 多少 马力", 所有有关"宝马 多少 马力", 那么所有包含这三个词中的一个或多个的文档就会被搜索出来
    	builder = QueryBuilders.matchQuery("title1", query);
    	//一个文档"我的保时捷马力不错"也会被搜索出来，那么想要精确匹配所有同时包含"宝马 多少 马力"的文档怎么做？就要使用 match_phrase 了
    	builder = QueryBuilders.matchPhraseQuery("title1", query);
    	//紧邻搜索,match_phrase的搜索方式和match类似，先对搜索词建立索引，并要求所有分词必须在文档中出现(像不像operator为and的match查询)，除此之外，还必须满足分词在文档中出现的顺序和搜索词中一致且各搜索词之间必须紧邻
    	//紧邻对于匹配度要求较高，为了减小精度增加可操作性，引入了slop参数。该参数可以指定相隔多少个词仍被算作匹配成功
    	builder = QueryBuilders.matchPhraseQuery("title1", query).slop(1);
    	//multi_match查询
    	//multiMatchQuery(text,fields)其中的fields是字段的名字，可以写好几个，每一个中间用逗号分隔
    	builder = QueryBuilders.multiMatchQuery(query, "title1","title2");
    	//我们希望完全匹配的文档占的评分比较高，则需要使用best_fields,意思就是完全匹配"宝马 发动机"的文档评分会比较靠前，如果只匹配宝马的文档评分乘以0.3的系数
		builder = QueryBuilders.multiMatchQuery(query, "title1","title2").type(Type.BEST_FIELDS).tieBreaker(0.3f);
    	//我们希望越多字段匹配的文档评分越高，就要使用most_fields
		builder = QueryBuilders.multiMatchQuery(query, "title1","title2").type(Type.MOST_FIELDS);
		//我们会希望这个词条的分词词汇是分配到不同字段中的，那么就使用cross_fields
		builder = QueryBuilders.multiMatchQuery(query, "title1","title2").type(Type.CROSS_FIELDS);
		//match_all查询
		builder = QueryBuilders.matchAllQuery();

		//term是代表完全匹配，即不进行分词器分析，文档中必须包含整个搜索的词汇
    	//对于三个词molong1208，blog，csdn在title字段进行查询，如果有三者中的任意一个即算匹配
    	builder = QueryBuilders.termsQuery("title1", "value1","value2","value3");
    	
    	//常用词查询
    	//可以在后面设置具体的cutoffFrequency
    	builder = QueryBuilders.commonTermsQuery("name", "lishici");
    	
    	
    	//query_string查询
    	builder = QueryBuilders.queryStringQuery("");
    	
    	//simple_query_string查询
    	builder = QueryBuilders.simpleQueryStringQuery("");
    	
    	//前缀查询
    	builder = QueryBuilders.prefixQuery("title", "mo");

    	//配符查询
    	builder = QueryBuilders.wildcardQuery("title", "molo?g");
    	
    	//rang查询
    	//对于某一个field，大于多少，小于多少
    	builder = QueryBuilders.rangeQuery("age").gt(10).lt(20).includeLower(true).includeUpper(false);
    	
    	//使用模糊查询匹配文档查询
    	builder = QueryBuilders.fuzzyQuery("title", "value1");
    	
    	//dismax查询
    	builder = QueryBuilders.disMaxQuery().add(QueryBuilders.termQuery("title", "molong1208"));
    	
    	//正则表达式查询
    	builder = QueryBuilders.regexpQuery("field", "regexp");
    	
    	//bool查询
    	BoolQueryBuilder bool = QueryBuilders.boolQuery();
    	List<QueryBuilder> list = bool.must();
    	list.add(builder);
    	list.add(builder);
    	bool.should();
    	
    	builder = QueryBuilders.boolQuery().must(QueryBuilders.multiMatchQuery("value1", "name","title","title_1")).must(QueryBuilders.multiMatchQuery("value2", "title2","title3"));
    	
    	//过滤器
    	builder = QueryBuilders.existsQuery("title");
    	
    	
    	
    	return builder;
    }
    
    
    public static void main(String[] args) {
		System.out.println(QueryBuilders.matchQuery("query", "我的宝马多少马力"));
	}
      
      
}
