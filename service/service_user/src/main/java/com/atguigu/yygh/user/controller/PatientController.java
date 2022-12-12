package com.atguigu.yygh.user.controller;


import com.atguigu.common.result.R;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.service.PatientService;
import com.atguigu.yygh.user.util.AuthContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/user/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    //添加就诊人
    @PostMapping("/auth/save")
    public R savePatient(@RequestBody Patient patient,HttpServletRequest request){

        Long userId = AuthContextHolder.getUserId(request);

        patient.setUserId(userId);

        patientService.save(patient);

        return R.ok();
    }

    //获取就诊人列表
    @GetMapping("auth/findAll")
    public R findAll(HttpServletRequest request){

        Long userId = AuthContextHolder.getUserId(request);

        List<Patient> list =patientService.findListByUserId(userId);

        return R.ok().data("list",list);
    }

    //根据id获取就诊人信息
    @GetMapping("auth/get/{id}")
    public R getPatient(@PathVariable Long id){
       Patient patient = patientService.getPatientById(id);
        return R.ok().data("patient",patient);
    }

    //修改就诊人
    @PostMapping("auth/update")
    public  R updatePatient(@RequestBody Patient patient){
        patientService.updateById(patient);
        return R.ok();
    }

    //删除就诊人
    @DeleteMapping("auth/remove/{id}")
    public R removePatient(@PathVariable Long id){
        patientService.removeById(id);
        return R.ok();
    }

    //封装Feign 获取Patient
    @GetMapping("inner/get/{id}")
    public Patient getPatientById( @PathVariable("id") Long id) {
        return patientService.getById(id);
    }

}
