package com.xuecheng.framework.exception;

import com.google.common.collect.ImmutableMap;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice//控制器增强
public class ExceptionCatch {


    //记录日志
    private static final Logger logger= LoggerFactory.getLogger(ExceptionCatch.class);

    //使用EXCEPTIONS存放异常类型和错误代码的映射，ImmutableMap的特点的一旦创建不可改变，并且线程安全
    private static ImmutableMap<Class<? extends Throwable>,ResultCode> EXCEPTIONS;
    //使用builder来构建一个异常类型和错误代码的异常
    protected static ImmutableMap.Builder<Class<? extends Throwable>,ResultCode> builder =
            ImmutableMap.builder();



    //捕获cus的异常到参数中
    @ExceptionHandler(CustomException.class)
    @ResponseBody//将模型转为json
    public ResponseResult customException(CustomException e){//可预知异常
//        开始记录
        logger.error("catch exception{}",e.getMessage());
        ResultCode resultCode = e.getResultCode();
        return new ResponseResult(resultCode);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult exception(Exception exception){//不可预知异常
        //记录日志
        logger.error("catch exception:{}",exception.getMessage());
        if(EXCEPTIONS == null)
            //创建异常map
            EXCEPTIONS = builder.build();
        //查看map有无对应异常
        final ResultCode resultCode = EXCEPTIONS.get(exception.getClass());
        final ResponseResult responseResult;
        if (resultCode != null) {
//            map有对应异常
            responseResult = new ResponseResult(resultCode);
        } else {
//            map无对应异常,统一返回系统繁忙
            responseResult = new ResponseResult(CommonCode.SERVER_ERROR);
        }
        return responseResult;
    }

    static{
        //在这里加入一些基础的异常类型判断,不可预知异常的map库
        builder.put(HttpMessageNotReadableException.class,CommonCode.INVALID_PARAM);
    }


}
