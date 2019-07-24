package com.xuecheng.manage_cms;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_cms.dao.SysDictionaryDao;
import com.xuecheng.manage_cms.service.PageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class pageService {
    @Autowired
    PageService pageService;

    @Autowired
    SysDictionaryDao sysDictionaryDao;
    @Test
    public void test(){
        String s = pageService.generateHtml("5a795ac7dd573c04508f3a56");
        System.out.println(s);
    }

    @Test
    public void getDic(){
        SysDictionary bydType = sysDictionaryDao.findBydType("200");
        System.out.println(bydType);
    }
}
