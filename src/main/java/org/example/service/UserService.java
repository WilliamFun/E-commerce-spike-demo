package org.example.service;

import org.example.error.BussinessException;
import org.example.service.model.UserModel;

public interface UserService {
    //通过用户id获取用户对象的方法
    UserModel getUserById(Integer id);
    //service层用户注册
    void register(UserModel userModel) throws BussinessException;
    //service层用户登录
    /*
    telphone:用户注册的手机号
    encrptPassword：用户加密后的密码
     */
    UserModel validatelogin(String telphone,String encrptPassword) throws BussinessException;

}
