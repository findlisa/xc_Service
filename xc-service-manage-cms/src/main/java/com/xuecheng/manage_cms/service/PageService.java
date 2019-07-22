package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PageService {



    @Autowired
    CmsPageRepository cmsPageRepository;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    CmsTemplateRepository cmsTemplateRepository;
    @Autowired//这个不用自己写bean
    GridFsTemplate gridFsTemplate;
    @Autowired//这个在config单独配置
    GridFSBucket gridFSBucket;
    @Autowired
    RabbitTemplate rabbitTemplate;



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
            //更新DataUrl
            one.setDataUrl(cmsPage.getDataUrl());
            //执行更新
            CmsPage save = cmsPageRepository.save(one);
            if(save!=null){
                //返回成功
                return new CmsPageResult(CommonCode.SUCCESS,save);
            }
        }
        return new CmsPageResult(CommonCode.FAIL,null);
    }

    //根据id查询页面
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

    //页面静态化，实现预览功能(要调用其他函数)
    public String generateHtml(String pageId){
       //1.获取页面模型数据
        Map modelByPageId = this.getModelByPageId(pageId);
        //2.获取页面模板
        String templateByPageId = this.getTemplateByPageId(pageId);
        //3.执行静态化
        String html = this.generateHtml(templateByPageId, modelByPageId);
        return html;
    }

    //执行页面发布
    public ResponseResult postPage(String pageId){
       //1.执行页面静态化
        String pageHtml = this.generateHtml(pageId);
        if(StringUtils.isEmpty(pageHtml)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        //2.将静态文件保存到gridFS
        CmsPage cmsPage = this.saveHtml(pageId, pageHtml);

        //3.给队列发送消息，等待消费者消费
        sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

//#######################################################################################上面是主要功能，下面是辅助函数
    //获取页面模型
    private Map getModelByPageId(String pageId){
       //通过页面id查询
        CmsPage byId = this.getById(pageId);
        if (byId==null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        String dataUrl = byId.getDataUrl();
        if(StringUtils.isEmpty(dataUrl)){
            ExceptionCast.cast(CmsCode.CMS_DataUrl_IS_NULL);
        }
        //通过url获取数据模型
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();
        return body;
    }

    //获取页面模板
    private String getTemplateByPageId(String pageId){
       //查询页面
        CmsPage byId = this.getById(pageId);
        if (byId==null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //获取页面模板id
        String templateId = byId.getTemplateId();
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if(optional.isPresent()){
            CmsTemplate cmsTemplate = optional.get();
            //模板文件id
            String templateFileId = cmsTemplate.getTemplateFileId();
            //取出模板文件内容
            GridFSFile gridFSFile =
                    gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开下载流对象
            GridFSDownloadStream gridFSDownloadStream =
                    gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建GridFsResource
            GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
            try {
                String content = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //静态化函数,使模板与数据结合
    private String generateHtml(String template,Map model){
        try {
            //生成配置类
            Configuration configuration = new Configuration(Configuration.getVersion());
            //模板加载器
            StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
            stringTemplateLoader.putTemplate("template",template);
            //配置模板加载器
            configuration.setTemplateLoader(stringTemplateLoader);
            //获取模板
            Template template1 = configuration.getTemplate("template");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template1, model);
            return html;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //id获取页面
    private CmsPage getById(String pageId){
        Optional<CmsPage> byId = cmsPageRepository.findById(pageId);
        if (byId.isPresent()){
            return byId.get();
        }
        return null;
    }

    //保存静态页面内容
    private CmsPage saveHtml(String pageId,String content){
    //查询页面
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if(!optional.isPresent()){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        CmsPage cmsPage = optional.get();
        //存储之前先删除
        String htmlFileId = cmsPage.getHtmlFileId();
        if(StringUtils.isNotEmpty(htmlFileId)){
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(htmlFileId)));
        }
    //保存html文件到GridFS
        InputStream inputStream = IOUtils.toInputStream(content);
       //保存后会得到一个文件id值
        ObjectId objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
    //文件id
        String fileId = objectId.toString();
    //将文件id存储到cmspage中
        cmsPage.setHtmlFileId(fileId);
        cmsPageRepository.save(cmsPage);
        return cmsPage;
    }
    //发送页面消息，生产者生产消息
    private void sendPostPage(String pageId){
        CmsPage cmsPage = this.getById(pageId);
        if(cmsPage == null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        Map<String,String> msgMap = new HashMap<>();
        msgMap.put("pageId",pageId);
        //消息内容
        String msg = JSON.toJSONString(msgMap);
        //获取站点id作为routingKey
        String siteId = cmsPage.getSiteId();
        //发布消息
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,siteId, msg);
    }

}
