package com.atguigu.yygh.hosp;


import com.atguigu.yygh.hosp.repository.DeparymentRepository;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
public class DepartmentTest {

    @Autowired
    DeparymentRepository deparymentRepository;

    @Test
    public void test1(){

        List<Department> list = deparymentRepository.findByHoscode("10000");
        System.out.println(list.size());
        //list集合 统计,他们来自哪一个大的科室

        //jdk-api  list按照bigCode进行分组
        //bigCode相同的小科室为一组
        //Key 大科室bigcode
        //value 表示bigcode相同的小科室集合
        Map<String, List<Department>> map = list.stream().collect(
                Collectors.groupingBy(Department::getBigcode)
        );

       // System.out.println(map);

        //map中有几组key-value，就说明有几个大科室

        List<DepartmentVo> bigDepartmentList = new ArrayList<>();
        for (Map.Entry<String, List<Department>> entry : map.entrySet()) {
            String key = entry.getKey();//大科室编号
            List<Department> value = entry.getValue();//当前大科室下的小科室集合

            DepartmentVo bigDepartment = new DepartmentVo();
            bigDepartment.setDepcode(key);//大科室编号
            bigDepartment.setDepname(value.get(0).getBigname());//大科室名称

//            List<Department> value  --->  List<DepartmentVo> children;
            List<DepartmentVo> children = new ArrayList<>();
            value.forEach(department -> {
                DepartmentVo smallDepartment = new DepartmentVo();
                smallDepartment.setDepcode(department.getDepcode());//小科室编号
                smallDepartment.setDepname(department.getDepname());//小科室名称
//                smallDepartment.setChildren();小科室没有下级
                children.add(smallDepartment);
            });
            bigDepartment.setChildren(children);//
            bigDepartmentList.add(bigDepartment);
        }
        System.out.println(bigDepartmentList);

    }

}
