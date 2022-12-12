package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.handler.YyghException;
import com.atguigu.yygh.hosp.repository.DeparymentRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    DeparymentRepository deparymentRepository;

    @Autowired
    DeparymentRepository departmentRepository;//这俩不同,名,因为自己在写接口名字的时候写错了,将就着用


    @Override
    public void save(Map<String, Object> map) {

        //1.map转成department
        String string = JSON.toJSONString(map);
        Department department = JSON.parseObject(string, Department.class);
        //2.查询可是是否存在 hoscode+depcode 根据医院编号+科室变化查询某个医院下的科室
        String hoscode = department.getHoscode();
        String depcode = department.getDepcode();
        if (StringUtils.isEmpty(hoscode) || StringUtils.isEmpty(depcode)) {
            throw new YyghException(20001, "科室编号和医院编号不能为空");
        }
        Department department_mg = deparymentRepository.findByHoscodeAndDepcode(hoscode, depcode);

        //3.判断是否存在
        //判断该科室是否存在
        //存在==>更新  不存在==>新增

        if (department_mg == null) {
            //添加
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            deparymentRepository.save(department);
        } else {
            department.setId(department_mg.getId());
            department.setCreateTime(department_mg.getCreateTime());
            department.setUpdateTime(new Date());

        }
        deparymentRepository.save(department);


    }

    @Override
    public Page<Department> selectPage(int page, int limit, DepartmentQueryVo departmentQueryVo) {
        //1、departmentQueryVo转成Department
        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo, department);

        //2、创建分页对象
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createTime"));

        //3、模糊查询匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withIgnoreCase(true).withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        //4、example对象
        Example<Department> example = Example.of(department, exampleMatcher);

        //5、findAll
        Page<Department> pageResult = departmentRepository.findAll(example, pageable);

        return pageResult;
    }

    @Override
    public void remove(String hoscode, String depcode) {
        //        departmentRepository.removeByHoscodeAndDepcode(hoscode,depcode);
        Department department = departmentRepository.findByHoscodeAndDepcode(hoscode, depcode);
        if (department != null) {
            departmentRepository.deleteById(department.getId());
        }
    }

    @Override
    public List<DepartmentVo> findDeptTree(String hoscode) {

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
        return bigDepartmentList;

    }

    //实现方法：根据医院编号 和 科室编号获取科室数据

}
