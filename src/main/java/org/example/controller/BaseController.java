package org.example.controller;

import org.example.error.BussinessException;
import org.example.error.EmBusinessError;
import org.example.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class BaseController {

    public static final String CONTENT_TYPE_FORMED = "application/x-www-form-urlencoded";//后端需要消费的CONTENT_TYPE名字（一般使用传统http的urlformedencoded方式）

    //定义exceptionhandler解决未被controller层吸收的exception
//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.OK)
//    @ResponseBody
//    public Object handlerException(HttpServletRequest request, Exception ex){
//        Map<String,Object> responseData = new HashMap<>();
//        if(ex instanceof BussinessException){
//            BussinessException bussinessException = (BussinessException) ex;
//            responseData.put("errCode",bussinessException.getErrCode());
//            responseData.put("errMsg",bussinessException.getErrMsg());
//        }else{
//            responseData.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrCode());
//            responseData.put("errMsg",EmBusinessError.UNKNOWN_ERROR.getErrMsg());
//        }
//        return CommonReturnType.create(responseData,"fail");
//    }

}
