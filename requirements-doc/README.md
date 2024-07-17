# 伙伴匹配系统

## 需求分析
1. 用户添加标签，标签的分类
2. 主动搜索：用户更加标签搜索其他用户
    1. redis缓存
3. 组队：创建队伍，加入队伍，查询队伍，邀请其他人
4. 允许用户修改标签
5. 推荐： 相似度计算算法+本地分布式计算


## 技术栈
### 前端
1. Vue3 开发框架
2. Vant UI
3. Vite 打包工具
4. Nginx单机部署

### 后端
1. java编程语言+springboot框架
2. springMVc + Mybatis + MyBatis Plus
3. MySQL数据库
4. Redis缓存
5. Swagger + Knife4j接口文档


## 主要流程

### 第一期任务
1. 数据库表设计
   1. 标签表
   2. 用户表
2. 开发后端 根据标签搜索用户
3. 后端整合Swagger+Knife4j接口文档
4. 存量用户信息导入
5. 前后端联调 使用@CrossOrigin注解

### 第二期任务
1. 页面和功能开发 前端
2. 改造用户中心，把单机登录改成分布式session登录

### 第三期任务
1. 用户修改页面的后端开发
2. 开发主页（默认推荐和自己兴趣相当的用户）
   - 开发/user/recommend后端接口
   - 导入数据 尽量一个量级一个量级增加 分批导入 写程序导入 更可控
3. 优化主页的性能（缓存+定时任务）


## 模块实现

### 数据库表设计

#### 1. 标签表
建议用标签不用分类 更加灵活

**标签的设计**
性别：男女  
方向：java  c++ go  
目标： 考研 求职 球招 社招 考公 竞赛  
段位： 入门 中级 高级  
身份： 大一 大二 大三 大四 研一 研二 研三   
状态： 单身 已婚 有对象
【用户自己定义标签】
如果标签固定的 是不是可以直接存在后端？？？

**字段**

id int 主键   
标签名 varchar 非空 （**必须唯一 唯一索引**）
上传标签的用户id user_id int  （如果要要根据userid查看已经上传标签的话，最好加上 普通索引）
父标签id parent_id int  
是否为父标签 is_parent tinyint(0, 1) **尽量不要用布尔 不灵活**  
创建时间 create_time   datetime
更新时间 update_time   datetime
是否删除 is_delete tinyint(0,1)

考虑的问题 怎么查询标签并分组？ 按照父标签id分组
根据父标签查询子标签？查服标签的id

标签字段设计的一般都有的属性
id 创建时间 更新时间 是否删除

#### 2. 用户表
用户可以知道有哪些标签 一对多的关系
   1. 用户表中加tags标签 【‘java’, '男'】存json字符串 ✅
      - 优点：查询用户标签比较方便 不用新建关联表，（标签是固有属性 除了系统其他系统页可能用）
        之后随着人数增多 20w+ 效率低了 也可以考虑用缓存的技术来缓存java标签下的所有用户
      - 缺点：通过标签查询用户不方便 用like效率很低 更新用户表或者更新标签很麻烦
   2. 关联表 记录用户和标签的关系 
      - 优点：正反查都很方便
      - 重点：企业大型项目开发中尽量减少关联查询，会应用查询系统

**字段**

id bigint 自增 主键  
用户昵称 username varchar  
用户账号 user_account  
用户头像 user_url  
用户性别 gender  
用户密码 password varchar  
电话 phone  
邮箱 email  
用户状态 user_status  
创建时间 create_time   datetime  
更新时间 update_time   datetime  
是否删除 is_delete tinyint(0,1)  
用户角色 user_role  
星球编号 planet_code  
标签 tags  varchar(1024)


### 主要功能

#### 用户中心提供用户检索 登陆 修改 鉴权
TODO: 细致分析用户中中心


#### 通过标签搜索用户功能
1. 允许用户输入标签，多个标签都存在才搜索出来 and  like "%java%" and like "%C++" 
2. 允许用户传入多个标签，只要有一个就搜索出来 or  like "%java%" or like "%C++"

两种查询方法：
1. sql查询
2. 内存查询MybatisX-Generator插件生成关于表的基本代码

> tips:可以通过MybatisX-Generator操作

**实现思路**
service实现 `searchUsersByTags`
- 标签不能为空 否则返回异常
- 用sql的方式实现
- 用内存的方式实现（可以通过并发）

#### 用户注册 登陆 （解决分布式登录问题）
模拟分布式登录：
```shell
java -jar 项目jar包 --server.port=8081
```
##### 服务器A登录后，请求发送到服务器B不认识用户
用户在A登录，所以session存储在了A中，请求B时，B没有用户信息
解决方案：共享存储，把用户信息放在**公共存储**中
如何共享存储？
1. mysql
2. redis ✅ 用户信息读取/是否登录的判断及其**频繁** 基于内存 读写性能很高 单机qps 5w-10w
3. 文件服务器 ceph

##### redis安装与使用
> https://blog.csdn.net/realize_dream/article/details/106227622

版本 redis 7.2.5 
管理工具 quick redis 

基本命令:
```shell
brew services start redis
redis-cli
redis-cli -h 127.0.0.1 -p 6379
redis-cli shutdown
```
引入mvn包 
配置application.yml
- 配置redis
- 配置session存储方式为redis store-type: redis 从redis中读写session

#### 修改用户信息
1. 判断参数是否为空
23可以写到service层
2. 校验权限
3. 触发更新

----
### 后端整合Swagger+Knife4j接口文档
每条接口内容
- 请求参数
- 响应参数
  - 错误码
- 接口地址
- 接口名称
- 请求类型
- 请求格式
- 备注

swagger https://blog.csdn.net/hadues/article/details/123753888
1. maven中引入
2. 配置config下文件 在SwaggerConfig类上加@Profile({"dev", "test"})注解 不暴露接口给外部
3. 配置需要生成接口文档的包
4. 配置application.yml(因为版本兼容带来的问题)

### 批量导入数据
1. 写程序导入
2. 用mybatis-plus的批量导入加速
3. 




----
## 改进和思考
1. 连接池用的jdbc 可以之后替换成druid连接池
2. 查询的两种方式 内存和sql 数据量大的时候实际看哪个快用哪个 如果接近： 
   - 如果参数可以分析，根据用户的参数选择查询方式，比如标签数 大于某个值内存更快
   - 如果参数不可以分析，并且数据库连接足够内存足够，可以并发查询，谁先返回用哪个
   - sql与内存结合，比如用sql先过滤到一部分tag
   - **之后可以多放数据选择合适的**
3. java8 parallelStream 去了解一下
4. session 和 cookie区别
5. 对比之前request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser)
的实现方式和换成redis存储的方式
6. jwt和session比较 https://www.cnblogs.com/ls1519/p/13428380.html 
7. 设置用户权限的方式  spring security 没必要用 细粒度权限控制
8. 
