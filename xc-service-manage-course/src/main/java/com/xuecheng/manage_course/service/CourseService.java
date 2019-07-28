package com.xuecheng.manage_course.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.CoursePic;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.dao.*;
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
    @Autowired
    CourseMarketRepository courseMarketRepository;
    @Autowired
    CoursePicRepository coursePicRepository;



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

    //添加基础课程
    @Transactional//添加事务
    public AddCourseResult addCourseBase(CourseBase courseBase){
        //课程状态默认为未发布
        courseBase.setStatus("202001");
        //保存
        CourseBase save = courseBaseRepository.save(courseBase);
        return new AddCourseResult(CommonCode.SUCCESS,save.getId());
    }

    //基本课程信息查询
    public CourseBase getCourseBaseById(String courseId) throws RuntimeException {
        Optional<CourseBase> byId = courseBaseRepository.findById(courseId);
        if (byId.isPresent()){
            CourseBase courseBase = byId.get();
            return courseBase;
        }
        return null;
    }
    //修改基基本课程信息
    @Transactional
    public ResponseResult updateCourseBase(String courseId,CourseBase courseBase){
        CourseBase one = this.getCourseBaseById(courseId);
        if (one == null) {
            //抛出异常..
        }
        //修改课程信息
        one.setName(courseBase.getName());
        one.setMt(courseBase.getMt());
        one.setSt(courseBase.getSt());
        one.setGrade(courseBase.getGrade());
        one.setStudymodel(courseBase.getStudymodel());
        one.setUsers(courseBase.getUsers());
        one.setDescription(courseBase.getDescription());
        CourseBase save = courseBaseRepository.save(one);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //查询课程营销信息
    public CourseMarket getCourseMarketById(String courseId){
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseId);
        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    //修改课程营销信息
    @Transactional
    public ResponseResult updateCourseMarket(String id,CourseMarket courseMarket){
        //查询
        CourseMarket  one= getCourseMarketById(id);
        if(one!=null){//如果有
            one.setCharge(courseMarket.getCharge());
            one.setStartTime(courseMarket.getStartTime());//课程有效期，开始时间
            one.setEndTime(courseMarket.getEndTime());//课程有效期，结束时间
            one.setPrice(courseMarket.getPrice());
            one.setQq(courseMarket.getQq());
            one.setValid(courseMarket.getValid());
            courseMarketRepository.save(one);
        }else{
        one=new CourseMarket();
        BeanUtils.copyProperties(courseMarket,one);
        //设置课程id
        one.setId(id);
        courseMarketRepository.save(one);
        }

        if(one==null){
            return new ResponseResult(CommonCode.FAIL);
        }

        return  new ResponseResult(CommonCode.SUCCESS);

    }

    //保存课程图片，关联到课程
    @Transactional
    public ResponseResult saveCoursePic(String courseId, String pic){

        //查询课程图片,有了就更新，没有就新添

        CoursePic coursePic = null;
        Optional<CoursePic> picOptional = coursePicRepository.findById(courseId);
        if(picOptional.isPresent()){

            coursePic = picOptional.get();
        }
        coursePic=new CoursePic();
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }
    //查询课程图片
    public CoursePic findCoursepic(String courseId) {
        CoursePic coursePic = null;
        Optional<CoursePic> picOptional = coursePicRepository.findById(courseId);
        if(picOptional.isPresent()){
            coursePic=picOptional.get();
            return coursePic;
        }

        return null;
    }

    //删除课程图片
    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {
        //执行删除，返回1表示删除成功，返回0表示删除失败
        long result = coursePicRepository.deleteByCourseid(courseId);
        if(result>0){
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
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
