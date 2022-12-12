package com.atguigu.yygh.hosp;


import com.atguigu.yygh.hosp.mgtest.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@SpringBootTest
public class MgTest {

    @Autowired
    MongoTemplate mongoTemplate;


    //添加
    //@GetMapping("create")
    @Test
    public void createUser() {
        User user = new User();
        user.setAge(28);
        user.setName("JOJO");
        user.setEmail("JOJO@163.com");

        mongoTemplate.insert(user);

        System.out.println(user);
    }


    //查询所有方法
    @Test
    public void findAll(){
        List<User> all = mongoTemplate.findAll(User.class);
        //集合
        System.out.println(all);
        //类
        all.forEach(user -> System.out.println(User.class) );
        //对象
        all.forEach(System.out::println);

    }

    //根据id进行查询
    @Test
    public void findById(){
        User userByid = mongoTemplate.findById("63840d075082fd7ee958e5ca", User.class);
        System.out.println(userByid);
    }

    //条件查询
    @Test
    public void findUserList() {
        // where name = "test" and age = 20
        Query query = new Query(
                Criteria.where("name").is("test").and("age").is(20)  //注意：age是整型，不要加“”
        );
        List<User> userList = mongoTemplate.find(query, User.class);
        userList.forEach(System.out::println);
    }

    @Test
    public void findUserList1() {
        // where name = "test" and age = 20
        Query query = new Query(
                Criteria.where("name").is("JOJO")
        );
        List<User> userList = mongoTemplate.find(query, User.class);
        userList.forEach(System.out::println);
    }

    //结果有多个的时候,只返回第一个
    @Test
    public void findUserOne() {
        // where name = "test" and age = 20
        Query query = new Query(
                Criteria.where("name").is("JOJO")
        );
        User one = mongoTemplate.findOne(query, User.class);
        System.out.println(one);
    }

    //模糊查询
   /* @Test
    public void TestLike(){
        Query query = new Query();
        //regex() 写模糊查询
        String name = "J";
        String regex = String.format("%s%s%s", "^.*", name, ".*$");
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        Query query = new Query(Criteria.where("name").regex(pattern));

        List<User> userList = mongoTemplate.find(query, User.class);

        System.out.println(userList);


    }*/

    //分页查询
    //分页两个参数有点迷糊
    @Test
    public void TestPage(){
        Query query = new Query();
        //skip(1) 跳过多少条
        //limit(1) 跳过5
        query.skip(1).limit(1);
        List<User> userList = mongoTemplate.find(query, User.class);
        userList.forEach(System.out::println);
    }

    //修改数据
    @Test
    public void testUpdate() {

        Query query = new Query(Criteria.where("name").is("test"));

        Update update = new Update();
        update.set("name", "杰克");
        update.set("age", 18);

        mongoTemplate.updateMulti(query, update, User.class);
    }

    //删除
    @Test
    public void delete() {
        Query query = new Query(Criteria.where("name").is("杰克"));
        mongoTemplate.remove(query, User.class);
    }


}
