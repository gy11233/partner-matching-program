# partner-matching-program 趣友圈

## 项目介绍
趣友圈是一个移动端网页的在线云交友平台。
用户可以根据标签匹配用户，可以根据距离搜索附近用户。用户可以添加感兴趣的人为好友，并基于Websocket 实现好友间私聊。
用户可以根据兴趣建立队伍，也可以加入其他人建立的队伍，方便用户找到感兴趣的圈子、结交趣友。
### 线上地址
[趣友圈](http://www.joinfun.online/)

账号:admin

密码:12345678

## 技术选型
| **技术**         | **用途**                         | **版本** |
|----------------|--------------------------------|--------|
| Spring Boot    | 快速Spring应用程序的后端框架              | 2.6.4  |
| JDK            | Java开发工具包                      | 1.8    |
| MyBatis        | 持久层框架，用于简化数据库操作的ORM框架          | 3.5.2  |
| MyBatis-Plus   | 基于MyBatis的增强框架，提供了CRUD、代码生成等功能 | 3.5.2  |
| MySQL          | 关系型数据库                         | 8.0.37 |
| Redis          | 非关系型数据库                        | 7.2.5  |
| WebSocket      | 双向通讯协议，用于实现好友聊天功能              | 2.4.1  |
| JUnit          | Java的单元测试框架，用于编写和执行自动化测试       | 4.13.2 |
| Knife4j        | 快速API文档生成工具                    | 2.0.7  |
| Lombok         | 简化代码，自动生成常见的Java代码样板           |        |
| Hutool         | 提供Java日常开发的工具集                 | 5.7.17 |

## 项目亮点
1. 使用编辑距离算法实现根据标签推荐相似用户功能，通过设置相似度阈值、运用优先队列等提升查询速度。
2. 为解决首次访问时主页加载过慢问题，采用Spring Scheduler定时任务实现缓存预热，并采用Redisson分布式锁保证分布式部署时定时任务不会重复执行。
3. 选用Redis GEO数据结构存储用户位置信息，支持通过地理位置查询附近用户。
4. 基于WebSocket通信协议建立持久性连接并进行双向通信，实现好友在线聊天功能。
5. 基于AOP实现全局登录拦截和日志输出
6. 基于AOP和Redis无入侵式添加布隆过滤器,解决雪崩穿透问题，利用Spring Scheduler定时任务实现布隆过滤器删除并更新。
7. 过期时间增加随机值缓解缓存雪崩问题。
8. 使用Redis List结构配合前端实现首页滚动分页功能。
9. 使用Knife4j + Swagger 自动生成后端接口文档。
10. 编写Dockerfile，依托云服务器实现镜像构建、容器部署。

## 功能展示
### 登录

![img.png](image/login.png)

### 注册

![img.png](image/register.png)

### 推荐页面

![img.png](image/index.png)

![img.png](image/match.png)

### 按标签匹配

![img.png](image/searchByTags.png)

![img.png](image/searchResult.png)

### 按距离搜索

![img.png](image/searchNearby.png)

### 添加好友

![img.png](image/addFriends.png)

![img.png](image/addFriends2.png)

### 好友聊天

![img.png](image/chat.png)

### 建立队伍

![img.png](image/team.png)

![img.png](image/addTeam.png)

### 更改个人信息

![img.png](image/userInfo.png)

![img.png](image/updateUserInfo.png)
