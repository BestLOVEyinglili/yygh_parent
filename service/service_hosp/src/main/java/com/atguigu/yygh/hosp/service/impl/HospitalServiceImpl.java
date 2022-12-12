package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.handler.YyghException;
import com.atguigu.common.utils.MD5;
import com.atguigu.yygh.enums.DictEnum;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.BookingRule;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    HospitalRepository hospitalRepository;

    @Autowired
    HospitalSetService hospitalSetService;

    @Autowired
    DictFeignClient dictFeignClient;


    @Override
    public void save(Map<String, Object> map) {

        //1.签名的校验sign
        //医院端传递的签名是经过md5校验的
        String sign = (String)map.get("sign");
        if (StringUtils.isEmpty(sign)){
            throw new YyghException(20001,"签名为空");
        }

        //从表中根据hoscode的唯一性去查询签名key
        //但是此签名key并不是加密的,但是传来的是加密的
        //需要将从医院设置表中查询到的还医院的签名key,在代码中进行md5加密,之后进行比较
        String hoscode = (String)map.get("hoscode");
        if (StringUtils.isEmpty(hoscode)){
            throw new YyghException(20001,"医院编号不能为空");
        }

        String signKey = hospitalSetService.getSignKey(hoscode);
        if (!MD5.encrypt(signKey).equalsIgnoreCase(sign)){
           throw new YyghException(20001,"签名验证错误");
        }


        //2.校验通过之后
        //map转成hosptal对象
        //Hospital hospital = new Hospital();
        String jsonString = JSON.toJSONString(map);
        Hospital hospital = JSON.parseObject(jsonString, Hospital.class);

        //调用save方法新增还是修改
        //此时hosptal里边有id?
        //3.判断该医院信息是否存在mg(根据hoscode主键去查?X)
        Hospital hospital_mg = hospitalRepository.findByHoscode(hospital.getHoscode());

        if (hospital_mg==null){
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setStatus(1);//0:未上线  1:一上线
        }else{
            //数据库mg里有数据
            hospital.setId(hospital_mg.getId());
            hospital.setCreateTime(hospital_mg.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setStatus(1);
        }

        /*
        * save方法,既可以做新增,又可以做修改,有id修改,无id新增
        * */

        String logoData = hospital.getLogoData();//医院的图片,
        hospital.setLogoData(logoData.replaceAll(" ","+"));

        hospitalRepository.save(hospital);

    }

    @Override
    public Hospital getByHoscode(String hoscode) {

        //根据医院编号查询医院设置
        Hospital hospital = hospitalRepository.findByHoscode(hoscode);

        return hospital;
    }


    /*@Override
    public Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        //查询科室列表，查询排班列表，查询医院列表   实现方式一样的

        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo,hospital);

        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withIgnoreCase(true)
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<Hospital> example = Example.of(hospital,exampleMatcher);

        Pageable pageable = PageRequest.of(page-1,limit,Sort.by(Sort.Direction.DESC,"createTime"));
        Page<Hospital> pageResult = hospitalRepository.findAll(example,pageable);

        List<Hospital> content = pageResult.getContent();
        content.forEach(hosp->{
            //数据字典中的value值
            String hostype = hosp.getHostype();

            String provinceCode = hosp.getProvinceCode();
            String cityCode = hosp.getCityCode();
            String districtCode = hosp.getDistrictCode();
            //根据value查询数据字典名称
            //service_hosp 服务调用 service_cmn服务
//            dictFeignClient.getName(hostype,"Hostype");
            String hostypeString = dictFeignClient.getName(hostype, DictEnum.HOSTYPE.getDictCode());

            String provinceString = dictFeignClient.getName(provinceCode);
            String cityString = dictFeignClient.getName(cityCode);
            String districtString = dictFeignClient.getName(districtCode);

            hosp.getParam().put("hostypeString",hostypeString);
            hosp.getParam().put("fullAddress",provinceString+cityString+districtString+hosp.getAddress());

            //放到hospital中的param属性中
        });

        return pageResult;
    }*/

    @Override
    public Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        //查询科室列表，查询排班列表，查询医院列表   实现方式一样的
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo,hospital);

        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withIgnoreCase(true)
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<Hospital> example = Example.of(hospital,exampleMatcher);

        Pageable pageable = PageRequest.of(page-1,limit,Sort.by(Sort.Direction.DESC,"createTime"));
        Page<Hospital> pageResult = hospitalRepository.findAll(example,pageable);


        List<Hospital> content = pageResult.getContent();
        content.forEach(hosp -> {
            //数据字典中的value值
            String hostype = hosp.getHostype();
            String provinceCode = hosp.getProvinceCode();
            String cityCode = hosp.getCityCode();
            String districtCode = hosp.getDistrictCode();

            //根据value查询数据字典名称
            //远程调用
            //service_hosp调用service_cmn服务
            dictFeignClient.getName(hostype,"Hostype");
            String hostypeString = dictFeignClient.getName(hostype, DictEnum.HOSTYPE.getDictCode());

            String provinceString = dictFeignClient.getName(provinceCode);
            String cityString = dictFeignClient.getName(cityCode);
            String districtString = dictFeignClient.getName(districtCode);

            hosp.getParam().put("hostypeString",hostypeString);
            hosp.getParam().put("fullAddress",provinceString+cityString+districtString+hosp.getAddress());

            //放到hospital中的param属性中

        });
        return pageResult;
    }

    @Override
    public void updateStatus(String id, Integer status) {
        //1、根据id查询
        Hospital hospital = hospitalRepository.findById(id).get();
        //2、设置status和updateTime
        hospital.setStatus(status);
        hospital.setUpdateTime(new Date());
        //3、调用save方法
        hospitalRepository.save(hospital);
    }

    @Override
    public Map<String, Object> show(String id) {

        //根据id查询医院对象
        Hospital hospital = hospitalRepository.findById(id).get();
        this.packHospital(hospital);//param中赋值

        BookingRule bookingRule = hospital.getBookingRule();

        Map<String,Object> map = new HashMap<>();
        map.put("bookingRule",bookingRule);//医院的预约规则信息
        map.put("hospital",hospital);//医院的基本信息

        return map;
    }

    @Override
    public String getHospName(String hoscode) {
        Hospital byHoscode = hospitalRepository.findByHoscode(hoscode);
        return byHoscode.getHosname();
    }

    @Override
    public List<Hospital> findByHosname(String hosname) {

        return hospitalRepository.findHospitalByHosnameLike(hosname);
    }

    //实现方法


    private void packHospital(Hospital hosp) {
        //数据字典中的value值
        String hostype = hosp.getHostype();

        String provinceCode = hosp.getProvinceCode();
        String cityCode = hosp.getCityCode();
        String districtCode = hosp.getDistrictCode();
        //根据value查询数据字典名称
        //service_hosp 服务调用 service_cmn服务
//            dictFeignClient.getName(hostype,"Hostype");
        String hostypeString = dictFeignClient.getName(hostype, DictEnum.HOSTYPE.getDictCode());

        String provinceString = dictFeignClient.getName(provinceCode);
        String cityString = dictFeignClient.getName(cityCode);
        String districtString = dictFeignClient.getName(districtCode);

        hosp.getParam().put("hostypeString",hostypeString);//医院等级名称
        hosp.getParam().put("fullAddress",provinceString+cityString+districtString+hosp.getAddress());//完整地址

        //放到hospital中的param属性中
    }


}