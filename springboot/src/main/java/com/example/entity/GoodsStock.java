package com.example.entity;

import lombok.Data;

@Data
public class GoodsStock {
    private Integer id;
    private Integer goodsId;
    private Integer num;
    private String channel;
    private String date;
    private String comment;
    private String goodsName;
}
