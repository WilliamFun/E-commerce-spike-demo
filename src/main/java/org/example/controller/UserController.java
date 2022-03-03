package org.example.controller;

import com.alibaba.druid.util.StringUtils;
import org.apache.coyote.Response;
import org.apache.tomcat.util.security.MD5Encoder;
import org.example.controller.viewobject.UserVO;
import org.example.error.BussinessException;
import org.example.error.EmBusinessError;
import org.example.response.CommonReturnType;
import org.example.service.UserService;
import org.example.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller("user")//Controller标记用于被Spring扫描到
@RequestMapping("/user")//在URL上的访问路径
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*",originPatterns = "*")//实现跨域请求，主要session共享
public class UserController extends BaseController{

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;//可以满足多个用户并发使用（原理？）

    @Autowired
    private RedisTemplate redisTemplate;

    //用户登录接口
    @RequestMapping(value = "/login",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})//映射到http的post请求
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telphone")String telphone,
                                  @RequestParam(name = "password")String password,HttpServletRequest request, HttpServletResponse response) throws BussinessException, NoSuchAlgorithmException {
        //入参校验
        if(org.apache.commons.lang3.StringUtils.isEmpty(telphone)|| org.apache.commons.lang3.StringUtils.isEmpty(password)){
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }



        //用户登陆服务，用来校验用户登录是否合法
        UserModel userModel = userService.validatelogin(telphone,this.EncodeByMd5(password));

        //将登陆凭证加入到用户登陆成功的session内（假设单点session登录）
        //修改成若用户登录验证成功后将对应的登录信息和登录凭证一起存入redis


        //生成登录凭证token，UUID（保证用户登录凭证唯一性）
        String uuidToken = UUID.randomUUID().toString();
        uuidToken = uuidToken.replace("-","");

        //建立token和用户登录态之间的联系
        redisTemplate.opsForValue().set(uuidToken,userModel);
        //设置超时时间1小时
        redisTemplate.expire(uuidToken,1, TimeUnit.HOURS);

//        this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
//        this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);

        //实现给予cookie的session共享
//        ResponseCookie cookie = ResponseCookie.from("JSESSIONID", request.getSession().getId() ) // key & value
//                .httpOnly(true)       // 禁止js读取
//                .secure(true)     // 在http下也传输
//                .domain("localhost")// 域名
//                .path("/")       // path
//                .sameSite("None")  // 大多数情况也是不发送第三方 Cookie，但是导航到目标网址的 Get 请求除外
//                .build()
//                ;
//        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        //下发token
        return CommonReturnType.create(uuidToken);



    }

    //用户注册接口
    @RequestMapping(value = "/register",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})//映射到http的post请求
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "telphone")String telphone,
                                     @RequestParam(name = "otpCode")String otpCode,
                                     @RequestParam(name = "name")String name,
                                     @RequestParam(name = "gender")Integer gender,
                                     @RequestParam(name = "age")Integer age,
                                     @RequestParam(name = "password")String password) throws BussinessException, NoSuchAlgorithmException {
        //验证手机号和对应otpCode相符合
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telphone);
        if(!StringUtils.equals(otpCode,inSessionOtpCode)){
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"短信验证码错误");
        }
        //用户的注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));
        userModel.setAge(age);
        userModel.setTelphone(telphone);
        userModel.setRegisterMode("byphone");
        userModel.setEncrptPassword(this.EncodeByMd5(password));//JDK自带MD5加密有问题，会为null
        userService.register(userModel);
        return CommonReturnType.create(null);


    }

    //实现MD5加密
    public String EncodeByMd5(String str) throws NoSuchAlgorithmException {
        //确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();
        //加密字符串
        String newstr = base64Encoder.encode(md5.digest(str.getBytes(StandardCharsets.UTF_8)));
        return newstr;
    }

    @RequestMapping(value = "/getotp",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})//映射到http的post请求
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name = "telphone")String telphone, HttpServletRequest request, HttpServletResponse response){
        //需要按照一定的规则生成OTP验证码
        //随机数
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);//取值[10000,109999)
        //将OTP验证码同对应用户的手机号关联(适合用Redis，暂时不用）
        //使用httpsession的方式绑定手机号与OTP验证码
        httpServletRequest.getSession().setAttribute(telphone,otpCode);

        //将OTP验证码通过短信通道发送给用户（省略，需要买第三方短信服务的通道，以httppost的方式将短信模板的内容post到对应用户的手机号）
        System.out.println("telephone = "+telphone+" otpcode = "+otpCode);

        //springboot高版本新加了samesite这个设置，需要降低其等级
        ResponseCookie cookie = ResponseCookie.from("JSESSIONID",request.getSession().getId())
                .httpOnly(true) //禁止js读取
                .secure(true) //http下也传输
                .domain("localhost") //域名
                .path("/") //
                .maxAge(3600) //过期时间（s)
                .sameSite("None")  //不发送第三方cookie
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE,cookie.toString());

        return CommonReturnType.create(null);

    }


    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name="id")Integer id) throws BussinessException {
        //调用service服务获取对应id的用户对象并返回给前端
        UserModel userModel = userService.getUserById(id);//不要把整个领域模型传给前端

        //若获取的对应用户信息不存在，抛出业务异常
        if(userModel==null){
            throw new NullPointerException();
            //throw new BussinessException(EmBusinessError.USER_NOT_EXIST);
        }

        //将核心领域模型用户对象转化为可供UI使用的viewobject
        UserVO userVO = convertFromModel(userModel);

        //返回通用对象
        return CommonReturnType.create(userVO);
    }
    private UserVO convertFromModel(UserModel userModel){//model->vo
        if(userModel==null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel,userVO);

        return userVO;
    }

}

