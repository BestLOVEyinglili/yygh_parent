package com.atguigu.yygh.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.atguigu.yygh.cmn.entity.Stu;


public class StuReadListener extends AnalysisEventListener<Stu> {

    @Override
    public void invoke(Stu stu, AnalysisContext analysisContext) {
        //逐行读取excel表中的数据
        //参数1：就是读取到的一行数据转换成的stu对象
        System.out.println(stu);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        //所有行读取完成后，执行
    }
}
