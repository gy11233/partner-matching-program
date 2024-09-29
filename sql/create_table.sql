create
database if not exists partner;

use
partner;

/**
  仅仅用作 es 测试
 */
create
    database if not exists partner_es;

use
    partner_es;

drop table user;
-- 用户表
create table user
(
    id           bigint auto_increment comment 'id' primary key,
    username     varchar(256) null comment '用户昵称',
    user_account  varchar(256) null comment '账号',
    avatar_url    varchar(1024) null comment '用户头像' default 'https://www.keaitupian.cn/cjpic/frombd/0/253/17551321/2476952379.jpg',
    gender       tinyint null comment '性别',
    profile      varchar(512)                       null comment '个人简介',
    user_password varchar(512)       not null comment '密码',
    phone        varchar(128) null comment '电话',
    email        varchar(512) null comment '邮箱',
    user_status   int      default 0 not null comment '状态 0 - 正常',
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_delete     tinyint  default 0 not null comment '是否删除',
    user_role     int      default 0 not null comment '用户角色 0 - 普通用户 1 - 管理员',
    planet_code   varchar(512) null comment '星球编号',
    tags         varchar(1024) null comment '标签 json 列表',
    longitude    decimal(10, 6)                     null comment '经度',
    dimension    decimal(10, 6)                     null comment '纬度'
) comment '用户';



-- 导入示范用户
INSERT INTO partner.user
(username, user_account, avatar_url, gender, user_password, phone, email, user_status, create_time, update_time, is_delete, user_role, planet_code, tags)
VALUES ('lily', '123', 'https://img.keaitupian.cn/newupload/08/1723015536445088.jpg',
        null, 'b0dd3697a192885d7c055db46155b26a', null, null, 0, '2024-03-06 14:14:22', '2024-03-06 14:39:37', 0, 1, '1', '["java","c++","python"]');

select * from partner.user;




use
    partner;

-- 队伍表
create table team
(
    id           bigint auto_increment comment 'id' primary key,
    name     varchar(256) null comment '队伍名称',
    description    varchar(1024) null comment '队伍描述',
    max_num   int      default 1 not null comment '最大人数',
    expire_time   datetime   null comment '过期时间',
    user_id           bigint not null  comment '创建人id',
    status   int      default 0 not null comment '状态 0 - 公开 1-私有 2-加密',
    password varchar(512)       null comment '队伍密码',
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_delete     tinyint  default 0 not null comment '是否删除'

) comment '队伍';


-- 队伍-用户 关系表
create table user_team
(
    id           bigint auto_increment comment 'id' primary key,
    user_id           bigint not null  comment '用户id',
    team_id           bigint not null  comment '队伍id',
    join_time   datetime  null comment '加入时间',
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_delete     tinyint  default 0 not null comment '是否删除'

) comment '队伍-用户关系表';


/*好友表*/
create table friend
(
    id         bigint auto_increment comment 'id'
        primary key,
    user_id     bigint                             not null comment '用户id（即自己id）',
    friend_id   bigint                             not null comment '好友id',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint  default 0                 not null comment '是否删除'
)
    comment '好友表';


/*聊天表*/
CREATE TABLE chat  (
   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '聊天记录id',
   `from_id` bigint(20) NOT NULL COMMENT '发送消息id',
   `to_id` bigint(20) NULL DEFAULT NULL COMMENT '接收消息id',
   `text` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
   `chat_type` tinyint(4) NOT NULL COMMENT '聊天类型 1-私聊 2-群聊',
   `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
   `team_id` bigint(20) NULL DEFAULT NULL,
   `is_delete` tinyint(4) NULL DEFAULT 0,
   PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '聊天消息表' ROW_FORMAT = COMPACT;

drop table partner.friend;
drop table partner.chat;



