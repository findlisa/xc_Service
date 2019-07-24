package com.xuecheng.manage_course.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.dao.CourseBaseRepository;
import com.xuecheng.manage_course.dao.CourseMapper;
import com.xuecheng.manage_course.dao.TeachplanMapper;
import com.xuecheng.manage_course.dao.TeachplanRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {


    @Autowired
    CourseMapper courseMapper;
    @Autowired
    TeachplanMapper  teachplanMapper;
    @Autowired
    TeachplanRepository teachplanRepository;
    @Autowired
    CourseBaseRepository courseBaseRepository;



    //查询课程计划
    public TeachplanNode findTeachplanList(String courseId){
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        return teachplanNode;
    }

    //添加课程计划
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        //对关键数据进行检查
        if(teachplan==null||
                StringUtils.isEmpty(teachplan.getCourseid())||
                   StringUtils.isEmpty(teachplan.getPname())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }

        //获取父节点，节点为空添加节点
        String courseid = teachplan.getCourseid();
        String parentid = teachplan.getParentid();
        if (StringUtils.isEmpty(parentid)){//父节点为空要添加新节点，这个新的根节点根据课程添加
            parentid=this.getTeachplanRoot(courseid);
        }
        Teachplan teachplanNew=new Teachplan();
        BeanUtils.copyProperties(teachplan,teachplanNew);
        teachplanNew.setCourseid(courseid);
        teachplanNew.setParentid(parentid);
        //设置级别,要根父节点级别确定
        String parentGrade = getParentGrade(parentid);
        if (parentGrade.equals("1")){
            teachplanNew.setGrade("2");
        }else if(parentGrade.equals("2")){
            teachplanNew.setGrade("3");
        }
        //添加课程计划,保存
        teachplanRepository.save(teachplanNew);

        return new ResponseResult(CommonCode.SUCCESS);




    }
    //查询课程列表
    public QueryResponseResult<CourseInfo> findCourseList(int page, int size,
                                                          CourseListRequest courseListRequest) {
        PageHelper.startPage(page,size);
        //分页查询
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage();
        //获得查询列表
        List<CourseInfo> result = courseListPage.getResult();
        //获取总记录
        long total = courseListPage.getTotal();
        //查询结果集
        QueryResult<CourseInfo> courseInfoQueryResult = new QueryResult<CourseInfo>();
        courseInfoQueryResult.setList(result);
        courseInfoQueryResult.setTotal(total);
        //响应结果集
        return new QueryResponseResult<>(CommonCode.SUCCESS,courseInfoQueryResult);



    }

    //#################################
//    得到新的根节点
    private String getTeachplanRoot(String courseId){
        //在course_base查询课程名称
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if(!optional.isPresent()) {//基本课程为空，什么都做不了，基本课程是系统拍的可以安排的课程
            return null;
        }

        List<Teachplan> list = teachplanRepository.findByCourseidAndParentid(courseId, "0");
        if(list==null||list.size()==0){//查询不到，自动添加根节点
            Teachplan teachplan=new Teachplan();
            teachplan.setCourseid(courseId);
            teachplan.setPname(optional.get().getName());
            teachplan.setParentid("0");
            teachplan.setGrade("1");//1级
            teachplan.setStatus("0");//未发布
            //保存
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }
        return list.get(0).getId();//这个查询的list就是父节点
    }
    //获取父节点grade
    private String getParentGrade(String parentId){
        Optional<Teachplan> optional = teachplanRepository.findById(parentId);
        if(!optional.isPresent()){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        Teachplan teachplan = optional.get();
        return teachplan.getGrade();
    }



}
