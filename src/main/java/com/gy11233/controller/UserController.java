package com.gy11233.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gy11233.common.BaseResponse;
import com.gy11233.common.ErrorCode;
import com.gy11233.common.ResultUtils;
import com.gy11233.exception.BusinessException;
import com.gy11233.model.domain.User;
import com.gy11233.model.request.UserLoginRequest;
import com.gy11233.model.request.UserRegisterRequest;
import com.gy11233.model.vo.UserFriendsVo;
import com.gy11233.model.vo.UserVO;
import com.gy11233.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.gy11233.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 */
@RestController
@RequestMapping("/user")
//@CrossOrigin(origins = "http://localhost:5173", allowCredentials="true") // 解决跨域
//@CrossOrigin // 解决跨域
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 校验
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前用户
     *
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }


    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "缺少管理员权限");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<UserVO>> searchUserByTags(@RequestParam(required = false) List<String> tagsNameList,
                                                       HttpServletRequest request){
        if(CollectionUtils.isEmpty(tagsNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<UserVO> userList = userService.searchUserByTags(tagsNameList, loginUser);
        return ResultUtils.success(userList);

    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }


    /**
     * 更新用户信息
     * @param user
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request){
        // 判断参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        int result = userService.updateUser(user, currentUser);
        return ResultUtils.success(result);
    }

    /**
     * 推荐主页用户
     * @param pageSize
     * @param pageNum
     * @param request
     * @return
     */
    // todo:推荐的用户是随机的 需要完善
    @GetMapping("/recommend")
    public BaseResponse<List<UserFriendsVo>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request){
        // 通过userId查看redis中是否有对应的信息
        User userObj = (User)request.getSession().getAttribute(USER_LOGIN_STATE);
        return ResultUtils.success(userService.recommendUsers(pageSize, pageNum, userObj));

    }

    /**
     * 获取最匹配的用户
     */
    @GetMapping("/match")
    public BaseResponse<List<UserFriendsVo>> matchUsers(int num, HttpServletRequest request) {
        if (num <= 0 || num >20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<UserFriendsVo> userList = userService.matchUsers(num, loginUser);
        return ResultUtils.success(userList);
    }

    /**
     * 搜索附近用户
     */
    @GetMapping("/searchNearby")
    public BaseResponse<List<UserFriendsVo>> searchNearby(int radius, HttpServletRequest request) {
        if (radius <= 0 || radius > 10000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        User loginUser = userService.getById(user.getId());
        List<UserFriendsVo> userVOList = userService.searchNearby(radius, loginUser);
        return ResultUtils.success(userVOList);
    }


}
