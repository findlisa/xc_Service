package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Service
public class PageService {
    @Autowired
    CmsPageRepository cmsPageRepository;
    //返回的是一个查询结果
    public QueryResponseResult findList(int page,int size, QueryPageRequest queryPageRequest){
        if(queryPageRequest==null){
            queryPageRequest=new QueryPageRequest();
        }
        //分页页面设置
        if(page<0){
            page=1;
        }
        if(size<0){
            size=10;
        }
        page=page-1;
        Pageable pageable= PageRequest.of(page,size);


        //封装条件
        CmsPage cmsPage=new CmsPage();
        //站点
        if(StringUtils.isNoneEmpty(queryPageRequest.getSiteId())){
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        //模板id
        if(StringUtils.isNoneEmpty(queryPageRequest.getTemplateId())){
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        //页面别名
        if(StringUtils.isNoneEmpty(queryPageRequest.getPageAliase())){
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        //页面名称
        if(StringUtils.isNoneEmpty(queryPageRequest.getPageName())){
            cmsPage.setPageName(queryPageRequest.getPageName());
        }
        //页面id
        if(StringUtils.isNoneEmpty(queryPageRequest.getPageId())){
            cmsPage.setPageId(queryPageRequest.getPageId());
        }


        //设置条件匹配器
        ExampleMatcher exampleMatcher=ExampleMatcher.matching();
        exampleMatcher = exampleMatcher.withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);

        Page<CmsPage> all = cmsPageRepository.findAll(example,pageable);

        QueryResult queryResult=new QueryResult();
        queryResult.setList(all.getContent());
        queryResult.setTotal(all.getTotalElements());

        QueryResponseResult queryResponseResult=new QueryResponseResult(CommonCode.SUCCESS,queryResult);

        return queryResponseResult;
    }

   /* //新增页面
    public CmsPageResult save(CmsPage cmsPage) {
        //调用dao新增页面
        //查看是否重复
        CmsPageResult cmsPageResult;
        CmsPage page = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath
                (cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if(page==null){
            cmsPageRepository.save(cmsPage);
            cmsPageResult=new CmsPageResult(CommonCode.SUCCESS,cmsPage);
            return cmsPageResult;
        }
        cmsPageResult=new CmsPageResult(CommonCode.FAIL,null);
        return cmsPageResult;
    }*/
   //新增页面,加入了异常处理
   public CmsPageResult save(CmsPage cmsPage) {

       //调用dao新增页面
       //查看是否重复
       CmsPageResult cmsPageResult;
       CmsPage page = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath
               (cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
       if(page!=null){
           //进行自定义异常处理
           ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
       }
       cmsPageRepository.save(cmsPage);
       cmsPageResult=new CmsPageResult(CommonCode.SUCCESS,cmsPage);
       return cmsPageResult;


   }

    //修改页面

    public CmsPageResult edit(String id, CmsPage cmsPage) {
        //先id查询
        CmsPage one = this.findById(id);
        if (one!=null){
            //要一个一个set,因为不是所有数据都修改，不修改的就没动。
            //更新模板id
            one.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            one.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            one.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            one.setPageName(cmsPage.getPageName());
            //更新访问路径
            one.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            one.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //执行更新
            CmsPage save = cmsPageRepository.save(one);
            if(save!=null){
                //返回成功
                return new CmsPageResult(CommonCode.SUCCESS,save);
            }
        }
        return new CmsPageResult(CommonCode.FAIL,null);
    }

    //根据id查询用户
    public CmsPage findById(String id) {
        Optional<CmsPage> byId = cmsPageRepository.findById(id);
        if(byId.isPresent()){
            CmsPage cmsPage = byId.get();
            return cmsPage;
        }
        return null;

    }

    //删除信息
    public ResponseResult deleteById(String id) {
        Optional<CmsPage> byId = cmsPageRepository.findById(id);
        if (byId.isPresent()){
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }
}
