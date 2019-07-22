package com.xuecheng.manage_cms_client.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.dao.CmsSiteRepository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Optional;
@Service
public class pageService {
    //日志排错
    private static final Logger LOGGER= LoggerFactory.getLogger(pageService.class);

    @Autowired
    CmsPageRepository cmsPageRepository;
    @Autowired
    CmsSiteRepository cmsSiteRepository;
    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    GridFSBucket gridFSBucket;


//    将页面html保存到服务器物理路径
    public void savePageToServerPath(String pageId){
        //1.取出页面要存的物理路径
        CmsPage cmsPage = this.getPageById(pageId);
        CmsSite cmsSite = this.getSiteById(cmsPage.getSiteId());
            //物理路径
        String pagePath=cmsSite.getSitePhysicalPath()+cmsPage.getPagePhysicalPath()+cmsPage.getPageName();


        //2.获取html页面
        String htmlFileId = cmsPage.getHtmlFileId();
        InputStream inputStream = this.findByHtmlId(htmlFileId);
        if(inputStream == null){
            //流为空，抛出异常
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }

        //3.将html页面存到服务器物理路径
        FileOutputStream fileOutputStream=null;
        try {
            //创建输出流，指定文件名
            fileOutputStream=new FileOutputStream(new File(pagePath));
            IOUtils.copy(inputStream,fileOutputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //##############################################################3
    //id查站点
    public CmsSite getSiteById(String siteId){
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        if(!optional.isPresent()){
            //抛出站点不存在
            ExceptionCast.cast(CmsCode.CMS_SITE_NOTEXISTS);
        }
        CmsSite cmsSite = optional.get();
        return cmsSite;

    }
    //id查页面
    public CmsPage getPageById(String pageId){
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if(!optional.isPresent()){
            //抛出页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //获取页面和站点
        CmsPage cmsPage = optional.get();
        return cmsPage;
    }
    //htmlFileId在mongodb查询页面内容,得到流
    public InputStream findByHtmlId(String fileId){
        try {
            //查
            GridFSFile gridFSFile =
                    gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
            //打开下载流流
            GridFSDownloadStream gridFSDownloadStream =
                    gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
            return gridFsResource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    //

}
