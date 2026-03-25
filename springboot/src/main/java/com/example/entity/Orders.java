package com.example.entity;

import lombok.Data;

@Data
public class Orders {
    private Integer id;
    private String orderNo;
    private Integer goodsId;
    private Integer num;
    private Integer userId;
    private String status;
    private String time;
    private String goodsName;
    private String goodsImg;
    private String userName;

    // 新增地址关联
    private Integer addressId;

    // 地址详细信息（从关联查询中获取）
    private String consignee;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String addressDetail;

    // 完整地址（计算属性）
    public String getFullAddress() {
        return province + city + district + addressDetail;
    }

    // 新增：地址信息字段（用于前端显示）
    private String addressInfo;

    // 价格相关字段
    private Double price;
    private Double total;
    private String specials;
    private String unit;
}