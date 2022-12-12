package com.atguigu.yygh.user.service.impl;

import com.atguigu.common.handler.YyghException;
import com.atguigu.common.utils.JwtHelper;
import com.atguigu.yygh.enums.AuthStatusEnum;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.mapper.UserInfoMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    
    @Autowired
    PatientService patientService;



    @Override
    public Map<String, Object> login(LoginVo loginVo) {

        String openid = loginVo.getOpenid();

        //1、获取手机号和验证码
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        if(StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)){
            throw new YyghException(20001,"参数为空");
        }

        //2、验证码是否正确 TODO
        /* 根据手机号，查询正确的验证码，和code比较
        从redis中根据phone，获取code
       code_redis = redisTemplate.boundValueOps(phone).get();
       if(!code_redis.equals(code)){
           throw new YyghException(20001,"验证码不正确");
       }
       */

        String code_redis = stringRedisTemplate.boundValueOps(phone).get();

        if(!code.equals(code_redis)){
            throw new YyghException(20001,"验证码不正确");
        }


        //3、从数据库中查询phone



        if (StringUtils.isEmpty(openid)){

            UserInfo userInfo = this.getUserInfoByPhone(phone);

            if(userInfo==null){
                //4、自动注册
                userInfo = new UserInfo();
                userInfo.setPhone(phone);
                userInfo.setAuthStatus(0);//未认证
                userInfo.setStatus(1);//正常
                userInfo.setCreateTime(new Date());
                userInfo.setUpdateTime(new Date());
                baseMapper.insert(userInfo);
            }

            //5、判断用户的锁定状态
            Integer status = userInfo.getStatus();
            if(status==0){
                throw new YyghException(20001,"用户已被锁定");
            }


            //验证码校验通过并且用户的状态为非锁定，就能说明已经登录成功
            //6、登录成功，封装返回值
            return this.get(userInfo);


        }else {
            //绑定手机号
            UserInfo userInfo = this.getUserInfoByOpenId(openid);
            //手机号在数据库中是否存在
            UserInfo userInfoByPhone = this.getUserInfoByPhone(phone);

            if (userInfoByPhone == null) {
                userInfo.setPhone(phone);
                userInfo.setUpdateTime(new Date());
                this.updateById(userInfo);
            }else {
                //合并数据
                userInfo.setName(userInfoByPhone.getName());
                userInfo.setCertificatesType(userInfoByPhone.getCertificatesType());
                userInfo.setCertificatesNo(userInfoByPhone.getCertificatesNo());
                userInfo.setCertificatesUrl(userInfo.getCertificatesUrl());
                userInfo.setAuthStatus(userInfoByPhone.getAuthStatus());
                userInfo.setUpdateTime(new Date());
                userInfo.setStatus(userInfoByPhone.getStatus());
                userInfo.setPhone(phone);

                this.updateById(userInfo);
                this.removeById(userInfoByPhone.getId());
            }

            if (userInfo.getStatus()==0) {
                throw new YyghException(20001,"用户已被锁定");
            }

            return this.get(userInfo);

        }
    }
    /*
    *
    * 封装
    * */
    private Map get(UserInfo userInfo){
        Map<String,Object> map = new HashMap<>();

        String name = userInfo.getName();//真实姓名
        if(StringUtils.isEmpty(name)){
            name = userInfo.getNickName();//微信昵称
            if(StringUtils.isEmpty(name)){
                name = userInfo.getPhone();
            }
        }
        map.put("name",name);// name  nickname  phone
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token",token);

        return map;

    }



    @Override
    public UserInfo getUserInfoByOpenId(String openid) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid",openid);//手机号不能重复
        UserInfo userInfo = baseMapper.selectOne(queryWrapper);
        return userInfo;
    }

    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {

        //根据用户id查询用户信息
        UserInfo userInfo = baseMapper.selectById(userId);

        //设置认证信息
        userInfo.setName(userAuthVo.getName());
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        //认证状态-认证中
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        //进行信息更新
        baseMapper.updateById(userInfo);
    }




    @Override
    public void auth(Long id, Integer authStatus) {
        UserInfo userInfo = baseMapper.selectById(id);
        userInfo.setAuthStatus(authStatus);
        baseMapper.updateById(userInfo);
    }

    @Override
    public Map<String, Object> show(Long id) {
        UserInfo userInfo = baseMapper.selectById(id);
        this.packUserInfo(userInfo);

        List<Patient> list = patientService.findListByUserId(id);

        Map<String,Object> map = new HashMap<>();
        map.put("userInfo",userInfo);
        map.put("patientList",list);

        return map;
    }

    @Override
    public void lock(Long id, Integer status) {
        UserInfo userInfo = baseMapper.selectById(id);
        userInfo.setStatus(status);
        baseMapper.updateById(userInfo);
    }

    @Override
    public Page<UserInfo> userList(Integer page, Integer limit, UserInfoQueryVo userInfoQueryVo) {
        Page<UserInfo> userInfoPage = new Page<>(page,limit);

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();


        String keyword = userInfoQueryVo.getKeyword();//用户名
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin();
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd();


        if(!StringUtils.isEmpty(keyword)){
            queryWrapper.like("name",keyword);
        }
        if(!StringUtils.isEmpty(createTimeBegin)){
            queryWrapper.ge("create_time",createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)){
            queryWrapper.le("create_time",createTimeEnd);
        }


        Integer status = userInfoQueryVo.getStatus();
        Integer authStatus = userInfoQueryVo.getAuthStatus();

        if(!StringUtils.isEmpty(status)){
            queryWrapper.eq("status",status);
        }

        if(!StringUtils.isEmpty(authStatus)){
            queryWrapper.eq("auth_status",authStatus);
        }

        baseMapper.selectPage(userInfoPage,queryWrapper);


        userInfoPage.getRecords().forEach(userInfo -> {
            Integer status1 = userInfo.getStatus();
            Integer authStatus1 = userInfo.getAuthStatus();

            String authStatusString = AuthStatusEnum.getStatusNameByStatus(authStatus1);
            String statusString = status1==1?"正常":"锁定";

            userInfo.getParam().put("authStatusString",authStatusString);
            userInfo.getParam().put("statusString",statusString);

        });

        return userInfoPage;
    }

    private void packUserInfo(UserInfo userInfo) {
        Integer status = userInfo.getStatus();
        userInfo.getParam().put("statusString",status==1?"正常":"锁定");
        userInfo.getParam().put("authStatusString",AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));

//        String certificatesType = userInfo.getCertificatesType();
//        userInfo.getParam().put("",);
    }

    private UserInfo getUserInfoByPhone(String phone){
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone",phone);//手机号不能重复
        UserInfo userInfo = baseMapper.selectOne(queryWrapper);
        return userInfo;
    }
}
