package com.gy11233.service.impl;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gy11233.common.PageRequest;
import com.gy11233.contant.RedisConstant;
import com.gy11233.mannger.RateLimiter;
import com.gy11233.model.domain.Friend;
import com.gy11233.model.domain.User;
import com.gy11233.common.ErrorCode;
import com.gy11233.exception.BusinessException;
import com.gy11233.model.request.UserRegisterRequest;
import com.gy11233.model.vo.UserFriendsVo;
import com.gy11233.model.vo.UserVO;
import com.gy11233.service.FriendService;
import com.gy11233.service.UserService;
import com.gy11233.mapper.UserMapper;
import com.gy11233.utils.AlgorithmUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gy11233.contant.UserConstant.ADMIN_ROLE;
import static com.gy11233.contant.UserConstant.USER_LOGIN_STATE;
import java.util.*;
import java.util.stream.Collectors;
import org.redisson.api.RLock;

/**
 * 用户服务实现类
 *
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RateLimiter redisLimiterManager;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    private Retryer<Boolean> retryer;

    @Resource
    private FriendService friendService;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "yupi";

    /**
     * 用户注册
     *
     * @return 新用户 id
     */
    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        List<String> tagNameList = userRegisterRequest.getTagNameList();
        String username = userRegisterRequest.getUsername();
        Double longitude = userRegisterRequest.getLongitude();
        Double dimension = userRegisterRequest.getDimension();
        String phone = userRegisterRequest.getPhone();
        String avatarUrl = userRegisterRequest.getAvatarUrl();
        Integer gender = userRegisterRequest.getGender();

        //1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短，至少要4位");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短，至少要8位");
        }
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请至少选择一个标签");
        }
        if (StringUtils.isBlank(username) || username.length() > 10) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称不合法，长度不得超过 10");
        }
        if (longitude == null || longitude > 180 || longitude < -180) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "坐标经度不合法");
        }
        if (dimension == null || dimension > 90 || dimension < -90) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "坐标维度不合法");
        }


        // 限流
        redisLimiterManager.doRateLimiter(userAccount);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        Long userCount = userMapper.selectCount(queryWrapper);
        if (userCount > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }
        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setLongitude(longitude);
        user.setDimension(dimension);
        user.setUsername(username);
        user.setGender(gender);
        user.setAvatarUrl(avatarUrl);
        user.setPhone(phone);
        // 处理用户标签
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('[');
        for (int i = 0; i < tagNameList.size(); i++) {
            stringBuilder.append('"').append(tagNameList.get(i)).append('"');
            if (i < tagNameList.size() - 1) {
                stringBuilder.append(',');
            }
        }
        stringBuilder.append(']');
        user.setTags(stringBuilder.toString());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "添加失败");
        }
        // 如果用户信息插入数据库，则计算用户坐标信息并存入Redis
        Long addToRedisResult = stringRedisTemplate.opsForGeo().add(RedisConstant.USER_GEO_KEY,
                new Point(user.getLongitude(), user.getDimension()), String.valueOf(user.getId()));
        if (addToRedisResult == null || addToRedisResult <= 0) {
            log.error("用户注册时坐标信息存入Redis失败");
        }
        // 统一设置星球编号 不让用户自己定义
        long userId = user.getId();
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPlanetCode(String.valueOf(userId));
        boolean updateResult = this.updateById(updateUser);
        // 用户注册后对标签进行缓存
        if (!updateResult) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "注册失败");
        }
