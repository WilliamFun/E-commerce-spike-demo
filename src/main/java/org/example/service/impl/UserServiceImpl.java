package org.example.service.impl;

import org.example.dao.UserDOMapper;
import org.example.dao.UserPasswordDOMapper;
import org.example.dataobject.UserDO;
import org.example.dataobject.UserPasswordDO;
import org.example.service.UserService;
import org.example.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Override
    public UserModel getUserById(Integer id) {
        //调用userdomappper获取对应的用户object
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);//不可以把UserDo直接传给前端,通过model包装一下
        if(userDO==null){
            return null;
        }//对应用户不存在
        //通过用户id获得对应用户加密密码
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());

        return convertFromDataObject(userDO,userPasswordDO);

    }

    private UserModel convertFromDataObject(UserDO userDo, UserPasswordDO userPasswordDO){
        if(userDo==null){
            return null;
        }

        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDo,userModel);//将userdo的属性复制到usermodel

        if(userPasswordDO!=null){
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }

        return userModel;
    }
}
