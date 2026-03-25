package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.example.entity.Account;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.UserMapper;
import com.example.utils.JwtTokenUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 用户业务处理
 **/
@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private HttpServletRequest request;

    @Resource
    private JwtTokenUtil jwtTokenUtil; // 注入JWT工具类

    // 手机号正则表达式
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    // 邮箱正则表达式
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    /**
     * 登录 - 修改为返回包含token的Account
     */
    // 在 UserService.java 的 login 方法中修改
    public Account login(Account account) {
        User dbUser = userMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException("用户不存在");
        }
        if (!account.getPassword().equals(dbUser.getPassword())) {
            throw new CustomException("账号或密码错误");
        }

        // 生成token
        String token = jwtTokenUtil.generateToken(dbUser.getId(), dbUser.getUsername(), "USER");

        // 创建返回的Account对象
        Account result = new Account();
        result.setId(dbUser.getId());
        result.setUsername(dbUser.getUsername());
        result.setName(dbUser.getName());
        result.setRole("USER");
        result.setToken(token); // 设置token
        result.setAvatar(dbUser.getAvatar()); // 添加头像字段

        return result;
    }

    // 其他方法保持不变...
    /**
     * 新增
     */
    public void add(User user) {
        // 验证用户信息
        validateUser(user);

        User dbUser = userMapper.selectByUsername(user.getUsername());
        if (ObjectUtil.isNotNull(dbUser)) {
            throw new CustomException("用户已存在");
        }
        if (ObjectUtil.isEmpty(user.getPassword())) {
            user.setPassword("123");
        }
        if (ObjectUtil.isEmpty(user.getName())) {
            user.setName(user.getUsername());
        }
        user.setRole("USER");
        userMapper.insert(user);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        userMapper.deleteById(id);
    }

    /**
     * 修改
     */
    public void updateById(User user) {
        // 验证用户信息
        validateUser(user);
        // 如果密码为空，保留原密码
        User dbUser = userMapper.selectById(user.getId());
        if (ObjectUtil.isEmpty(user.getPassword())) {
            user.setPassword(dbUser.getPassword()); // 保留原密码
        }
        userMapper.updateById(user);
    }

    /**
     * 根据ID查询
     */
    public User selectById(Integer id) {
        return userMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<User> selectAll(User user) {
        return userMapper.selectAll(user);
    }

    /**
     * 分页查询
     */
    public PageInfo<User> selectPage(User user, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> list = userMapper.selectAll(user);
        return PageInfo.of(list);
    }

    /**
     * 修改密码
     */
    public void updatePassword(Account account) {
        User dbUser = userMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException("用户不存在");
        }
        if (!account.getPassword().equals(dbUser.getPassword())) {
            throw new CustomException("原密码错误");
        }
        dbUser.setPassword(account.getNewPassword());
        userMapper.updateById(dbUser);
    }

    // 注册
    public void register(User user) {
        this.add(user);
    }

    /**
     * 验证用户信息
     */
    private void validateUser(User user) {
        // 验证用户名
        if (ObjectUtil.isEmpty(user.getUsername())) {
            throw new CustomException("用户名不能为空");
        }

        // 验证姓名
        if (ObjectUtil.isEmpty(user.getName())) {
            throw new CustomException("姓名不能为空");
        }

        // 验证手机号
        if (ObjectUtil.isEmpty(user.getPhone())) {
            throw new CustomException("手机号不能为空");
        }
        if (!Pattern.matches(PHONE_REGEX, user.getPhone())) {
            throw new CustomException("手机号格式不正确，请输入11位有效手机号");
        }

        // 验证邮箱（如果邮箱不为空）
        if (ObjectUtil.isNotEmpty(user.getEmail()) && !user.getEmail().trim().isEmpty()) {
            if (!Pattern.matches(EMAIL_REGEX, user.getEmail())) {
                throw new CustomException("邮箱格式不正确");
            }
        }
    }
}