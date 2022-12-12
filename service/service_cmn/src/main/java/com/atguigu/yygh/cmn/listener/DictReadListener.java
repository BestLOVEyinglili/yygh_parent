package com.atguigu.yygh.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DictReadListener extends AnalysisEventListener<DictEeVo> {

    @Autowired
    DictMapper dictMapper;

    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        //1、dictEeVo---》dict
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictEeVo,dict);
        //2、mapper.insert(dict)
        dictMapper.insert(dict);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
