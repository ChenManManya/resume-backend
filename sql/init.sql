create table resumes
(
    id           bigint unsigned auto_increment comment '简历ID'
        primary key,
    user_id      bigint unsigned                       not null comment '所属用户ID',
    title        varchar(200)                         not null comment '简历标题',
    template_id  bigint unsigned                       null comment '来源模板ID',
    status       varchar(20) default 'draft'          not null comment '状态：draft/published/archived',
    content_json json                                  not null comment '当前简历内容JSON',
    layout_json  json                                  not null comment '当前布局配置JSON',
    create_by    bigint unsigned                       null comment '创建人',
    update_by    bigint unsigned                       null comment '修改人',
    create_time  datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time  datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted   tinyint(1)  default 0                 not null comment '逻辑删除'
)
    comment '简历主表'
    collate = utf8mb4_unicode_ci;
create index idx_resumes_user_id
    on resumes (user_id);
create index idx_resumes_template_id
    on resumes (template_id);
create index idx_resumes_status
    on resumes (status);
create index idx_resumes_create_by
    on resumes (create_by);
create index idx_resumes_update_by
    on resumes (update_by);
create index idx_resumes_update_time
    on resumes (update_time);