//        if (!updateResult) {
//            log.info("{}用户星球编号设置失败", userId);
//        } else {
//            Set<String> keys = stringRedisTemplate.keys(RedisConstant.USER_RECOMMEND_KEY + ":*");
//            for (String key : keys) {
//                try {
//                    retryer.call(() -> stringRedisTemplate.delete(key));
//                } catch (ExecutionException e) {
//                    log.error("用户注册后删除缓存重试时失败");
//                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
//                } catch (RetryException e) {
//                    log.error("用户注册后删除缓存达到最大重试次数或超过时间限制");
//                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
//                }
//            }
//        }
        return userId;
    }


    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        System.out.println(request.getSession().getId());
        System.out.println("Session Attribute: " + request.getSession().getAttribute(USER_LOGIN_STATE));
        return safetyUser;
    }



    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    @Override
    public UserVO getUserVo(User user, User originUser) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setUserAccount(user.getUserAccount());
        userVO.setAvatarUrl(user.getAvatarUrl());
        userVO.setGender(user.getGender());
        userVO.setPhone(user.getPhone());
        userVO.setEmail(user.getEmail());
        userVO.setUserStatus(user.getUserStatus());
        userVO.setCreateTime(user.getCreateTime());
        userVO.setUpdateTime(user.getUpdateTime());
        userVO.setUserRole(user.getUserRole());
        userVO.setPlanetCode(user.getPlanetCode());
        userVO.setTags(user.getTags());
        if (originUser == null) {
            userVO.setDistance(null);
            return userVO;
        }
        originUser = this.getById(originUser.getId());

        String redisUserGeoKey = RedisConstant.USER_GEO_KEY;
        List<Point> userPosition = stringRedisTemplate.opsForGeo().position(redisUserGeoKey, String.valueOf(user.getId()));
        List<Point> originUserPosition = stringRedisTemplate.opsForGeo().position(redisUserGeoKey, String.valueOf(originUser.getId()));

        // 如果用户没有位置信息，不用管缓存直接距离为null
        if (user.getLongitude()==null || user.getDimension()==null || originUser.getDimension()==null || originUser.getLongitude()==null) {
            userVO.setDistance(null);
        }

        else {

            if (userPosition == null || userPosition.get(0)==null) { // 更缓存
                Long addToRedisResult = stringRedisTemplate.opsForGeo().add(redisUserGeoKey,
                        new Point(user.getLongitude(), user.getDimension()), String.valueOf(user.getId()));
                if (addToRedisResult == null || addToRedisResult <= 0) {
                    log.error("用户坐标信息存入Redis失败");
                }
            }
            if (originUserPosition == null ||originUserPosition.get(0) == null) {
                Long addToRedisResult = stringRedisTemplate.opsForGeo().add(redisUserGeoKey,
                        new Point(originUser.getLongitude(), originUser.getDimension()), String.valueOf(originUser.getId()));
                if (addToRedisResult == null || addToRedisResult <= 0) {
                    log.error("当前用户坐标信息存入Redis失败");
                }
            }

            // 计算两者距离
            Distance distance = stringRedisTemplate.opsForGeo().distance(redisUserGeoKey,
                    String.valueOf(originUser.getId()), String.valueOf(user.getId()),
                    RedisGeoCommands.DistanceUnit.KILOMETERS);

            if (distance == null){
                log.error("计算两个用户距离失败");
                userVO.setDistance(null);
            }
            else {
                // 保存
                userVO.setDistance(distance.getValue());
            }
        }



        return userVO;
    }

    public UserFriendsVo getUserFriendsVo(User user, User originUser) {
        if (user == null) {
            return null;
        }
        UserFriendsVo userfriendsV0 = new UserFriendsVo();
        userfriendsV0.setId(user.getId());
        userfriendsV0.setUsername(user.getUsername());
        userfriendsV0.setUserAccount(user.getUserAccount());
        userfriendsV0.setAvatarUrl(user.getAvatarUrl());
        userfriendsV0.setGender(user.getGender());
        userfriendsV0.setPhone(user.getPhone());
        userfriendsV0.setEmail(user.getEmail());
        userfriendsV0.setUserStatus(user.getUserStatus());
        userfriendsV0.setCreateTime(user.getCreateTime());
        userfriendsV0.setUpdateTime(user.getUpdateTime());
        userfriendsV0.setUserRole(user.getUserRole());
        userfriendsV0.setPlanetCode(user.getPlanetCode());
        userfriendsV0.setTags(user.getTags());
        if (originUser == null) {
            userfriendsV0.setDistance(null);
        }

        else {
            String redisUserGeoKey = RedisConstant.USER_GEO_KEY;
            List<Point> userPosition = stringRedisTemplate.opsForGeo().position(redisUserGeoKey, String.valueOf(user.getId()));
            List<Point> originUserPosition = stringRedisTemplate.opsForGeo().position(redisUserGeoKey, String.valueOf(originUser.getId()));

            // 如果用户没有位置信息，不用管缓存直接距离为null
            if (user.getLongitude()==null || user.getDimension()==null || originUser.getDimension()==null || originUser.getLongitude()==null) {
                userfriendsV0.setDistance(null);
            }

            else {

                if (userPosition == null || userPosition.get(0)==null) { // 更缓存
                    Long addToRedisResult = stringRedisTemplate.opsForGeo().add(redisUserGeoKey,
                            new Point(user.getLongitude(), user.getDimension()), String.valueOf(user.getId()));
                    if (addToRedisResult == null || addToRedisResult <= 0) {
                        log.error("用户坐标信息存入Redis失败");
                    }
                }
                if (originUserPosition == null ||originUserPosition.get(0) == null) {
                    Long addToRedisResult = stringRedisTemplate.opsForGeo().add(redisUserGeoKey,
                            new Point(originUser.getLongitude(), originUser.getDimension()), String.valueOf(originUser.getId()));
                    if (addToRedisResult == null || addToRedisResult <= 0) {
                        log.error("当前用户坐标信息存入Redis失败");
                    }
                }

                // 计算两者距离
                Distance distance = stringRedisTemplate.opsForGeo().distance(redisUserGeoKey,
                        String.valueOf(originUser.getId()), String.valueOf(user.getId()),
                        RedisGeoCommands.DistanceUnit.KILOMETERS);

                if (distance == null){
                    log.error("计算两个用户距离失败");
                    userfriendsV0.setDistance(null);
                }
                else {
                    // 保存
                    userfriendsV0.setDistance(distance.getValue());
                }
            }

        }
        if (originUser == null) {
            userfriendsV0.setIsFriends(2); // 未登录
            return userfriendsV0;
        }
        QueryWrapper<Friend> friendQueryWrapper = new QueryWrapper<>();
        friendQueryWrapper.eq("user_id", originUser.getId());
        friendQueryWrapper.eq("friend_id", user.getId());
        List<Friend> list = friendService.list(friendQueryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            userfriendsV0.setIsFriends(0); // 不是好友
        }
        else {
            userfriendsV0.setIsFriends(1); // 是好友
        }
        return userfriendsV0;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签搜索用户 用sql
     * @param tagNameList
     * @return
     */
    @Deprecated
    public List<User> searchUserByTagsBySQL(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 用 like "%java%" and like 查询
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        for (String tagName : tagNameList) {
            userQueryWrapper = userQueryWrapper.like("tags", tagName);
        }
//        List<User> users = userMapper.selectList(userQueryWrapper);
//        users.forEach(user -> {
//            getSafetyUser(user);
//        });
        List<User> users = userMapper.selectList(userQueryWrapper);
//        users.forEach(this::getSafetyUser);
        return users.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 根据标签搜索用户 内存方法
     * @param tagNameList
     * @return
     */
    @Override
    public List<UserVO> searchUserByTags(List<String> tagNameList, User loginUser){
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 用内存的方式查询
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        List<User> users = userMapper.selectList(userQueryWrapper);
        return users.stream().filter(user -> { //过滤器
//        return users.parallelStream().filter(user -> {  // 并发的方式
            String tagsStr = user.getTags();
            Gson gson = new Gson();
            Set<String> tagSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            tagSet = Optional.ofNullable(tagSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tagSet.contains(tagName)) return false;
            }
            return true;
        }).map(user -> getUserVo(user, loginUser)).collect(Collectors.toList());
    }

    @Override
    public int updateUser(User user, User currentUser) {
        // 获取修改用户id
        long id = user.getId();
        // 判断id合法
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断id对用的用户是不是存在
        User userOld = userMapper.selectById(id);
        if (userOld == null) {
           throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 如果不是管理员 也不是当前用户 -> 不能修改信息
        if (!isAdmin(currentUser) && id!=currentUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return userMapper.updateById(user);
    }

    @Override
    public List<UserFriendsVo> recommendUsers(long pageSize, long pageNum, User user) {

        // 如果user没有登录 key设置为默认的结果
        String key;
        RLock lock;
        if (user == null) {
            key = RedisConstant.USER_RECOMMEND_KEY + ":" + "default";
            lock = redissonClient.getLock(RedisConstant.USER_RECOMMEND_LOCK + ":default");
        }
        // 如果登录且合法，设置成id
        else{
            // 获取修改用户id
            long id = user.getId();
            // 判断id合法
            if (id <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            // 判断id对用的用户是不是存在
            User userOld = userMapper.selectById(id);
            if (userOld == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            user = userOld; // 用完整的user信息 方便之后查询距离
            key = RedisConstant.USER_RECOMMEND_KEY + ":" + user.getId();
            lock = redissonClient.getLock(RedisConstant.USER_RECOMMEND_LOCK + ":" + user.getId());

        }
        // 用redisson分布式锁
        try {
            if (lock.tryLock(3000, -1, TimeUnit.MILLISECONDS)) {
                // 如果缓存中有数据，直接读缓存
                long start = (pageNum - 1) * pageSize;
                long end = start + pageSize - 1;
                List<String> userVOJsonListRedis = stringRedisTemplate.opsForList().range(key, start, end);
                // 判断 Redis 中是否有数据
                if (!CollectionUtils.isEmpty(userVOJsonListRedis)) {
                    // 有数据直接返回
                    // 将查询的缓存反序列化为 User 对象
                    return userVOJsonListRedis.stream()
                            .map(UserServiceImpl::transferToUserFriendsVO).collect(Collectors.toList());
                }
                // 缓存无数据再走数据库
                else {
                    // 无缓存，查询数据库，并将数据写入缓存
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    if (user != null) {
                        queryWrapper.ne("id", user.getId());
                    }
                    Page<User> page = this.page(new Page<>(pageNum, pageSize), queryWrapper);
                    List<User> userList = page.getRecords();
                    // 将User转换为UserVO，在进行序列化
                    User finalUser = user;
                    List<UserFriendsVo> userFriendV0 = userList.stream()
                            .map(user1 -> getUserFriendsVo(user1, finalUser))
                            .collect(Collectors.toList());
                    // 将序列化的 List 写入缓存
                    List<String> userVOJsonList = userFriendV0.stream().map(JSONUtil::toJsonStr).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(userVOJsonList)) {
                        return userFriendV0;
                    }
                    try {
                        stringRedisTemplate.opsForList().rightPushAll(key, userVOJsonList);
                        stringRedisTemplate.expire(key, 24, TimeUnit.HOURS);
                    } catch (Exception e) {
                        log.error("redis set key error", e);
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "缓存写入失败");
                    }
                    return userFriendV0;
                }
            }
        } catch (InterruptedException e) {
            log.error("user recommend lock error");
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                log.info("unlock{}", Thread.currentThread().getId());
                lock.unlock();
            }
        }
        return null;
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public boolean isAdmin(User user) {
        // 仅管理员可查询
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }


    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return user;
    }

    @Override
    public List<UserFriendsVo> matchUsers(int num, User loginUser) {
        loginUser = this.getById(loginUser.getId());
        // 距离阈值 推荐用户需要小于此阈值
        final int distanceThreshold = 4;
        final int numThreshold = 10000;
        // 获取当前数据的总量 如果缓存中有直接从缓存中获取
        String userCountKey = RedisConstant.USER_COUNT_KEY;
        Long count = (Long) redisTemplate.opsForValue().get(userCountKey);
        if (count == null) {
            count = this.count();
            redisTemplate.opsForValue().set(userCountKey, count, 24, TimeUnit.HOURS);
        }


        List<User> userList;
        // 分页查询的参数
        Random random = new Random();
        int mod = (int) Math.ceil((double) count / numThreshold );
        int pageNum = random.nextInt(mod);
        int pageSize = numThreshold;

        // 只维护top num个用户的优先队列
        Queue<Pair<User, Long>> listTop = new PriorityQueue<>((o1, o2) -> -Long.compare(o1.getValue(), o2.getValue()));

        // 尝试次数
        int tryNum = 0;
        final int tryNumMax = 10;
        while (true){
            if (count < numThreshold) {
                // 如果总量小于numThreshold 直接查询全部的数据
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                queryWrapper.select("id", "tags");
                // 去除tags为空的用户
                queryWrapper.isNotNull("tags");
                userList = this.list(queryWrapper);
            }
            else {
                // 如果总量大于numThreshold 随机查询1万个用户信息的数据
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                queryWrapper.select("id", "tags");
                queryWrapper.isNotNull("tags");
                Page<User> page = this.page(new Page<>((pageNum + tryNum) % mod + 1, pageSize), queryWrapper);
                userList = page.getRecords();
            }

            String tags = loginUser.getTags();
            Gson gson = new Gson();
            List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
            }.getType());
            // 用户列表 => 相似度
            // 依次计算所有用户和当前用户的相似度
            for (User user : userList) {
                String userTags = user.getTags();
                // 无标签或者为当前用户自己
                if (StringUtils.isBlank(userTags) || user.getId() == loginUser.getId()) {
                    continue;
                }
                List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
                }.getType());
                // 计算分数
                long distance = AlgorithmUtils.minDistance(tagList, userTagList);
                // 加入优先队列
                if (listTop.isEmpty() || listTop.size() < num) {
                    listTop.offer(new Pair<>(user, distance));
                }
                else {
                    Pair<User, Long> lastPair = listTop.poll();

                    if (lastPair.getValue() > distance) {
                        // 新节点比最小节点优先值大时 加入队列
                        listTop.offer(new Pair<>(user, distance));
                    }
                    else {
                        listTop.offer(lastPair);
                    }
                }
            }
            // 用了分页查找但是找到的用户相似度不满足numThreshold阈值设置 需要反复查找
            if(listTop.isEmpty() || listTop.peek().getValue() < distanceThreshold || count < numThreshold){
                break;
            }
            if(tryNum > tryNumMax) {
                break;
            }
            else {
                // 取出listTop中不符合的用户
                while(!listTop.isEmpty() && listTop.peek().getValue() > distanceThreshold) {
                    listTop.poll();
                }
            }
            tryNum ++;
        }

        List<Long> userIdList = new ArrayList<>();
        // 按编辑距离由小到大排序
        while (!listTop.isEmpty()) {
            Pair<User, Long> pair = listTop.poll();
            userIdList.add(pair.getKey().getId());
        }
        Collections.reverse(userIdList);

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        // 1, 3, 2
        // User1、User2、User3
        // 1 => User1, 2 => User2, 3 => User3
        User finalLoginUser = loginUser;
        Map<Long, List<UserFriendsVo>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(user -> getUserFriendsVo(user, finalLoginUser))
                .collect(Collectors.groupingBy(UserFriendsVo::getId));
        List<UserFriendsVo> finalUserList = new ArrayList<>();
        // 安装匹配度顺序返回
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
//        long time2 = System.currentTimeMillis();
//        log.info("matchUsers cost time: {}", time2 - startTime);
        return finalUserList;
    }


    @Override
    public List<UserFriendsVo> searchNearby(int radius, User loginUser) {
        String geoKey = RedisConstant.USER_GEO_KEY;
        String userId = String.valueOf(loginUser.getId());
        Double longitude = loginUser.getLongitude();
        Double dimension = loginUser.getDimension();
        if (longitude == null || dimension == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "登录用户经纬度参数为空");
        }
        Distance geoRadius = new Distance(radius, RedisGeoCommands.DistanceUnit.KILOMETERS);
        Circle circle = new Circle(new Point(longitude, dimension), geoRadius);
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo()
                .radius(geoKey, circle);
        List<Long> userIdList = new ArrayList<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results) {
            String id = result.getContent().getName();
            if (!userId.equals(id)) {
                userIdList.add(Long.parseLong(id));
            }
        }
        List<UserFriendsVo> userVOList = userIdList.stream().map(
                id -> {
//                    UserFriendsVo userVO = new UserFriendsVo();
                    User user = this.getById(id);
//                    BeanUtils.copyProperties(user, userVO);
//                    Distance distance = stringRedisTemplate.opsForGeo().distance(geoKey, userId, String.valueOf(id),
//                            RedisGeoCommands.DistanceUnit.KILOMETERS);
//                    userVO.setDistance(distance.getValue());
                    return getUserFriendsVo(user, loginUser);
                }
        ).collect(Collectors.toList());
        return userVOList.stream().filter(userFriendsVo -> userFriendsVo.getDistance()!=null).collect(Collectors.toList());
    }

    private static UserVO transferToUserVO(String userVOJson) {
        return JSONUtil.toBean(userVOJson, UserVO.class);
    }

    private static UserFriendsVo transferToUserFriendsVO(String userVOJson) {
        return JSONUtil.toBean(userVOJson, UserFriendsVo.class);
    }


}

