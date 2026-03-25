// Address.java
package com.example.entity;

import lombok.Data;

@Data
public class Address {
    private Integer id;
    private Integer userId;
    private String consignee;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private Boolean isDefault;
    private String createTime;
    private String updateTime;
    // 添加用户名字段（用于管理员显示）
    private String userName;

    // 完整地址（计算属性）
    public String getFullAddress() {
        return province + city + district + detail;
    }
}