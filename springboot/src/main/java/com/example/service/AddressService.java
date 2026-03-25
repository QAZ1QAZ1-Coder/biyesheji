package com.example.service;

import com.example.entity.Address;
import com.example.mapper.AddressMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    @Resource
    private AddressMapper addressMapper;

    /**
     * 新增地址
     */
    @Transactional
    public void add(Address address) {
        // 如果设置为默认地址，先取消其他默认地址
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressMapper.cancelAllDefault(address.getUserId());
        }
        addressMapper.insert(address);
    }

    /**
     * 删除地址
     */
    public void deleteById(Integer id) {
        addressMapper.deleteById(id);
    }

    /**
     * 修改地址
     */
    @Transactional
    public void updateById(Address address) {
        // 如果设置为默认地址，先取消其他默认地址
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressMapper.cancelAllDefault(address.getUserId());
        }
        addressMapper.updateById(address);
    }

    /**
     * 根据ID查询
     */
    public Address selectById(Integer id) {
        return addressMapper.selectById(id);
    }

    /**
     * 查询用户的所有地址
     */
    public List<Address> selectByUserId(Integer userId) {
        return addressMapper.selectByUserId(userId);
    }

    /**
     * 查询用户的默认地址
     */
    public Address selectDefaultByUserId(Integer userId) {
        return addressMapper.selectDefaultByUserId(userId);
    }

    /**
     * 设置默认地址
     */
    @Transactional
    public void setDefault(Integer id, Integer userId) {
        // 先取消所有默认地址
        addressMapper.cancelAllDefault(userId);
        // 设置新的默认地址
        addressMapper.setDefault(id);
    }
    /**
     * 查询所有地址（管理员专用）
     */
    public List<Address> selectAllWithUser() {
        return addressMapper.selectAllWithUser();
    }
}