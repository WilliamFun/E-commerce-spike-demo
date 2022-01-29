package org.example.response;

public class CommonReturnType {
    private String status;
    //表明对应请求的返回处理结果“success”或“fail”：200-成功
    //使前端判定服务端有没有正确受理
    private Object data;
    //若status=success，则data内返回前端需要的Jason数据
    //若status=fail，则data内使用通用的错误码格式

    //定义一个通用的创建方法
    public static CommonReturnType create(Object result){
        return CommonReturnType.create(result,"success");
    }

    public static CommonReturnType create(Object result,String status){
        CommonReturnType type = new CommonReturnType();
        type.setData(result);
        type.setStatus(status);
        return type;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
