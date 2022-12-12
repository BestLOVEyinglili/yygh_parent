package com.atguigu.yygh.hosp.service.impl;


import com.alibaba.fastjson.JSON;
import com.atguigu.common.handler.YyghException;
import com.atguigu.yygh.hosp.repository.DeparymentRepository;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.BookingRule;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import org.joda.time.DateTime;

import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    DepartmentService departmentService;


    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    DeparymentRepository departmentRepository;
    //这里有一个单词写错了 DeparymentRepository




    @Autowired
    HospitalService hospitalService;
 //   HospitalRepository hospitalRepository;

    @Override
    public void save(Map<String, Object> map) {
        Schedule schedule = JSON.parseObject(JSON.toJSONString(map), Schedule.class);

        //根据hoscode和hosScheduleId从mg中查询排班对象（唯一的）
        String hoscode = schedule.getHoscode();
        String hosScheduleId = schedule.getHosScheduleId();
        if(StringUtils.isEmpty(hoscode)||StringUtils.isEmpty(hosScheduleId)){
            throw new YyghException(20001,"医院编号和排班编号不能为空");
        }

        Schedule schedule_mg = scheduleRepository.findByHoscodeAndHosScheduleId(hoscode, hosScheduleId);

        if(schedule_mg==null){
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
//            schedule.setStatus(1);
        }else{
            schedule.setId(schedule_mg.getId());
            schedule.setCreateTime(schedule_mg.getCreateTime());
            schedule.setUpdateTime(new Date());
        }
        scheduleRepository.save(schedule);
    }


    @Override
    public Page<Schedule> selectPage(int page, int limit, ScheduleQueryVo scheduleQueryVo) {

        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo,schedule);//hoscode + depcode

        Example<Schedule> example =  Example.of(schedule);

        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page-1,limit,sort);

        Page<Schedule> pageResult = scheduleRepository.findAll(example, pageable);

        return pageResult;
    }

    @Override
    public void remove(String hoscode, String hosScheduleId) {
        Schedule schedule = scheduleRepository.findByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if(schedule!=null){
            scheduleRepository.deleteById(schedule.getId());
        }
    }

    @Override
    public Map<String, Object> getScheduleRule(long page, long limit, String hoscode, String depcode) {
       /*
       String hoscode = "10000";
        String depcode = "200048585";

        Long page = 2L;
        Long size = 2L;
        */

        //查询排班的条件
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);

        //mg的聚合查询
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria), //指定对哪些数据要进行聚合统计
                Aggregation.group("workDate") //按照workDate进行分组
                        .sum("reservedNumber").as("reservedNumber") //每一组的哪个字段进行就和,求和之后的结果赋值给 bookingScheduleRuleVo.setReservedNumber
                        .sum("availableNumber").as("availableNumber") //每一组的哪个字段进行就和,bookingScheduleRuleVo.setAvailableNumber(availableNumber)
                        .count().as("docCount") //该日期下的排班数量,bookingScheduleRuleVo.setDocCount(docCount)
                        //从这一组排班（workDate肯定是相同）中取出第一个排班的workDate
                        .first("workDate").as("workDate")  //bookingScheduleRuleVo.setWorkDate(workDate);
                        .first("workDate").as("workDateMd"),
                Aggregation.sort(Sort.Direction.ASC,"workDate"),
                Aggregation.skip((page-1)*limit),
                Aggregation.limit(limit)
        );

        AggregationResults<BookingScheduleRuleVo> aggregate =
                //聚合统计的是Schedule数据，每一组的结果封装了BookingScheduleRuleVo对象
                mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);

        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggregate.getMappedResults();

        bookingScheduleRuleVoList.forEach(bookingScheduleRuleVo -> {
            bookingScheduleRuleVo.setDayOfWeek(this.getDayOfWeek(bookingScheduleRuleVo.getWorkDate()));
//            bookingScheduleRuleVo.setWorkDateMd(bookingScheduleRuleVo.getWorkDate());
        });

        HashMap<String, Object> map = new HashMap<>();
        map.put("bookingScheduleRuleList",bookingScheduleRuleVoList);
        map.put("total",this.getTotal(hoscode,depcode));//总日期个数

        //医院名称
        //Hospital hospital = hospitalRepository.findByHoscode(hoscode);
        //String hosname = hospital.getHosname();
        String hosname = hospitalService.getHospName(hoscode);
        Map<String,Object> baseMap = new HashMap<>();
        baseMap.put("hosname",hosname);

        map.put("baseMap",baseMap);
        return map;
    }

    @Override
    public List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate) {
        //字符串转成date类型的
        DateTime dateTime = new DateTime(workDate);
        Date date = dateTime.toDate();

        List<Schedule> scheduleList = scheduleRepository.findByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, date);

        scheduleList.forEach(schedule -> {
            this.packSchedule(schedule);
        });

        return scheduleList;
    }


    /*
    * 描述:预约挂号详情接口
    * 日期列表实现类
    * */
    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        //1.获取 医院,预约规则
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        BookingRule bookingRule = hospital.getBookingRule();

        //2.获取可预约日期分页数据, 注意:排班数据是存储在MongoDB中的
        IPage iPage = this.getListDate(page,limit,bookingRule);

        //3.日期集合
        List<Date> dateList = iPage.getRecords();

        //4.对排班数据,按照workDate分组
        Criteria criteria = Criteria
                .where("hoscode").is(hoscode)
                .and("depcode").is(depcode)
                .and("workDate").in(dateList);

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
                        .first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("availableNumber").as("availableNumber")
                        .sum("reservedNumber").as("reservedNumber")

        );
        AggregationResults<BookingScheduleRuleVo> aggregationResults
                = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);

        List<BookingScheduleRuleVo> scheduleVoList = aggregationResults.getMappedResults();

        //5.List转成map
        Map<Date,BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();

        scheduleVoMap = scheduleVoList.stream().collect(
                Collectors.toMap(BookingScheduleRuleVo::getWorkDate,bookingScheduleRuleVo -> bookingScheduleRuleVo)
        );

        //6.deteList中每一个日期对应一个BookingScheduleRuleVo
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList =
                new ArrayList<>();
        for (int i = 0; i < dateList.size(); i++) {
            Date date =  dateList.get(i);

            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date);

            if (bookingScheduleRuleVo==null){
                //当天值班没有医生
                 bookingScheduleRuleVo = new BookingScheduleRuleVo();

                 bookingScheduleRuleVo.setWorkDate(date);
                 bookingScheduleRuleVo.setDocCount(0);
                 bookingScheduleRuleVo.setAvailableNumber(-1);  //-1表示没有号
                 bookingScheduleRuleVo.setReservedNumber(-1);

            }

            bookingScheduleRuleVo.setWorkDateMd(date);
            bookingScheduleRuleVo.setDayOfWeek(this.getDayOfWeek(date));


            //最后一页最后一条记录为即将预约。  0：正常, 1：即将放号，-1：当天已停止挂号
            if (page == iPage.getPages() && i==dateList.size()-1){
                bookingScheduleRuleVo.setStatus(1);//即将放号

            }else {
                bookingScheduleRuleVo.setStatus(0);//正常
            }

            //当天（第一页第一条） 如果过了停挂时间 status=-1 ，当天已停止挂号
            if (page == 1 && i==0){
                DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                if (stopTime.isBeforeNow()){
                    bookingScheduleRuleVo.setStatus(-1);//当天停止挂号
                }
            }

            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }
        // -----------------封装返回结果--------------------------
        Map<String, Object> result = new HashMap<>();
        Map<String, String> baseMap = new HashMap<>();

        baseMap.put("hosname", hospitalService.getHospName(hoscode));

        //获取大小科室
        baseMap.put("bigname", departmentRepository.findByHoscodeAndDepcode(hoscode,depcode).getBigname());
        baseMap.put("depname", departmentRepository.findByHoscodeAndDepcode(hoscode,depcode).getDepname());


        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        baseMap.put("stopTime", bookingRule.getStopTime());

        result.put("bookingScheduleList", bookingScheduleRuleVoList);
        result.put("total", iPage.getTotal());
        result.put("baseMap", baseMap);

        return result;
    }



    /*
    * 封装的方法
    *
    * */
    private IPage getListDate(Integer page, Integer limit, BookingRule bookingRule) {

        //预约周期
        Integer cycle = bookingRule.getCycle();

        //当天放号时间,yyyy-MM-dd HH:mm
        DateTime releaseTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());


        //如果当天的放号时间已经过,则预约周期后一天状态为即将放号,周期+1
        if (releaseTime.isBeforeNow()){
            cycle +=1;
        }

        //可预约所有日期,后一天显示即将放号倒计时
        ArrayList<Date> dateList = new ArrayList<>();

        for (Integer i = 0; i < cycle; i++) {
            DateTime curDateTime = new DateTime().plusDays(i);
            String dateString = curDateTime.toString("yyy-MM-dd");
            Date date = new DateTime(dateString).toDate();
            dateList.add(date);
        }

        //分页日期(date:所有的日期)
        List<Date> pageDateList = new ArrayList<>();
        int start= (page-1)*limit;
        int end = (page-1)*limit+limit;

        if (end>dateList.size()){
            end=dateList.size();
        }
        for (int i = start; i < end; i++) {
            pageDateList.add(dateList.get(i));
        }

        //利用Mp中的page封装返回值
        IPage<Date> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page(page, limit, dateList.size());
        iPage.setRecords(pageDateList);

        return iPage;


    }

    /*
    * 二层封装*/
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd")+" "+ timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }

    public Integer getTotal(String hoscode,String depcode){

       /*
       String hoscode = "10000";
        String depcode = "200048585";
        */

        //查询排班的条件
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);

        //mg的聚合查询
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );

        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);

        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggregate.getMappedResults();

        /*System.out.println(bookingScheduleRuleVoList.size());*/
        return bookingScheduleRuleVoList.size();
    }



    /*
    * 描述:获取星期api
    * */
    public String getDayOfWeek(Date date){
        List<String> strings = Arrays.asList("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日");
//        Date date = new Date();//今天的日期
        DateTime dateTime = new DateTime(date);
        int dayOfWeek = dateTime.getDayOfWeek();//1-星期一   2-星期二

        String s = strings.get(dayOfWeek - 1);
        System.out.println(s);
        return s;
    }


    private Schedule packSchedule(Schedule schedule) {

        String hoscode = schedule.getHoscode();//医院编号
        String hospName = hospitalService.getHospName(hoscode);

        String depcode = schedule.getDepcode();//科室编号
        Department department = departmentRepository.findByHoscodeAndDepcode(hoscode, depcode);
        String depname = department.getDepname();

        Date workDate = schedule.getWorkDate();
        String dayOfWeek = this.getDayOfWeek(workDate);

        schedule.getParam().put("hosname",hospName);//医院名称
        schedule.getParam().put("depname",depname);//科室名称
        schedule.getParam().put("dayOfWeek",dayOfWeek);//星期
        return schedule;
    }


    /*
    * 描述: 根据id获取排班
    * */
    @Override
    public Schedule getById(String id) {
        Schedule schedule = scheduleRepository.findById(id).get();
        return this.packSchedule(schedule);
    }


    //根据排班id获取预约下单数据（医院+排班数据）
    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();

        //1、查询排班
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        //2、查询预约规则
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        BookingRule bookingRule = hospital.getBookingRule();

        //3、赋值
        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());
        scheduleOrderVo.setHosname(hospitalService.getHospName(schedule.getHoscode()));
        // scheduleOrderVo.setDepname(departmentService.getDepName(schedule.getHoscode(), schedule.getDepcode()));
        /*
        * 可能有问题
        * */
        Department department = departmentRepository.findByHoscodeAndDepcode(schedule.getHoscode(), schedule.getDepcode());
        scheduleOrderVo.setDepname(department.getDepname());

        //退号截止天数（如：就诊前一天为-1，当天为0）
        int quitDay = bookingRule.getQuitDay();
        DateTime quitTime=this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());

        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //预约开始时间
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //预约截止时间
        DateTime endTime = this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //当天停止挂号时间
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStopTime(stopTime.toDate());

        return scheduleOrderVo;
    }
}
