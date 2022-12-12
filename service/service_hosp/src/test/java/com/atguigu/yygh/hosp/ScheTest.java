package com.atguigu.yygh.hosp;

import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest
public class ScheTest {

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Test
    public void test1(){

        //查询某个医院，某个科室下的排班，按照workDate字段进行分组

        List<Schedule> list = scheduleRepository.findByHoscodeAndDepcode("10000", "200048585");
        System.out.println(list);

        //来自于哪些不同的日期
        Map<Date, List<Schedule>> collect = list.stream().collect(
                Collectors.groupingBy(Schedule::getWorkDate)
        );
        System.out.println(collect);

        //页面上遍历日期
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        for (Map.Entry<Date, List<Schedule>> entry : collect.entrySet()) {
            Date workDate = entry.getKey();
            List<Schedule> scheduleList = entry.getValue();

            //该日期下的排班列表的号源数量总和
            Integer reservedNumber = 0 ;
            Integer availableNumber = 0 ;
            for (Schedule schedule : scheduleList) {
                reservedNumber+=schedule.getReservedNumber();
                availableNumber+=schedule.getAvailableNumber();
            }

            //医生数量=排班数量
            Integer docCount = scheduleList.size();//该日期下的排班数量

            //星期几
            String dayOfWeek = this.testDayOfWeek(workDate);


            //日期的小方块
            BookingScheduleRuleVo bookingScheduleRuleVo = new BookingScheduleRuleVo();
            bookingScheduleRuleVo.setWorkDate(workDate);
            bookingScheduleRuleVo.setWorkDateMd(workDate);
            bookingScheduleRuleVo.setDocCount(docCount);
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
            bookingScheduleRuleVo.setReservedNumber(reservedNumber);
            bookingScheduleRuleVo.setAvailableNumber(availableNumber);

            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);

        }

        System.out.println(bookingScheduleRuleVoList);

    }

    //@Test
    public String testDayOfWeek(Date date){

        List<String> strings = Arrays.asList("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日");

//        Date date = new Date();//今天的日期
        DateTime dateTime = new DateTime(date);
        int dayOfWeek = dateTime.getDayOfWeek();//1-星期一   2-星期二

        String s = strings.get(dayOfWeek - 1);
        System.out.println(s);
        return s;
    }

    @Test
    public void test2(){
        String hoscode = "10000";
        String depcode = "200048585";

        Long page = 2L;
        Long size = 2L;

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
                Aggregation.skip((page-1)*size),
                Aggregation.limit(size)
        );

        AggregationResults<BookingScheduleRuleVo> aggregate =
                //聚合统计的是Schedule数据，每一组的结果封装了BookingScheduleRuleVo对象
                mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);

        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggregate.getMappedResults();

        bookingScheduleRuleVoList.forEach(bookingScheduleRuleVo -> {
            bookingScheduleRuleVo.setDayOfWeek(this.testDayOfWeek(bookingScheduleRuleVo.getWorkDate()));
//            bookingScheduleRuleVo.setWorkDateMd(bookingScheduleRuleVo.getWorkDate());
        });

        System.out.println(bookingScheduleRuleVoList);

    }



    @Test
    public void test3(){

        String hoscode = "10000";
        String depcode = "200048585";

        //查询排班的条件
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);

        //mg的聚合查询
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );

        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);

        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggregate.getMappedResults();

        System.out.println(bookingScheduleRuleVoList.size());
    }


}
