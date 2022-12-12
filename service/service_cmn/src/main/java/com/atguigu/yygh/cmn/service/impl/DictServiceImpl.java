package com.atguigu.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.cmn.listener.DictReadListener;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

//    @Autowired
//    DictMapper dictMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    DictReadListener dictReadListener;

    //value = "dict" 命名空间,可以用不同的空间缓存不同模块的缓存数据
    //key = "'cache_'+#id"      缓存数据key的生成规则
    //@Cacheable(value = "dict",key = "'cache_'+#id")
    //被该注解标注的方法被调用的时候,首先会从指定的命名空间下,检查该key的值是否存在,若存在直接返回,方法体不会执行
    //方法体若别执行,最后在retrue时,会自动将key和value自动存进redis缓存中
    @Cacheable(value = "dict",key = "'cache_'+#id")
    @Override
    public List<Dict> findChildData(Long id) {
        //SELECT * FROM `dict` WHERE parent_id = 10000
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id",id);//不是类中的属性名
        // baseMapper  ==  DictMapper
        List<Dict> dictList = baseMapper.selectList(queryWrapper);
        //遍历每一个dict，然后判断它是否有下级数据
        dictList.forEach(dict -> {
            boolean children = this.isChildren(dict);
            dict.setHasChildren(children);
        });
        return dictList;
    }


    /*
    * findChildData2BianChengShi()
    * 描述:根据id查询数据,redis中有直接返回,没有直接查询sql并且存入redis中
    * 手写的编程式方法
    * 上边的findChildData()是注解形式
    * */
   // @Override
    public List<Dict> findChildData2BianChengShi(Long id) {
        //手写:
        /*
        * 从redis缓存中查找,存在直接返回,否则去sql查询,并且将数据保存在redis中(id=redis中的key)
        * */
        String key = "dict_cache_"+id;//自定义的key的命名规则
        List<Dict> dictList_redis = (List<Dict>)redisTemplate.boundValueOps(key).get();
        if (dictList_redis!=null){
            System.out.println("从redis中加载的数据");
            return dictList_redis;//从redis中查询的结
        }

        System.out.println("从sql中查询到的数据");
        //SELECT * FROM `dict` WHERE parent_id = 10000
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id",id);//不是类中的属性名

        // baseMapper  ==  DictMapper
        List<Dict> dictList = baseMapper.selectList(queryWrapper);

        //遍历每一个dict，然后判断它是否有下级数据
        dictList.forEach(dict -> {
            boolean children = this.isChildren(dict);
            dict.setHasChildren(children);
        });

        //从sql中查询的数据添加到redis中
        redisTemplate.boundValueOps(key).set(dictList,5, TimeUnit.MINUTES);

        return dictList;
    }

    @Override
    public void exportData(HttpServletResponse response) {
        try {
            //1、文件下载必须要设置的响应头
            response.setContentType("application/vnd.ms-excel"); //指示响应内容的格式
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition",
                    "attachment;filename="+ URLEncoder.encode("数据字典", "UTF-8") + ".xlsx");//附件形式下载

            //2、查询所有的数据字典
//            baseMapper.selectList(null);
            List<Dict> list = this.list();

            //3、List<Dict> list==》List<DictEeVo>
            List<DictEeVo> dictEeVoList = new ArrayList<>();
            list.forEach(dict -> {
                DictEeVo dictEeVo = new DictEeVo();
//                dictEeVo.setId(dict.getId());
//                dictEeVo.setName(dict.getName());
//                dictEeVo.setDictCode(dict.getDictCode());
//                dictEeVo.setParentId(dict.getParentId());
                BeanUtils.copyProperties(dict,dictEeVo);
                dictEeVoList.add(dictEeVo);
            });

            //4、调用api方法
            EasyExcel.write(response.getOutputStream(),DictEeVo.class).sheet().doWrite(dictEeVoList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //导入方法
    /*
    *  @CacheEvict(value = "dict",allEntries = true,beforeInvocation = true)
    * 该注解表示后 调用该方法,清空dict命名空间下所有的缓存
    * allEntries = true 命名空间下所有的缓存
    * beforeInvocation = true 方法体执行之前
    * 连起来:在方法体执行前,要将dict命名空间下的所有缓存数据清空
    *此注解标注的方法,方法体每次都会执行
    * */
    @CacheEvict(value = "dict",allEntries = true,beforeInvocation = true)
    @Override
    public void importData(MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            EasyExcel.read(
                    inputStream,
                    DictEeVo.class,dictReadListener
            ).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNameByValueAndDictCode(String value, String dictCode) {

        if (StringUtils.isEmpty(dictCode)){
           //查询省市区,根据value查询
           QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
           queryWrapper.eq("value",value);

           Dict dict = baseMapper.selectOne(queryWrapper);
           return dict.getName();
       }else {
           //查询医院等级,根据value和dictCode去查询

            QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("value",value);
            queryWrapper.eq("parent_id",this.getDictByDictCode(dictCode).getId());

            Dict dict = baseMapper.selectOne(queryWrapper);
            return dict.getName();
        }

    }

    @Override
    public List<Dict> findByDictCode(String dictCode) {

        Dict dict = this.getDictByDictCode(dictCode);
        List<Dict> list = this.findChildData(dict.getId());

        return list;
    }

    private  Dict getDictByDictCode(String dictCode){
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dict_code",dictCode);
        Dict dict = baseMapper.selectOne(queryWrapper);
        return dict;
    }

    private boolean isChildren(Dict dict) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id",dict.getId());
        Integer integer = baseMapper.selectCount(queryWrapper);
        return integer>0;
    }

    //添加某一个数据字典
    /*
    * @CachePut(value = "dict",key = "'test_dict_'+#dict.id")
    * 被该方法标注,每次调用,方法体都会被执行
    * 执行完之后,每次都会向redis中存储一组key-value
    * key:'test_dict_'+#dict.id
    * value: dict对象
    * 每次调用,redis中的key都会更新成新的(覆盖)
    * */
    @CachePut(value = "dict",key = "'test_dict_'+#dict.id")
    public  Dict updateDict(Dict dict){
        baseMapper.updateById(dict);
        return dict;
    }
}
