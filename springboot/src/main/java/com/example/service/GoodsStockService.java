package com.example.service;

import com.example.entity.Goods;
import com.example.entity.GoodsStock;
import com.example.mapper.GoodsStockMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * 系统公告业务处理
 **/
@Service
public class GoodsStockService {

    @Resource
    private GoodsStockMapper goodsStockMapper;

    @Resource
    private GoodsService goodsService;


    /**
     * 如果商品不存在则创建
     */
    @Transactional
    public GoodsStock createGoodsIfNotExists(GoodsStock goodsStock) {
        // 根据商品名称查找是否已存在
        Goods query = new Goods();
        query.setName(goodsStock.getGoodsName());
        List<Goods> existingGoods = goodsService.selectAll(query);

        if (existingGoods.isEmpty()) {
            // 创建新商品
            Goods newGoods = new Goods();
            newGoods.setName(goodsStock.getGoodsName());
            newGoods.setStore(0); // 初始库存为0
            newGoods.setUnit("斤"); // 默认单位

            // 这个add方法现在应该能正确设置newGoods的id
            goodsService.add(newGoods);

            // 直接使用newGoods的id（MyBatis会通过@Options自动设置）
            goodsStock.setGoodsId(newGoods.getId());
        } else {
            goodsStock.setGoodsId(existingGoods.get(0).getId());
        }

        return goodsStock;
    }


    /**
     * 新增
     */
    @Transactional
    public void add(GoodsStock goodsStock) {
        // 检查进货数量是否为空
        if (goodsStock.getNum() == null) {
            throw new RuntimeException("进货数量不能为空");
        }

        // 检查商品ID是否为空
        if (goodsStock.getGoodsId() == null) {
            throw new RuntimeException("商品ID不能为空");
        }

        // 查询到当前商品进货的商品信息
        Goods goods = goodsService.selectById(goodsStock.getGoodsId());
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }

        // 增加库存
        goods.setStore(goods.getStore() + goodsStock.getNum());
        goodsService.updateById(goods);

        // 插入进货记录
        goodsStockMapper.insert(goodsStock);
    }

    /**
     * 删除
     */
    @Transactional
    public void deleteById(Integer id) {
        // 查询要删除的进货记录
        GoodsStock goodsStock = goodsStockMapper.selectById(id);
        if (goodsStock != null) {
            // 查询对应的商品信息
            Goods goods = goodsService.selectById(goodsStock.getGoodsId());
            if (goods != null) {
                // 删除进货记录时，减少对应的库存
                goods.setStore(goods.getStore() - goodsStock.getNum());
                goodsService.updateById(goods);
            }
            // 删除进货记录
            goodsStockMapper.deleteById(id);
        }
    }


    /**
     * 修改
     */
    @Transactional
    public void updateById(GoodsStock goodsStock) {
        // 检查进货数量是否为空
        if (goodsStock.getNum() == null) {
            throw new RuntimeException("进货数量不能为空");
        }

        // 检查商品ID是否为空
        if (goodsStock.getGoodsId() == null) {
            throw new RuntimeException("商品ID不能为空");
        }

        // 查询原有的进货记录
        GoodsStock oldGoodsStock = goodsStockMapper.selectById(goodsStock.getId());
        if (oldGoodsStock == null) {
            throw new RuntimeException("进货记录不存在");
        }

        // 查询对应的商品信息
        Goods goods = goodsService.selectById(goodsStock.getGoodsId());
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }

        // 计算进货数量的变化量（新数量 - 旧数量）
        Integer changeNum = goodsStock.getNum() - oldGoodsStock.getNum();

        // 更新商品库存（只增加变化的部分）
        goods.setStore(goods.getStore() + changeNum);
        goodsService.updateById(goods);

        // 更新进货记录
        goodsStockMapper.updateById(goodsStock);
    }

    /**
     * 根据id查询
     */
    public GoodsStock selectById(Integer id) {
        return goodsStockMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<GoodsStock> selectAll(GoodsStock goodsStock) {
        return goodsStockMapper.selectAll(goodsStock);
    }

    /**
     * 分页查询
     */
    public PageInfo<GoodsStock> selectPage(GoodsStock goodsStock, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<GoodsStock> list = goodsStockMapper.selectAll(goodsStock);
        return PageInfo.of(list);
    }



}