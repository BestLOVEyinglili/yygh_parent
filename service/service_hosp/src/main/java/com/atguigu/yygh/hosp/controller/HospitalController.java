package com.atguigu.yygh.hosp.controller;

import com.atguigu.common.result.R;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

//@CrossOrigin
@RestController
@RequestMapping("/admin/hosp/hospital")
public class HospitalController {

    @Autowired
    HospitalService hospitalService;

    //医院详情查看功能
    //根据id查看医院详情
    @GetMapping("show/{id}")
    public R show(@PathVariable String id){
        // map 中两组key-value  （ hospital + bookingRule ）
        Map<String,Object> map = hospitalService.show(id);// map = hospital +  bookingRule
        return R.ok().data("hospital",map);
//        return R.ok().data("hospital",hospitalService.show2(id));
    }


    //医院上下线功能
    @GetMapping("updateStatus/{id}/{status}")
    public R updateStatus(@PathVariable("id") String id, @PathVariable("status") Integer status){
        hospitalService.updateStatus(id, status);
        return R.ok();
    }

    //医院列表查询
    @GetMapping("{page}/{limit}")
    public R index(@PathVariable Integer page, @PathVariable Integer limit, /*@RequestBody*/ HospitalQueryVo hospitalQueryVo){

        Page<Hospital> pageResult = hospitalService.selectPage(page,limit,hospitalQueryVo);

        return R.ok().data("pages",pageResult);

    }
}
