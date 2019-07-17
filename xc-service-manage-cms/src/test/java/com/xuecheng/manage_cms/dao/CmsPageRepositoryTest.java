package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {
    @Autowired
    CmsPageRepository cmsPageRepository;
    @Test
    public void testFindAll(){
        List<CmsPage> all = cmsPageRepository.findAll();
        System.out.println(all);
    }

    @Test
    public void testFindPage(){
        Pageable pageable=PageRequest.of(0,10);
        Page<CmsPage> all = cmsPageRepository.findAll(pageable);
        System.out.println(all);
    }

    @Test
    public  void testFindCondition(){
        //分页
        Pageable pageable=PageRequest.of(0,10);

       //封装条件,想用什么属性就封装什么属性
        CmsPage cmsPage=new CmsPage();
        cmsPage.setSiteId("5a751fab6abb5044e0d19ea12");
        //条件匹配器
        ExampleMatcher exampleMatcher=ExampleMatcher.matching();
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);//结合，全匹配

        //调用查询函数
        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);

        System.out.println(all);


    }
    @Test
    public  void testFindCondition2(){
        //分页
        Pageable pageable=PageRequest.of(0,10);

        //封装条件,想用什么属性就封装什么属性
        CmsPage cmsPage=new CmsPage();
        cmsPage.setPageAliase("轮播");
        //条件匹配器模糊匹配
        ExampleMatcher exampleMatcher=ExampleMatcher.matching();
        exampleMatcher = exampleMatcher.withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);//结合，模糊匹配
        //调用查询函数
        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);

        System.out.println(all);


    }
}
