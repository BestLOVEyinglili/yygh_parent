package com.atguigu.yygh.cmn.test;


import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.cmn.entity.Stu;
import com.atguigu.yygh.cmn.listener.StuReadListener;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class EasyExcelTest {

    //写数据（创建一个excel文档）
    @Test
    public void test1(){

        // excel文档中有三行学生对象
        Stu stu1 = new Stu(101, "张三1");
        Stu stu2 = new Stu(102, "张三2");
        Stu stu3 = new Stu(103, "张三3");

        List<Stu> list = Arrays.asList(stu1, stu2, stu3);

        //sheet方法，用来指定sheet表的名字，默认Sheet1
//        EasyExcel.write("C:\\Users\\70208\\Desktop\\尚硅谷学生列表.xlsx",Stu.class).sheet().doWrite(list);

        try {
            OutputStream outputStream = new FileOutputStream(new File("C:\\Users\\70208\\Desktop\\尚硅谷学生列表1.xlsx"));
            EasyExcel.write(outputStream,Stu.class).sheet().doWrite(list);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test2() throws FileNotFoundException {

        //参数1：要读取的excel （string路径，inputstream输入流）
        //参数2：class类型
        //参数3：负责逐行读取的  读监听器
//        EasyExcel.read("C:\\Users\\70208\\Desktop\\尚硅谷学生列表.xlsx",Stu.class,new StuReadListener())
//                .sheet()
//                .doRead();

        EasyExcel.read(new FileInputStream("C:\\Users\\70208\\Desktop\\尚硅谷学生列表.xlsx"),Stu.class,new StuReadListener())
                .sheet()
                .doRead();

    }
}
