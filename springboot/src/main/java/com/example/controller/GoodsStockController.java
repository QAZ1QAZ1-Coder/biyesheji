package com.example.controller;

import com.example.common.Result;
import com.example.entity.GoodsStock;
import com.example.service.GoodsStockService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 系统公告前端操作接口
 **/
@RestController
@RequestMapping("/goodsStock")
public class GoodsStockController {
    @Resource
    private GoodsStockService goodsStockService;

    /**
     * 新增 - 修改为支持创建新商品
     */
    @PostMapping("/add")
    public Result add(@RequestBody GoodsStock goodsStock) {
        // 前端传入 goodsName，但需要 goodsId
        if (goodsStock.getGoodsId() == null && goodsStock.getGoodsName() != null) {
            // 需要先创建商品
            goodsStock = goodsStockService.createGoodsIfNotExists(goodsStock);
        }
        goodsStockService.add(goodsStock);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        goodsStockService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改 - 同样支持创建新商品
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody GoodsStock goodsStock) {
        if (goodsStock.getGoodsId() == null && goodsStock.getGoodsName() != null) {
            // 需要先创建商品
            goodsStock = goodsStockService.createGoodsIfNotExists(goodsStock);
        }
        goodsStockService.updateById(goodsStock);
        return Result.success(goodsStock);
    }

    /**
     * 根据标题查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        GoodsStock goodsStock = goodsStockService.selectById(id);
        return Result.success(goodsStock);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(GoodsStock goodsStock) {
        List<GoodsStock> list = goodsStockService.selectAll(goodsStock);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(GoodsStock goodsStock,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<GoodsStock> page = goodsStockService.selectPage(goodsStock, pageNum, pageSize);
        return Result.success(page);
    }
}
