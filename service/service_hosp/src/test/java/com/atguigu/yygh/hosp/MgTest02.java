package com.atguigu.yygh.hosp;


import com.atguigu.yygh.hosp.mgtest.User;
import com.atguigu.yygh.hosp.mgtest.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;

import java.util.List;

@SpringBootTest
public class MgTest02 {
        @Autowired
        private UserRepository userRepository;

        //添加
       @Test
       public void createUser() {
            User user = new User();
            user.setAge(20);
            user.setName("张三");
            user.setEmail("3332200@qq.com");
            //user.setId("62cd3438fb1166732a1391ad");  save方法既可以添加也可以修改，取决于user中有没有id
            userRepository.save(user);
    }
    //修改
    @Test
    public void updateUser(){
        User user = userRepository.findById("6384258384ac0752ea0bcc09").get();
        user.setName("张三修改李四");
        user.setAge(25);
        User save = userRepository.save(user);
        System.out.println(save);

    }

    //查询所有
    @Test
    public void findUser(){
        List<User> userList = userRepository.findAll();
        userList.forEach(System.out::println);
    }

    //根据ID查询
    @Test
    public void findById(){
        User user = userRepository.findById("6384258384ac0752ea0bcc09").get();
        System.out.println(user);
    }

    //根据id删除
    @Test
    public void delete(){
           userRepository.deleteById("6384258384ac0752ea0bcc09");
    }

    //条件查询
    @Test
    public void findQuery(){
        User user = new User();
        user.setName("张三");
        user.setAge(20);

        Example<User> userExample = Example.of(user);
        List<User> list = userRepository.findAll(userExample);
        list.forEach(System.out::println     );

    }

    //模糊查询

    @Test
    public void findLike(){
        User user = new User();
        user.setName("张");

        ExampleMatcher matcher =
                ExampleMatcher
                        .matching()
                        .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)//模糊查询
                        .withIgnoreCase(true); //忽略大小写

        Example<User> example =  Example.of(user,matcher);

        List<User> all = userRepository.findAll(example);

        System.out.println(all);
    }


    //分页查询
    @Test
    public List<User> findPage(int page,int limit) {

        User user = new User();
        user.setName("张");

        //模糊查询
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);

        Example<User> example = Example.of(user, matcher);

        Pageable pageable = PageRequest.of(page, limit);

        Page<User> pages = userRepository.findAll(example, pageable);

        return pages.getContent();
    }
}


