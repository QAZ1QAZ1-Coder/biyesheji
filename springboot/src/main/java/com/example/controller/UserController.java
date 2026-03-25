package com.example.controller;

import com.example.common.Result;
import com.example.entity.User;
import com.example.mapper.UserMapper;
import com.example.service.UserService;
import com.example.utils.JwtTokenUtil;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import java.util.List;

/**
 * 用户前端操作接口
 **/
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody User user) {
        userService.add(user);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        userService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody User user) {
        userService.updateById(user);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        User user = userService.selectById(id);
        return Result.success(user);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(User user) {
        List<User> list = userService.selectAll(user);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(User user,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<User> page = userService.selectPage(user, pageNum, pageSize);
        return Result.success(page);
    }
    /**
     * 获取当前登录用户完整信息
     */
    /**
     * 获取当前登录用户完整信息
     */
    /**
     * 获取当前登录用户完整信息
     */
    /**
     * 获取当前登录用户完整信息 - 增强容错性
     */
    @GetMapping("/currentUser")
    public Result getCurrentUser(HttpServletRequest request) {
        try {
            // 1. 从Token获取用户名
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.error("用户未登录");
            }

            token = token.substring(7);
            String username = jwtTokenUtil.getUsernameFromToken(token);

            if (username == null) {
                return Result.error("用户未登录");
            }

            // 2. 使用selectById方法查询（更可靠）
            // 先通过username获取ID
            User tempUser = userMapper.selectByUsername(username);
            if (tempUser == null) {
                return Result.error("用户不存在");
            }

            // 3. 使用ID查询完整用户信息（包含所有字段）
            User user = userMapper.selectById(tempUser.getId());

            if (user == null) {
                return Result.error("用户不存在");
            }

            // 4. 确保敏感信息不返回
            user.setPassword(null);

            return Result.success(user);

        } catch (Exception e) {
            return Result.error("系统异常");
        }
    }

    /**
     * 上传头像 - 支持从指定基础目录上传
     */
    @PostMapping("/uploadAvatar")
    public Result uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            // 1. 验证Token
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.error("用户未登录");
            }

            token = token.substring(7);
            String username = jwtTokenUtil.getUsernameFromToken(token);

            if (username == null) {
                return Result.error("Token无效或已过期");
            }

            // 2. 获取用户
            User user = userMapper.selectByUsername(username);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 3. 验证文件
            if (file == null || file.isEmpty()) {
                return Result.error("请选择图片文件");
            }

            // 4. 获取原始文件名并生成新文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + suffix;

            // 5. 定义多个可能的存储目录（按优先级）
            String[] possibleDirs = {
                    "E:\\javaEE 2025\\farm_system\\files\\avatars\\",  // 您的指定目录
                    "uploads/avatars/",                               // 项目根目录
                    System.getProperty("java.io.tmpdir") + "uploads/avatars/", // 系统临时目录
                    "src/main/resources/static/uploads/avatars/"      // resources目录
            };

            File targetDir = null;
            File targetFile = null;

            // 6. 尝试每个目录，使用第一个可用的
            for (String dirPath : possibleDirs) {
                File dir = new File(dirPath);
                if (dir.exists() || dir.mkdirs()) {
                    targetFile = new File(dir, filename);
                    try {
                        file.transferTo(targetFile);
                        targetDir = dir;
                        break; // 成功保存，退出循环
                    } catch (IOException e) {
                        System.out.println("保存到目录 " + dirPath + " 失败: " + e.getMessage());
                        continue; // 尝试下一个目录
                    }
                }
            }

            if (targetFile == null || !targetFile.exists()) {
                return Result.error("文件保存失败，请检查目录权限");
            }

            // 7. 根据保存的目录生成访问路径
            String avatarPath = "";
            if (targetDir.getAbsolutePath().contains("E:\\javaEE 2025\\farm_system\\files")) {
                // 如果保存在您的指定目录
                avatarPath = "/files/avatars/" + filename;
            } else {
                // 其他目录
                String relativePath = targetFile.getAbsolutePath()
                        .replace(System.getProperty("user.dir"), "")
                        .replace("\\", "/");
                avatarPath = relativePath.startsWith("/") ? relativePath : "/" + relativePath;
            }

            // 8. 更新用户头像
            user.setAvatar(avatarPath);
            userMapper.updateById(user);

            System.out.println("头像保存到: " + targetFile.getAbsolutePath());
            System.out.println("头像访问路径: " + avatarPath);

            return Result.success(avatarPath);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("上传失败: " + e.getMessage());
        }
    }

}