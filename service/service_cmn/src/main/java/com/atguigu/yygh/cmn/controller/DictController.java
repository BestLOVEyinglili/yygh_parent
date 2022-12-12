package com.atguigu.yygh.cmn.controller;

import com.atguigu.common.result.R;
import com.atguigu.yygh.cmn.service.DictService;

import com.atguigu.yygh.model.cmn.Dict;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/admin/cmn/dict")
//@CrossOrigin //跨域
public class DictController {

    @Autowired
    DictService dictService;

    @GetMapping("findByDictCode/{dictCode}")
    public R findByDictCode(@PathVariable String dictCode){

        List<Dict> list = dictService.findByDictCode(dictCode);
        return R.ok().data("list",list);

    }



    @GetMapping("/getName/{value}")
    public String getName(@PathVariable String value){
        return dictService.getNameByValueAndDictCode(value,"");

    }

    @GetMapping("/getName/{value}/{dictCode}")
    public String getName(@PathVariable String value
            ,@PathVariable String dictCode){
        return dictService.getNameByValueAndDictCode(value,dictCode);

    }

    /*
    * 根据id查询下级数据列表
    *
    * */

    @GetMapping("findChildData/{id}")
    public R findChildData(@PathVariable Long id){
        List<Dict> list = dictService.findChildData(id);
        return R.ok().data("list",list);
    }

    //导出（文件下载）
    @GetMapping("exportData")
    public void exportData(HttpServletResponse response){
        //文件下载是需要使用输出流
//        OutputStream outputStream = response.getOutputStream();
        dictService.exportData(response);
    }

    //参数=你选中的excel文件
    @PostMapping("importData")
    public R importData(MultipartFile file){
        dictService.importData(file);
        return R.ok();
    }

}