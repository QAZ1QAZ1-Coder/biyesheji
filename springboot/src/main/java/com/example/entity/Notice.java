package com.example.entity;


import lombok.Data;

import java.time.LocalDateTime;


@Data
public class Notice {
    //标题id
    private Integer id;
    //标题
    private String title;
    //内容
    private String content;
    //时间
    private String time;

}
