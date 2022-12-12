package com.atguigu.yygh.hosp.service.impl;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-cmn")
public interface DictFeignClient {

    @GetMapping("/admin/cmn/dict/getName/{value}")
    public String getName(@PathVariable String value);

    @GetMapping("/admin/cmn/dict/getName/{value}/{dictCode}")
    public String getName(@PathVariable String value,@PathVariable String dictCode);
}
