package com.atguigu.yygh.hosp.controller.api;


//这个apiController是给医院用的

import com.atguigu.common.handler.YyghException;
import com.atguigu.common.result.Result;
import com.atguigu.common.utils.HttpRequestHelper;
import com.atguigu.yygh.hosp.repository.DeparymentRepository;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/hosp/")
//任何一种接口,都已一下方式获取参数
public class apiController {
    @Autowired
    HospitalService hospitalService;
    @Autowired
    DepartmentService departmentService;

    @Autowired
    ScheduleService scheduleService;

    /*
    * 删除排班
    * */
    @PostMapping("schedule/remove")
    public Result scheduleRemove(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String)map.get("hoscode");
        String hosScheduleId = (String)map.get("hosScheduleId");
        scheduleService.remove(hoscode,hosScheduleId);
        return Result.ok();
    }

    /*
    * 查询排班
    * */
    @PostMapping("schedule/list")
    public Result schedule(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());

        //查询某个医院某个科室下的排班列表
        String hoscode = (String) map.get("hoscode");
        String depcode = (String) map.get("depcode");

        if(StringUtils.isEmpty(hoscode)||StringUtils.isEmpty(depcode)){
            throw new YyghException(20001,"参数为空");
        }

        int page = Integer.parseInt((String) map.get("page"));
        int limit = Integer.parseInt((String) map.get("limit"));

        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        scheduleQueryVo.setDepcode(depcode);

        Page<Schedule> pageResult = scheduleService.selectPage(page,limit,scheduleQueryVo);

        return Result.ok(pageResult);
    }

    /*
    *
    * 上传排班
    * */
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request){
        //上传排班和上传科室写法一样
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        scheduleService.save(map);
        return Result.ok();
    }



    /**
     * 删除科
     */
    @PostMapping("department/remove")
    public Result remove(HttpServletRequest request){
        //1、统一获取参数
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        //2、解析参数
        String hoscode = (String)map.get("hoscode");
        String depcode = (String)map.get("depcode");
        String sign = (String)map.get("sign");

        if (StringUtils.isEmpty(hoscode)||StringUtils.isEmpty(depcode)){
            throw new YyghException(20001,"医院编号和科室编号不能为空");
        }

        departmentService.remove(hoscode,depcode);
        return Result.ok();
    }





    /*
    * 查询科室列表 带条件,带分页的,从mg中
    *
    * */
    @PostMapping("department/list")
    public Result department(HttpServletRequest request){
        //1、统一获取参数
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());

        //2、解析参数
        String hoscode = (String)map.get("hoscode");
        int page = Integer.parseInt((String) map.get("page"));
        int limit = Integer.parseInt((String) map.get("limit"));
//        Department department = new Department();
//        department.setHoscode(hoscode);// where hoscode = ?
        //3、封装查询条件DepartmentQueryVo
        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);
        //4、service方法
        Page<Department> pageResult = departmentService.selectPage(page, limit, departmentQueryVo);
        return Result.ok(pageResult);
    }


    /*@PostMapping("department/list")
    public Result chaxun(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String)map.get("hoscode");
        int page = Integer.parseInt((String) map.get("page"));
        int limit = Integer.parseInt((String) map.get("limit"));
        //降序分页
        Pageable pageable = PageRequest.of(page-1,limit, Sort.by(Sort.Direction.DESC,"createTime"));
        Department department = new Department();
        department.setHoscode(hoscode);
        //若希望模糊查询
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withIgnoreCase(true)
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<Department> example = Example.of(department);
        deparymentRepository.findAll();
        return Result.ok();

    }*/







    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());

        departmentService.save(map);
        return Result.ok();
    }


    /*
    * 开通医院设置服务
    * */
    @PostMapping("/saveHospital")
    public Result aa(HttpServletRequest request){
     /*  自己定义的
      //获取医院端传过来的参数
        //将这些参数封装成hosptal对象
        //传进MongoDB中
        //String key = request.getParameter("hoscode");
        //批量获取医院端的参数  但是value1是string数组String[]
        Map<String, String[]> parameterMap = request.getParameterMap();
        //Map<String, String[]> ==>转换成MAp<string,string>

       Map<String, String> map = new HashMap<>();

        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String key = entry.getKey();
            String[] value = entry.getValue();
            map.put(key,value[0]);
        }*/
        //调用工具类(参数获取)
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());

        hospitalService.save(map);
        return Result.ok();
    }

    /*
    * 上传科室列表
    *
    * */
    @PostMapping("hospital/show")
    public Result show(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());

        String hoscode = (String)map.get("hoscode");
        if (StringUtils.isEmpty(hoscode)){
            throw new YyghException(20001,"医院编号未传递");
        }
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        return Result.ok(hospital);
    }


}
