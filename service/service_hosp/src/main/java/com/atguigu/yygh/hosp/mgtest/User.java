package com.atguigu.yygh.hosp.mgtest;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document("User") //集合名称
public class User {
    @Id//主键
    private String id;


    private String name;
    private Integer age;
    private String email;
    private Date createDate;
}
