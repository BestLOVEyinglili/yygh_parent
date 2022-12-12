package com.atguigu.yygh.hosp.controller;


import com.atguigu.common.result.R;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalSetQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;


@Slf4j
@Api(description = "医院设置接口")
//@Api(description = "医院设置接口")
//@CrossOrigin  //跨域
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
public class HospitalSetController {

    @Autowired
    private HospitalSetService hospitalSetService;
    /*
    * 根据医院设置获取每个医院不同的地址 Url
    * */

    @GetMapping("getHospApiUrl/{hoscode}")
    public String getHospApiUrl(@PathVariable String hoscode){
        return hospitalSetService.getHospApiUrl(hoscode);

    }

    //查询所有医院的设置
   /* @GetMapping("findAll")
    public List<HospitalSet> findAll(){
        List<HospitalSet> list = hospitalSetService.list();
        return list;
    }*/

    //查询所有医院的设置
    @ApiOperation(value="医院设置列表")
    @GetMapping("findAll")
    public R findAll(){
        //int i = 1/0;
        List<HospitalSet> list = hospitalSetService.list();
        return R.ok().data("list",list);
    }

    @ApiOperation(value = "根据id查询一个医院设置")  //url?key=value  request ,   url/1/1/1  path
    @GetMapping("getHospSet/{id}")  //name=参数名  value=参数作用
    public R findById(@ApiParam(name = "id", value = "医院设置id主键", required = true) @PathVariable Long id) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return R.ok().data("item", hospitalSet);
    }
//    @GetMapping("getHospSet/{id}")
//    public R getById(@PathVariable("id") Long id){
//        HospitalSet hospitalSet = hospitalSetService.getById(id);
//        return R.ok().data("item", hospitalSet);
//    }

    /*//逻辑删除
    @DeleteMapping("{id}")
    public boolean removeById(@PathVariable Long id){
        return hospitalSetService.removeById(id);
    }*/

    @ApiOperation(value = "医院设置删除")
    @DeleteMapping("{id}")
    public R removeById(@ApiParam(name = "id",value = "医院设置编号",required = true)
                        @PathVariable String id){
        Boolean b= hospitalSetService.removeById(id);
        return R.ok();
    }

    /*
    * 分页查询
    * */
   /* @GetMapping("{page}/{limit}")
    public R pageList(@PathVariable("page") Long page,@PathVariable("limit") Long limit)
    {
        Page<HospitalSet> pageParam = new Page<>(page, limit);
        hospitalSetService.page(pageParam,null);

        List<HospitalSet> rows = pageParam.getRecords();
        long total = pageParam.getTotal();

        return  R.ok().data("total",total).data("rows",rows);


    }*/

    /*
    * 描述;条件查询
    *
    * */
    @PostMapping("{page}/{limit}")
    public R pageQuery(@PathVariable("page") Long page, @PathVariable("limit") Long limit,
                       @RequestBody HospitalSetQueryVo hospitalSetQueryVo){

        Page<HospitalSet> pageParam = new Page<>(page, limit);

        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();

        if(hospitalSetQueryVo!=null){
            String hosname = hospitalSetQueryVo.getHosname();
            String hoscode = hospitalSetQueryVo.getHoscode();
            if (!StringUtils.isEmpty(hosname)){
                queryWrapper.like("hosname",hosname);
            }
            if (!StringUtils.isEmpty(hoscode)){
                queryWrapper.eq("hoscode",hoscode);
            }
        }

        hospitalSetService.page(pageParam,queryWrapper);

        List<HospitalSet> rows = pageParam.getRecords();
        long total = pageParam.getTotal();

        return R.ok().data("total",total).data("rows",rows);

    }

    /*
    * 开通医院设置
    * */
    @PostMapping("saveHospSet")
    public  R save(@RequestBody HospitalSet hospitalSet){

        //设置状态, 1-正常  0-锁定
        hospitalSet.setStatus(1);
        hospitalSetService.save(hospitalSet);

        return  R.ok();
    }

    /*
    * 根据id修改
    * */
    @PostMapping("updateById")
    public  R updateById(@RequestBody HospitalSet hospitalSet){
        hospitalSetService.updateById(hospitalSet);
        return R.ok();
    }

    @PostMapping("updateHospSet")
    public R updateHospSet(@RequestBody HospitalSet hospitalSet) {
        hospitalSetService.updateById(hospitalSet);
        return R.ok().message("修改成功");
    }

    /*
    * 描述:批量删除
    * */
    @DeleteMapping("batchRemove")
    public R batchRemove(@RequestBody List<Long> idList){
        hospitalSetService.removeByIds(idList);
        return R.ok();
    }

    /*
    * 描述:锁定和解锁
    * */
    @PutMapping("lockHospitalSet/{id}/{status}")
    public R lockHospitalSet(@PathVariable("id") Long id,@PathVariable("status")Integer status){

        //根据id查询医院设置信息
        HospitalSet hospitalSet = hospitalSetService.getById(id);

        //设置状态
        hospitalSet.setStatus(status);
        hospitalSet.setUpdateTime(new Date());

        //调用方法
        hospitalSetService.updateById(hospitalSet);

        return R.ok();
    }
}
