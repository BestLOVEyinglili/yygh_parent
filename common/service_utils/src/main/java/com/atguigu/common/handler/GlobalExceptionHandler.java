package com.atguigu.common.handler;

import com.atguigu.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ResponseBody
@ControllerAdvice
public class GlobalExceptionHandler {

    //出现了Exception类型的异常时，就会执行该方法
    @ExceptionHandler(value=Exception.class)
    public R exception(Exception ex){
        return R.error().message("出现了异常");
    }


    //实际出现的异常，距离哪个ExceptionHandler更近，就会执行该异常处理器方法
    //方法的参数，就是目前所出现的异常
    @ExceptionHandler(value=ArithmeticException.class)
    public R exception(ArithmeticException exp){
        return R.error().message(exp.getMessage());
    }


    @ExceptionHandler(value=YyghException.class)
    public R exception(YyghException exp){

        //打印指定级别的日志
//        log.debug();
//        log.warn();
//        log.error();  程序报错了

        log.error("出现了尚医通的自定义异常，" + exp.getMsg());

        return R.error().message(exp.getMsg());
    }

}
