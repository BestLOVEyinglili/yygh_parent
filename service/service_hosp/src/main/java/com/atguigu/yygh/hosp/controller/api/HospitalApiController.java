package com.atguigu.yygh.hosp.controller.api;


import com.atguigu.common.result.R;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//用户系统使用的
@RestController
@RequestMapping("/api/hosp/hospital")
public class HospitalApiController {

    @Autowired
    HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;


    //查询首页医院列表
    @GetMapping("{page}/{limit}")
    public R index(@PathVariable Integer page, @PathVariable Integer limit, HospitalQueryVo hospitalQueryVo){

        Page<Hospital> hospitalPage = hospitalService.selectPage(page, limit, hospitalQueryVo);
        return R.ok().data("pages",hospitalPage);

    }

    //名称模糊查询
    @GetMapping("findByHosname/{hosname}")
    public R findByHosname(@PathVariable String hosname){
        List<Hospital> list = hospitalService.findByHosname(hosname);
        return R.ok().data("list",list);

    }

    /*
    *  需要获取医院信息（医院基本信息、预约信息）。
    * */
    @GetMapping("{hoscode}")
    public R item(@PathVariable("hoscode") String hoscode) {
        String id = hospitalService.getByHoscode(hoscode).getId();
        Map<String, Object> map = hospitalService.show(id);
        return R.ok().data(map);
    }

    @GetMapping("department/{hoscode}")
    public R index( @PathVariable String hoscode ) {
        List<DepartmentVo> list = departmentService.findDeptTree(hoscode);
        return R.ok().data("list",list);
    }

    /*
    * 描述:预约挂号详情接口
    * */
    //日期列表
    @GetMapping("auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public R getBookingSchedule(@PathVariable Integer page,
                                @PathVariable Integer limit,
                                @PathVariable String hoscode,
                                @PathVariable String depcode)
    {
        Map<String,Object> map =scheduleService.getBookingScheduleRule(page,limit,hoscode,depcode);
        return R.ok().data(map);
    }

    /*
    *描述:根据医院编号+科室编号+日期 查询排班列表
    *
    * */
    @GetMapping("auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public R findScheduleList(
            @PathVariable String hoscode,
            @PathVariable String depcode,
            @PathVariable String workDate
    ){
        List<Schedule> scheduleList=scheduleService.getDetailSchedule(hoscode, depcode, workDate);
        return R.ok().data("scheduleList",scheduleList);
    }

    /*
    * 描述:根据id获取排班
    * */
    @GetMapping("getSchedule/{id}")
    public R getSchedule(@PathVariable String id){
        Schedule schedule = scheduleService.getById(id);
        return R.ok().data("schedule",schedule);
    }


    //封装Feign 获取ScheduleOrderVo
    @GetMapping("inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId) {
        return scheduleService.getScheduleOrderVo(scheduleId);
    }


}

