package com.xuecheng.manage_cms;

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
    @Test
    public void test(){
        String s = pageService.generateHtml("5a795ac7dd573c04508f3a56");
        System.out.println(s);
    }
}
