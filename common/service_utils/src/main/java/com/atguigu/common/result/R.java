package com.atguigu.common.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@ApiModel(value = "R",description = "公共返回结果类")
//公共的返回结果类
@AllArgsConstructor
@NoArgsConstructor
@Data
public class R {

    @ApiModelProperty("是否操作成功")
    private boolean success;
    @ApiModelProperty("自定义状态码，20000-成功  20001-失败")
    private Integer code;//20000-成功  20001-失败
    @ApiModelProperty("自定义描述信息")
    private String message;//提示
    @ApiModelProperty("返回数据")
    private Map<String,Object> data = new HashMap<>();//返回的数据


    public static R ok(){
        R r = new R();
        r.setSuccess(true);
        r.setCode(ResultCode.SUCCESS);
        r.setMessage("操作成功");
        return r;//data为空
    }

    public static R error(){
        R r = new R();
        r.setSuccess(false);
        r.setCode(ResultCode.ERROR);
        r.setMessage("操作失败");
        return r;//data为空
    }

    //通过哪个r对象调用的success方法，最后返回当前r对象
    public R success(boolean success){
        this.setSuccess(success);
        return this;
    }

    public R message(String message){
        this.setMessage(message);
        return this;
    }

    public R code(Integer code){
        this.setCode(code);
        return this;
    }

    public R data(Map<String,Object> data){
        this.setData(data);
        return this;
    }
    public R data(String key,Object value){
        this.getData().put(key,value);
        return this;
    }



    public static void main1(String[] args) {
        R r = R.ok();
        R success = r.success(false);
        System.out.println(r==success);
    }

    public static void main(String[] args) {
        R r = R.ok();
        r.data("name","tom");
        System.out.println(r);
//        Exception in thread "main" java.lang.NullPointerException
//        at com.atguigu.yygh.util.result.R.data(R.java:58)
//        at com.atguigu.yygh.util.result.R.main(R.java:72)
    }

}
