package com.example.controller;

import com.example.common.Result;
import com.example.entity.Admin;
import com.example.mapper.AdminMapper;
import com.example.service.AdminService;
import com.example.utils.JwtTokenUtil;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员前端操作接口
 **/
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private AdminService adminService;
    @Resource
    private AdminMapper adminMapper;
    @Resource
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Admin admin) {
        adminService.add(admin);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        adminService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Admin admin) {
        adminService.updateById(admin);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Admin admin = adminService.selectById(id);
        return Result.success(admin);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Admin admin) {
        List<Admin> list = adminService.selectAll(admin);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Admin admin,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Admin> page = adminService.selectPage(admin, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 获取当前登录管理员完整信息
     */
    @GetMapping("/currentAdmin")
    public Result getCurrentAdmin(HttpServletRequest request) {
        try {
            System.out.println("=== 调试：获取当前管理员 ===");

            // 从Token获取用户名
            String token = request.getHeader("Authorization");
            System.out.println("原始Token头: " + token);

            String username = null;
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                System.out.println("提取的Token: " + (token.length() > 10 ? token.substring(0, 10) + "..." : token));

                try {
                    // 使用Token解析用户名
                    username = jwtTokenUtil.getUsernameFromToken(token);
                    System.out.println("✅ 从Token解析的管理员用户名: " + username);
                } catch (Exception e) {
                    System.out.println("❌ Token解析异常: " + e.getMessage());
                }
            }

            if (username == null) {
                System.out.println("❌ 无法获取管理员用户名");
                return Result.error("管理员未登录");
            }

            // 查询管理员表
            Admin admin = adminMapper.selectByUsername(username);
            if (admin == null) {
                System.out.println("❌ 数据库中没有找到管理员: " + username);
                return Result.error("管理员不存在");
            }

            System.out.println("✅ 成功获取管理员: " + admin.getName());
            return Result.success(admin);

        } catch (Exception e) {
            System.err.println("获取管理员信息异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("系统异常: " + e.getMessage());
        }
    }

}