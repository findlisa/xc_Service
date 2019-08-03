package com.xuecheng.manage_cms;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class uploadTemplate {
    @Autowired
    GridFsTemplate gridFsTemplate;
    //文件存储2
    @Test
    public void testStore2() throws FileNotFoundException {
        File file = new File("D:\\course.ftl");
        FileInputStream inputStream = new FileInputStream(file);
        //保存模版文件内容
        ObjectId gridFSFileId = gridFsTemplate.store(inputStream, "课程详情模板文件i");
        System.out.println(gridFSFileId);
    }

}
