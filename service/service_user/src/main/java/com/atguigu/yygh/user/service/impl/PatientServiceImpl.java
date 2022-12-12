package com.atguigu.yygh.user.service.impl;

import com.atguigu.yygh.feign.client.DictFeignClient;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.mapper.PatientMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;


@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {


    @Autowired
    DictFeignClient dictFeignClient;


    @Override   //获取就诊人列表
    public List<Patient> findListByUserId(Long userId) {
        QueryWrapper<Patient> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        List<Patient> patientList = baseMapper.selectList(wrapper);

        patientList.forEach(patient -> {
            this.packPatient(patient);
        });

        return patientList;
    }


    @Override
    //根据id获取就诊人信息
    public Patient getPatientById(Long id) {
        return this.packPatient(baseMapper.selectById(id));
    }



    private Patient packPatient(Patient patient) {
        String certificatesType = patient.getCertificatesType();//根据value查询就诊人证件类型的名称
        String contactsCertificatesType = patient.getContactsCertificatesType();//根据value查询联系人的证件类型名称

        String provinceCode = patient.getProvinceCode();
        String cityCode = patient.getCityCode();
        String districtCode = patient.getDistrictCode();

        //注意：证件类型，省市区   ， 他们的value是唯一的。

//            dictFeignClient.getName();

        String certificatesTypeString = dictFeignClient.getName(certificatesType);
        // /a/b/c/{id}
        // /a/b/c/ == 404
        String contactsCertificatesTypeString = "";
        if(!StringUtils.isEmpty(contactsCertificatesType)){
            contactsCertificatesTypeString = dictFeignClient.getName(contactsCertificatesType);
        }
        //其他的也可以判断

        String provinceString = dictFeignClient.getName(provinceCode);
        String cityString = dictFeignClient.getName(cityCode);
        String districtString = dictFeignClient.getName(districtCode);

        String fullAddress = provinceString + cityString + districtString + patient.getAddress();

        patient.getParam().put("certificatesTypeString",certificatesTypeString);
        patient.getParam().put("contactsCertificatesTypeString",contactsCertificatesTypeString);

        patient.getParam().put("provinceString",provinceString);
        patient.getParam().put("cityString",cityString);
        patient.getParam().put("districtString",districtString);

        patient.getParam().put("fullAddress",fullAddress);
        return patient;
    }
}