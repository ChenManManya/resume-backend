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


DROP TABLE IF EXISTS  articles;
CREATE TABLE articles (
      id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
      cover_url VARCHAR(512) DEFAULT NULL COMMENT '封面URL',
      title VARCHAR(200) NOT NULL COMMENT '文章标题',
      content LONGTEXT NOT NULL COMMENT '正文内容',
      tags JSON NOT NULL COMMENT '标签(JSON数组)',
      view_num int null comment '阅读量',
      status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0草稿 1已发布 2已下架',
      published_time DATETIME DEFAULT NULL COMMENT '发布时间',
      create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
          ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
      create_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
      update_by BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
      deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
      PRIMARY KEY (id),
      INDEX idx_status (status),
      INDEX idx_create_time (create_time),
      INDEX idx_published_time (published_time),
      INDEX idx_create_by (create_by)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
    COMMENT='文章表';




INSERT INTO articles
(cover_url, title, content, tags, view_num, status, published_time, create_by, update_by)
VALUES

    (
        '/uploads/article-cover/java1.jpg',
        'Spring Boot 权限系统从零实现 RBAC',
        '<p>本文详细讲解如何基于 Spring Boot + Sa-Token + MyBatis Plus 实现完整 RBAC 权限系统。</p>',
        JSON_ARRAY('Spring Boot', 'RBAC', '权限系统'),
        128,
        1,
        NOW(),
        1,
        1
    ),

    (
        '/uploads/article-cover/resume1.jpg',
        '程序员简历优化指南：面试官最想看到什么？',
        '<p>从招聘视角分析技术简历应该如何写，哪些项目经历最加分。</p>',
        JSON_ARRAY('简历优化', '求职技巧'),
        356,
        1,
        NOW(),
        1,
        1
    ),

    (
        '/uploads/article-cover/frontend1.jpg',
        'Nuxt 4 SSR 项目最佳实践',
        '<p>介绍 Nuxt 4 在 SSR 场景下的目录结构、数据请求、权限控制方案。</p>',
        JSON_ARRAY('Nuxt', 'SSR', '前端'),
        219,
        1,
        NOW(),
        1,
        1
    ),

    (
        '/uploads/article-cover/interview1.jpg',
        '2026 Java 面试八股文整理（最新版）',
        '<p>整理常见 Java 后端面试题，包括 JVM、并发、MySQL、Redis、Spring。</p>',
        JSON_ARRAY('Java', '面试', '八股文'),
        888,
        1,
        NOW(),
        1,
        1
    ),

    (
        '/uploads/article-cover/mysql1.jpg',
        'MySQL 索引失效的 12 种场景',
        '<p>深入讲解导致索引失效的常见原因及优化建议。</p>',
        JSON_ARRAY('MySQL', '数据库优化'),
        421,
        1,
        NOW(),
        1,
        1
    ),

    (
        '/uploads/article-cover/project1.jpg',
        '如何包装一个高质量的个人项目写进简历',
        '<p>教你如何把普通 CRUD 项目包装成面试亮点项目。</p>',
        JSON_ARRAY('项目经验', '简历优化'),
        512,
        1,
        NOW(),
        1,
        1
    ),

    (
        '/uploads/article-cover/redis1.jpg',
        'Redis 在登录认证中的实战应用',
        '<p>验证码缓存、Token 黑名单、分布式 Session 管理完整实践。</p>',
        JSON_ARRAY('Redis', '认证授权'),
        199,
        1,
        NOW(),
        1,
        1
    ),

    (
        '/uploads/article-cover/deploy1.jpg',
        'Spring Boot 项目 Docker 部署教程',
        '<p>从 Dockerfile 到 Docker Compose 完整部署流程。</p>',
        JSON_ARRAY('Docker', '部署', 'Spring Boot'),
        167,
        1,
        NOW(),
        1,
        1
    ),

    (
        '/uploads/article-cover/portfolio1.jpg',
        '前端工程师作品集应该如何设计？',
        '<p>分享高质量前端作品集页面设计思路和案例。</p>',
        JSON_ARRAY('前端', '作品集'),
        143,
        1,
        NOW(),
        1,
        1
    ),

    (
        '/uploads/article-cover/draft1.jpg',
        '这是一篇草稿文章示例',
        '<p>用于测试后台草稿状态展示。</p>',
        JSON_ARRAY('草稿'),
        0,
        0,
        NULL,
        1,
        1
    );