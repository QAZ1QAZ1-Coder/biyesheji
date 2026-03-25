package com.example.entity;

import lombok.Data;

/**
 * 用户
*/
@Data
public class User extends Account {

    /** ID */
    private Integer id;
    /** 用户名 */
    private String username;
    /** 密码 */
    private String password;
    /** 姓名 */
    private String name;
    /** 头像 */
    private String avatar;
    /** 角色标识 */
    private String role;
    private String sex;
    private String phone;
    private String email;



}