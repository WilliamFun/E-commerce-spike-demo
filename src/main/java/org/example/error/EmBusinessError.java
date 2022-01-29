package org.example.error;

public enum EmBusinessError implements CommonError{
    //通用错误类型00001
    PARAMETER_VALIDATION_ERROR(00001,"参数不合法"),
    //1开头为用户信息相关错误
    USER_NOT_EXIST(10001,"用户不存在"),



    ;

    EmBusinessError(int errCode, String errMsg){
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    private int errCode;
    private String errMsg;

    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }
}
