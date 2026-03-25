package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.example.entity.Address;
import com.example.entity.Goods;
import com.example.entity.Orders;
import com.example.exception.CustomException;
import com.example.mapper.AddressMapper;
import com.example.mapper.OrdersMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrdersService {

    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    private GoodsService goodsService;
    @Resource
    private AddressMapper addressMapper;

    /**
     * 新增
     */
//    @Transactional 保证了订单创建和库存扣减的一致性，防止出现库存扣了订单没创建的数据不一致问题。

//    第三步：核心业务逻辑处理
    @Transactional
    public void add(Orders orders) {
        // 2.1验证地址是否存在
        if (orders.getAddressId() == null) {
            throw new CustomException("请选择收货地址");
        }
        Address address = addressMapper.selectById(orders.getAddressId());
        if (address == null) {
            throw new CustomException("收货地址不存在");
        }
        if (!address.getUserId().equals(orders.getUserId())) {
            throw new CustomException("收货地址不属于当前用户");
        }
        // 2.2 生成订单基础信息
        orders.setOrderNo(IdUtil.fastSimpleUUID());//唯一的订单编号
        orders.setTime(DateUtil.now());// 设置当前时间
        orders.setStatus("待支付");// 设置初始状态
        // 2.3 库存检查与扣减
        Goods goods = goodsService.selectById(orders.getGoodsId());
        if (goods == null){
            throw new CustomException("商品不存在");
        }
        int store = goods.getStore() - orders.getNum();
        if (store < 0){
            throw new CustomException("商品库存不足");
        }
        // 2.4 库存扣减 + 订单创建
        goods.setStore(store);
        goodsService.updateById(goods);// 更新库存
        ordersMapper.insert(orders);// 创建订单
    }

    //第三步：核心业务逻辑处理
//    @Transactional
//    public void add(Orders orders) {
//        // 3.1：地址验证（必须存在且属于用户）
//        Address address = addressMapper.selectById(orders.getAddressId());
//        if (!address.getUserId().equals(orders.getUserId())) {
//            throw new CustomException("收货地址不属于当前用户");
//        }
//        // 3.2：库存扣减（核心业务）
//        Goods goods = goodsService.selectById(orders.getGoodsId());
//        int store = goods.getStore() - orders.getNum();
//        if (store < 0){
//            throw new CustomException("商品库存不足");
//        }
//        // 3.3：同步操作（库存更新 + 订单创建）
//        goods.setStore(store);
//        goodsService.updateById(goods);
//        ordersMapper.insert(orders);
//    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        ordersMapper.deleteById(id);
    }

    /**
     * 修改
     */

    // 第四步：核心业务逻辑处理
    public void updateById(Orders orders) {
        //  4.1. 查询原订单信息（获取当前状态）
        Orders dbOrder = ordersMapper.selectById(orders.getId());
        // 4.2. 处理状态转换逻辑
        String oldStatus = dbOrder.getStatus();  // 原状态："待支付"
        String newStatus = orders.getStatus();   // 新状态："待发货"
        // 4.3. 支付成功处理（待支付 → 待发货）
        if ("待支付".equals(oldStatus) &&
                "待发货".equals(newStatus)) {
        }

        // 4.4. 取消订单处理（待支付 → 已取消）
        if ("待支付".equals(oldStatus) && "已取消".equals(newStatus)) {
            // 返还库存
            Goods goods = goodsService.selectById(orders.getGoodsId());
            if (goods != null) {
                goods.setStore(goods.getStore() + orders.getNum());
                goodsService.updateById(goods);
            }
        }

        // ✅ 5. 更新订单状态到数据库
        ordersMapper.updateById(orders);
    }

    /**
     * 根据ID查询
     */
    public Orders selectById(Integer id) {
        return ordersMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Orders> selectAll(Orders orders) {
        return ordersMapper.selectAll(orders);
    }

    /**
     * 分页查询
     */
    public PageInfo<Orders> selectPage(Orders orders, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> list = ordersMapper.selectAll(orders);
        return PageInfo.of(list);
    }
}