package com.gy11233.service;

import com.gy11233.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gy11233.model.request.UserRegisterRequest;
import com.gy11233.model.vo.UserFriendsVo;
import com.gy11233.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 *
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @return 新用户 id
     */
    long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 将普通用户转成userV0
     * 普通用户为user 主要查询用户时originUser，距离为计算两者的距离
     * @param originUser
     * @return
     */
    UserVO getUserVo(User user, User originUser);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    List<UserVO> searchUserByTags(List<String> tagNameList, User loginUser);

    int updateUser(User user, User currentUser);

    List<UserFriendsVo> recommendUsers(long pageSize, long pageNum, User user);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);


    boolean isAdmin(User user);

    User getLoginUser(HttpServletRequest request);

    /**
     * 匹配最合适的num个用户
     * @param num
     *
     * @return
     */
    List<UserFriendsVo> matchUsers(int num, User loginUser);

    List<UserFriendsVo> searchNearby(int radius, User loginUser);

    UserFriendsVo getUserFriendsVo(User user, User originUser);
}
