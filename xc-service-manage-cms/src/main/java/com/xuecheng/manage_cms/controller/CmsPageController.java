package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.CmsPageControllerApi;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cms/page")
public class CmsPageController implements CmsPageControllerApi {
    @Autowired
    private PageService pageService;
    @Override
    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult findList(@PathVariable("page") int page, @PathVariable("size") int size, QueryPageRequest queryPageRequest) {
        //暂时采用测试数据，测试接口是否可以正常运行

//        QueryResult queryResult = new QueryResult();
//        //总数目
//        queryResult.setTotal(1);
//        //创建一个页面
//        CmsPage cmsPage=new CmsPage();
//        cmsPage.setPageName("测试页面");
//        List list=new ArrayList();
//        //添加页面
//        list.add(cmsPage);

        //添加list
//        queryResult.setList(list);

        QueryResponseResult queryResponseResult = pageService.findList(page, size, queryPageRequest);

        return queryResponseResult;


    }

    @Override
    @PostMapping("/add")
    //RequestBody把json格式转为模型
    public CmsPageResult save(@RequestBody CmsPage cmsPage) {
        return pageService.save(cmsPage);
    }
}
