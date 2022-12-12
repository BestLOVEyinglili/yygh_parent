package com.atguigu.yygh.oss.controller;

import com.atguigu.common.result.R;
import com.atguigu.yygh.oss.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/oss/file")
public class FileUploadController {

    @Autowired
    private FileService fileService;

    /**
     * 文件上传
     */
    @PostMapping("upload")
    public R upload(@RequestParam("file") MultipartFile file) {
        String uploadUrl = fileService.upload(file);
        return R.ok().message("文件上传成功").data("url", uploadUrl);
    }
}
