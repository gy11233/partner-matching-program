create
database if not exists partner;

use
partner;

-- 用户表
create table user
(
    id           bigint auto_increment comment 'id' primary key,
    name     varchar(256) null comment '用户昵称',
    user_account  varchar(256) null comment '账号',
    avatar_url    varchar(1024) null comment '用户头像',
    gender       tinyint null comment '性别',
    user_password varchar(512)       not null comment '密码',
    phone        varchar(128) null comment '电话',
    email        varchar(512) null comment '邮箱',
    user_status   int      default 0 not null comment '状态 0 - 正常',
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_delete     tinyint  default 0 not null comment '是否删除',
    user_role     int      default 0 not null comment '用户角色 0 - 普通用户 1 - 管理员',
    planet_code   varchar(512) null comment '星球编号',
    tags         varchar(1024) null comment '标签 json 列表'
) comment '用户';


-- 标签表
create table tag
(
    id           bigint auto_increment comment 'id' primary key,
    tag_name     varchar(256) null comment '标签名',
    user_id      bigint       null comment '上传标签的用户id',
    parent_id    bigint          null comment '父标签id',
    is_parent       tinyint null comment '是否为父标签',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete     tinyint  default 0 not null comment '是否删除'

) comment '标签';

create unique index unique_tag_name on tag(tag_name);
create index index_user_id on tag(user_id);


-- 导入示范用户
INSERT INTO partner.user
(username, user_account, avatar_url, gender, user_password, phone, email, user_status, create_time, update_time, is_delete, user_role, planet_code, tags)
VALUES ('lily', '123', 'https://himg.bdimg.com/sys/portraitn/item/public.1.e137c1ac.yS1WqOXfSWEasOYJ2-0pvQ',
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





