package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EsCourseService {

    @Value("${xuecheng.elasticsearch.course.index}")
    private String index;
    @Value("${xuecheng.elasticsearch.course.type}")
    private String type;
    @Value("${xuecheng.elasticsearch.course.source_field}")
    private String source_field;

    @Autowired
    RestHighLevelClient restHighLevelClient;


    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam) throws IOException {
        if(courseSearchParam==null){//传进来的值为空
            courseSearchParam = new CourseSearchParam();
        }

        //创建搜索请求对象
        SearchRequest searchRequest=new SearchRequest(index);
        //搜索类型
        searchRequest.types(type);
        //显示字段，显示在搜索结果里
        SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder();
        String[] source_field_array = source_field.split(",");
        searchSourceBuilder.fetchSource(source_field_array,new String[]{});//显示字段和不显示字段,不显示Description，因为内容较多，不显示

        //创建bool查询对象,组装条件
        BoolQueryBuilder boolQueryBuilder=new BoolQueryBuilder();
        //搜索条件
        //1.关键字
        if(StringUtils.isNoneEmpty(courseSearchParam.getKeyword())){
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(), "name", "description", "teachplan")
                    .minimumShouldMatch("70%")
                    .field("name", 10);
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }
        //2.根据分类
        if(StringUtils.isNoneEmpty(courseSearchParam.getMt())){//一级分类
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt",courseSearchParam.getMt()));
        }
        if(StringUtils.isNoneEmpty(courseSearchParam.getSt())){//二级分类
            boolQueryBuilder.filter(QueryBuilders.termQuery("st",courseSearchParam.getSt()));
        }
        if(StringUtils.isNoneEmpty(courseSearchParam.getGrade())){//难度等级
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade",courseSearchParam.getGrade()));
        }

        //分页查询
        if(page<0){
            page=1;
        }
        if(size<0) {
            size = 1;
        }
        //起始下标.页码从0开始
        int from=(page-1)*size;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        //加入源
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);

        //创建结果集对象
        QueryResult<CoursePub> queryResult=new QueryResult<CoursePub>();
        List<CoursePub> list =new ArrayList<>();

        //执行搜索,要用客户端
        SearchResponse search = restHighLevelClient.search(searchRequest);

        //获取响应结果
        SearchHits hits = search.getHits();

        //匹配度高的记录
        SearchHit[] seachHits = hits.getHits();
        for (SearchHit seachHit : seachHits) {
            CoursePub coursePub=new CoursePub();
            //源文档
            Map<String, Object> sourceAsMap = seachHit.getSourceAsMap();
            //取出名称
            String name = (String) sourceAsMap.get("name");
            coursePub.setName(name);
            //图片
            String pic = (String) sourceAsMap.get("pic");
            coursePub.setPic(pic);
            //课程id
            String id=(String) sourceAsMap.get("id");
            coursePub.setId(id);
            //价格
            Float price = null;
            try {
                if(sourceAsMap.get("price")!=null ){
                    price = Float.parseFloat(sourceAsMap.get("price").toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            coursePub.setPrice(price);
            Float price_old = null;
            try {
                if(sourceAsMap.get("price_old")!=null ){
                    price_old = Float.parseFloat(sourceAsMap.get("price_old").toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            coursePub.setPrice_old(price_old);
            list.add(coursePub);

        }
        //查询列表
        queryResult.setList(list);
        //总记录数
        queryResult.setTotal(hits.totalHits);
        //返回响应结果集
        QueryResponseResult<CoursePub> queryResponseResult=new QueryResponseResult<CoursePub>(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }
}
