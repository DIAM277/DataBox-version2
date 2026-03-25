-- 创建数据库
CREATE DATABASE IF NOT EXISTS databox DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- 使用数据库
USE databox;

create table if not exists ai_doc_summary
(
    summary_id      varchar(20) not null comment '摘要ID'
        primary key,
    file_md5        varchar(32) not null comment '文件MD5',
    summary_content longtext    not null comment '摘要内容',
    create_time     datetime    not null comment '生成时间',
    constraint idx_file_md5
        unique (file_md5) comment '通过MD5保证全站唯一'
)
    comment 'AI智能文档摘要表';

create table if not exists email_code
(
    email       varchar(150) not null comment '邮箱',
    code        varchar(6)   not null comment '验证码',
    create_time datetime     null comment '创建时间',
    status      tinyint(1)   null comment '验证码状态：0未使用 1已使用',
    primary key (email, code)
)
    comment '邮箱验证码表';

create table if not exists file_favorite
(
    favorite_id int auto_increment comment '收藏ID'
        primary key,
    user_id     varchar(15) not null comment '用户ID',
    file_id     varchar(20) not null comment '被收藏的文件ID',
    create_time datetime    not null comment '收藏时间',
    constraint idx_user_file
        unique (user_id, file_id) comment '防止同一用户重复收藏'
)
    comment '用户文件收藏表';

create table if not exists file_info
(
    file_id          varchar(10)   not null comment '文件编号',
    user_id          varchar(10)   not null comment '文件所有者',
    file_md5         varchar(32)   null comment '文件MD5值',
    file_pid         varchar(10)   null comment '文件父级别ID',
    file_size        bigint        null comment '文件大小',
    file_name        varchar(200)  null comment '文件名称',
    file_cover       varchar(100)  null comment '文件缩略图',
    file_path        varchar(100)  null comment '文件路径',
    create_time      datetime      null comment '文件创建时间',
    last_update_time datetime      null comment '最近更新时间',
    folder_type      tinyint(1)    null comment '类型；0文件，1目录',
    file_category    tinyint(1)    null comment '文件分类:1：视频 2：音频 3：图片 4：文档 5：其他',
    file_type        tinyint(1)    null comment '文件类型:1：视频 2：音频 3：图片 4：pdf 5：doc 6:excel 7:txt 8:code 9:zip 10:其他',
    status           tinyint(1)    null comment '文件状态;1:转码失败  2：转码成功',
    recovery_time    datetime      null comment '加入回收站时间',
    del_flag         tinyint(1)    null comment '删除标记 0：删除 1：回收站 2：正常',
    reference_count  int default 0 not null,
    primary key (file_id, user_id)
)
    comment '文件信息记录表';

create index index_craete_time
    on file_info (create_time)
    comment '时间索引';

create index index_del_flag
    on file_info (del_flag);

create index index_file_md5
    on file_info (file_md5);

create index index_file_pid
    on file_info (file_pid);

create index index_recovery_time
    on file_info (recovery_time);

create index index_status
    on file_info (status);

create index index_user_id
    on file_info (user_id)
    comment '用户id索引';

create table if not exists file_share
(
    share_id    varchar(20)   not null comment '分享ID'
        primary key,
    file_id     varchar(10)   null comment '文件ID',
    user_id     varchar(10)   null comment '分享人ID',
    valid_type  tinyint(1)    null comment '有效期类型 0:1天 1:7天 2:30天 4:永久有效',
    expire_time datetime      null comment '失效时间',
    share_time  datetime      null comment '分享时间',
    code        varchar(5)    null comment '提取码',
    show_count  int default 0 null comment '浏览次数'
)
    comment '文件分享信息';

create table if not exists share_report
(
    report_id      int auto_increment comment '举报ID'
        primary key,
    share_id       varchar(20)       not null comment '分享ID',
    file_id        varchar(20)       not null comment '文件ID',
    report_user_id varchar(15)       null comment '举报人ID(未登录为空)',
    report_ip      varchar(50)       not null comment '举报人IP(防刷必备)',
    reason         varchar(200)      not null comment '举报原因',
    status         tinyint default 0 not null comment '状态 0:待处理 1:已封禁(通过) 2:正常(驳回)',
    create_time    datetime          not null comment '举报时间'
)
    comment '分享违规举报表';

create index idx_check_duplicate
    on share_report (share_id, file_id, status);

create table if not exists sys_message
(
    message_id  int auto_increment comment '消息ID'
        primary key,
    user_id     varchar(15)       not null comment '接收消息的用户ID',
    title       varchar(100)      not null comment '消息标题',
    content     varchar(500)      not null comment '消息正文内容',
    read_status tinyint default 0 not null comment '阅读状态 0:未读 1:已读',
    create_time datetime          not null comment '发送时间',
    del_flag    tinyint default 0 not null comment '0:正常 1:删除'
)
    comment '系统消息通知表';

create index idx_user_id
    on sys_message (user_id)
    comment '加速查询用户的消息';

create table if not exists sys_op_log
(
    log_id      int auto_increment comment '日志ID'
        primary key,
    user_id     varchar(15)       null comment '用户ID',
    user_name   varchar(50)       null comment '用户名',
    module      varchar(50)       not null comment '操作模块（如：文件管理）',
    action      varchar(50)       not null comment '操作动作（如：删除文件）',
    status      tinyint default 1 not null comment '状态 1:成功 0:失败',
    result_msg  varchar(255)      null comment '返回信息或异常提示',
    create_time datetime          not null comment '操作时间',
    detail      varchar(500)      null comment '操作详情(如：操作的文件名称列表)'
)
    comment '用户操作日志表';

create index idx_user_id
    on sys_op_log (user_id);

create table if not exists user_info
(
    user_id         varchar(10)  not null comment '用户ID'
        primary key,
    user_name       varchar(20)  null comment '用户名',
    email           varchar(150) null comment '邮箱',
    qq_open_id      varchar(35)  null comment '用户QQ',
    qq_avatar       varchar(150) null comment 'QQ头像',
    password        varchar(32)  null comment '密码',
    create_time     datetime     null comment '创建时间',
    last_login_time datetime     null comment '最后登陆时间',
    status          tinyint(1)   null comment '用户状态:0禁用 1启用',
    user_space      bigint       null comment '用户使用空间',
    total_space     bigint       null comment '总空间',
    constraint key_email
        unique (email) comment '邮箱唯一索引',
    constraint key_qq_open_id
        unique (qq_open_id) comment '唯一qq索引',
    constraint key_user_name
        unique (user_name) comment '用户名唯一索引'
)
    comment '用户信息表';

create table if not exists user_login_log
(
    log_id         int auto_increment comment '日志ID（主键）'
        primary key,
    user_id        varchar(15)  not null comment '用户ID',
    login_ip       varchar(50)  not null comment '登录IP地址',
    login_location varchar(100) null comment 'IP归属地（如：中国-广东-深圳）',
    login_time     datetime     not null comment '登录时间'
)
    comment '用户登录日志表';

create index idx_user_id
    on user_login_log (user_id)
    comment '用户ID索引，便于查询某人的登录记录';

