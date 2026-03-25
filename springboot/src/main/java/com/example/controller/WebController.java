package com.example.controller;

import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.Goods;
import com.example.entity.Notice;
import com.example.entity.User;
import com.example.service.AdminService;
import com.example.service.GoodsService;
import com.example.service.NoticeService;
import com.example.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class WebController {

    @Resource
    private AdminService adminService;
    @Resource
    private UserService userService;
    @Resource
    private GoodsService goodsService;
    @Resource
    private NoticeService noticeService;

    /**
     * 默认请求接口
     */
    @GetMapping("/")
    public Result hello() {
        return Result.success("欢迎使用助农农产品销售系统");
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        Account ac = null;
        if ("ADMIN".equals(account.getRole())) {
            ac = adminService.login(account);
        } else {
            ac = userService.login(account);
        }
        return Result.success(ac);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        userService.register(user);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody Account account) {
        if ("ADMIN".equals(account.getRole())) {
            adminService.updatePassword(account);
        } else {
            userService.updatePassword(account);
        }
        return Result.success();
    }

    /**
     * 获取首页数据 - 小程序专用
     */
    @GetMapping("/home/data")
    public Result getHomeData() {
        Map<String, Object> data = new HashMap<>();

        try {
            // 获取热门商品（前6个）
            Goods goodsQuery = new Goods();
            List<Goods> allGoods = goodsService.selectAll(goodsQuery);
            if (allGoods.size() > 6) {
                allGoods = allGoods.subList(0, 6);
            }
            data.put("hotProducts", allGoods);

            // 获取系统公告（按时间倒序）
            Notice noticeQuery = new Notice();
            List<Notice> notices = noticeService.selectAll(noticeQuery);
            data.put("notices", notices);

            return Result.success(data);
        } catch (Exception e) {
            return Result.error("获取首页数据失败");
        }
    }

    /**
     * 获取商品分类 - 小程序专用
     */
    @GetMapping("/categories")
    public Result getCategories() {
        // 这里可以根据你的分类表来返回数据
        // 暂时返回静态分类
        Map<String, Object> categories = new HashMap<>();
        categories.put("1", "蔬菜");
        categories.put("2", "水果");
        categories.put("3", "菌菇");
        categories.put("4", "禽蛋");
        return Result.success(categories);
    }
}