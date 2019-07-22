package com.xuecheng.manage_cms_client.mq;

import com.alibaba.fastjson.JSONObject;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.service.pageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sun.awt.SunHints;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

//消费者接收消息消息
@Component
public class ConsumerPostPage {


    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerPostPage.class);
    @Autowired
    CmsPageRepository cmsPageRepository;
    @Autowired
    pageService pageService;


    @RabbitListener(queues={"${xuecheng.mq.queue}"})
    public void postPage(String msg){
        Map map= JSONObject.parseObject(msg,Map.class);
        LOGGER.info("receive cms post page:{}",msg.toString());
        //取出页面id
        String pageId = (String)map.get("pageId");
        //查询页面信息
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if(!optional.isPresent()){
            LOGGER.error("receive cms post page,cmsPage is null:{}",msg.toString());
        }
        //将页面保存 到物理路径
        pageService.savePageToServerPath(pageId);
    }
}
