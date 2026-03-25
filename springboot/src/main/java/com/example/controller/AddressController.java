package com.example.controller;

import com.example.common.Result;
import com.example.entity.Address;
import com.example.service.AddressService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    /**
     * 新增地址
     */
    @PostMapping("/add")
    public Result add(@RequestBody Address address) {
        addressService.add(address);
        return Result.success();
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        addressService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改地址
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Address address) {
        addressService.updateById(address);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Address address = addressService.selectById(id);
        return Result.success(address);
    }

    /**
     * 查询用户的所有地址
     */
    @GetMapping("/selectByUserId/{userId}")
    public Result selectByUserId(@PathVariable Integer userId) {
        List<Address> list = addressService.selectByUserId(userId);
        return Result.success(list);
    }

    /**
     * 查询用户的默认地址
     */
    @GetMapping("/selectDefaultByUserId/{userId}")
    public Result selectDefaultByUserId(@PathVariable Integer userId) {
        Address address = addressService.selectDefaultByUserId(userId);
        return Result.success(address);
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/setDefault/{id}")
    public Result setDefault(@PathVariable Integer id, @RequestParam Integer userId) {
        addressService.setDefault(id, userId);
        return Result.success();
    }

    /**
     * 管理员获取所有地址（包含用户信息）
     */
    @GetMapping("/admin/all")
    public Result getAllAddressesWithUser() {
        List<Address> list = addressService.selectAllWithUser();
        return Result.success(list);
    }

    /**
     * 管理员删除地址
     */
    @DeleteMapping("/admin/delete/{id}")
    public Result adminDelete(@PathVariable Integer id) {
        addressService.deleteById(id);
        return Result.success();
    }
}