package com.atguigu.yygh.cmn.service;

import com.atguigu.yygh.model.cmn.Dict;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface DictService extends IService<Dict> {
    /**
     * 根据id查询下级数据字典列表
     * @param id
     * @return
     * SELECT * FROM `dict` WHERE parent_id = 10000
     */
    List<Dict> findChildData(Long id);

    void exportData(HttpServletResponse response);

    void importData(MultipartFile file);

    public String getNameByValueAndDictCode(String value,String dictCode);

    List<Dict> findByDictCode(String dictCode);
}
