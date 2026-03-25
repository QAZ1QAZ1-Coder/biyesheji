package com.example.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Goods {
    //商品id
    private Integer id;
    //商品名称
    private String name;
    //商品图片
    private String img;
    //商品
    private String descr;
    private String channel;  // 进货渠道
    private String specials;
    private BigDecimal price;
    private String unit;
    private Integer store;
    private Integer categoryId;
    private String categoryName;

}
