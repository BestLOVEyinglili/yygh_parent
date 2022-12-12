package com.atguigu.yygh.hosp.controller;


import com.atguigu.common.result.R;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/hosp/department")
public class DepartmentController {

    @Autowired
    DepartmentService departmentService;

    @GetMapping("getDeptList/{hoscode}")
    public R getDeptList(@PathVariable String hoscode){
        List<DepartmentVo> list= departmentService.findDeptTree(hoscode);
        return R.ok().data("list",list);

    }
}
