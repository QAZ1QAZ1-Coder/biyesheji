// AddressMapper.java
package com.example.mapper;

import com.example.entity.Address;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface AddressMapper {

    /**
     * 新增
     */
    int insert(Address address);

    /**
     * 删除
     */
    @Delete("delete from address where id = #{id}")
    int deleteById(Integer id);

    /**
     * 修改
     */
    int updateById(Address address);

    /**
     * 根据ID查询
     */
    @Select("select * from address where id = #{id}")
    Address selectById(Integer id);

    /**
     * 查询用户的所有地址
     */
    @Select("select * from address where user_id = #{userId} order by is_default desc, update_time desc")
    List<Address> selectByUserId(Integer userId);

    /**
     * 查询用户的默认地址
     */
    @Select("select * from address where user_id = #{userId} and is_default = true")
    Address selectDefaultByUserId(Integer userId);

    /**
     * 取消用户的所有默认地址
     */
    @Update("update address set is_default = false where user_id = #{userId}")
    int cancelAllDefault(Integer userId);

    /**
     * 设置默认地址
     */
    @Update("update address set is_default = true where id = #{id}")
    int setDefault(Integer id);

    /**
     * 查询所有地址（管理员专用）- 移除注解，使用XML配置
     */
    List<Address> selectAllWithUser();
}