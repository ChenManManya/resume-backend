-- =========================
-- 模板表
-- =========================
CREATE TABLE templates (
                           id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '模板ID',
                           code VARCHAR(50) NOT NULL COMMENT '模板编码，如 minimal / online',
                           name VARCHAR(100) NOT NULL COMMENT '模板名称',
                           description VARCHAR(500) DEFAULT NULL COMMENT '模板描述',
                           preview_image_url VARCHAR(500) DEFAULT NULL COMMENT '模板预览图',
                           category VARCHAR(50) DEFAULT NULL COMMENT '模板分类',
                           is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
                           schema_json JSON NOT NULL COMMENT '模板结构配置',
                           style_json JSON NOT NULL COMMENT '模板样式配置',
                           create_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
                           update_by BIGINT UNSIGNED DEFAULT NULL COMMENT '修改人',
                           create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                           is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                           PRIMARY KEY (id),
                           UNIQUE KEY uk_templates_code (code),
                           KEY idx_templates_category (category),
                           KEY idx_templates_create_by (create_by),
                           KEY idx_templates_update_by (update_by),
                           CONSTRAINT fk_templates_create_by
                               FOREIGN KEY (create_by) REFERENCES sys_user(id)
                                   ON DELETE SET NULL
                                   ON UPDATE CASCADE,
                           CONSTRAINT fk_templates_update_by
                               FOREIGN KEY (update_by) REFERENCES sys_user(id)
                                   ON DELETE SET NULL
                                   ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历模板表';
-- =========================
-- 简历主表
-- =========================
CREATE TABLE resumes (
                         id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '简历ID',
                         user_id BIGINT UNSIGNED NOT NULL COMMENT '所属用户ID',
                         title VARCHAR(200) NOT NULL COMMENT '简历标题',
                         template_id BIGINT UNSIGNED DEFAULT NULL COMMENT '当前模板ID',
                         status VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态：draft/published/archived',
                         current_version_id BIGINT UNSIGNED DEFAULT NULL COMMENT '当前版本ID',
                         create_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
                         update_by BIGINT UNSIGNED DEFAULT NULL COMMENT '修改人',
                         create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                         is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                         PRIMARY KEY (id),
                         KEY idx_resumes_user_id (user_id),
                         KEY idx_resumes_template_id (template_id),
                         KEY idx_resumes_status (status),
                         KEY idx_resumes_current_version_id (current_version_id),
                         KEY idx_resumes_create_by (create_by),
                         KEY idx_resumes_update_by (update_by),
                         CONSTRAINT fk_resumes_user
                             FOREIGN KEY (user_id) REFERENCES sys_user(id)
                                 ON DELETE CASCADE
                                 ON UPDATE CASCADE,
                         CONSTRAINT fk_resumes_template
                             FOREIGN KEY (template_id) REFERENCES templates(id)
                                 ON DELETE SET NULL
                                 ON UPDATE CASCADE,
                         CONSTRAINT fk_resumes_create_by
                             FOREIGN KEY (create_by) REFERENCES sys_user(id)
                                 ON DELETE SET NULL
                                 ON UPDATE CASCADE,
                         CONSTRAINT fk_resumes_update_by
                             FOREIGN KEY (update_by) REFERENCES sys_user(id)
                                 ON DELETE SET NULL
                                 ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历主表';
-- =========================
-- 简历版本 / 快照表
-- =========================
CREATE TABLE resume_versions (
                                  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '版本ID',
                                  resume_id BIGINT UNSIGNED NOT NULL COMMENT '简历ID',
                                  title VARCHAR(200) NOT NULL COMMENT '版本快照标题',
                                  template_id BIGINT UNSIGNED DEFAULT NULL COMMENT '版本快照模板ID',
                                  version_no INT NOT NULL COMMENT '版本号',
                                  content_json JSON NOT NULL COMMENT '简历内容JSON',
                                  layout_json JSON NOT NULL COMMENT '布局配置JSON',
                                 snapshot_html LONGTEXT DEFAULT NULL COMMENT '渲染后的HTML快照',
                                 change_note VARCHAR(255) DEFAULT NULL COMMENT '版本说明',
                                 create_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
                                 update_by BIGINT UNSIGNED DEFAULT NULL COMMENT '修改人',
                                 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                 is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                  PRIMARY KEY (id),
                                  UNIQUE KEY uk_resume_versions_resume_version (resume_id, version_no),
                                  KEY idx_resume_versions_resume_id (resume_id),
                                  KEY idx_resume_versions_template_id (template_id),
                                  KEY idx_resume_versions_created_by (create_by),
                                  KEY idx_resume_versions_updated_by (update_by),
                                  KEY idx_resume_versions_create_time (create_time),
                                 KEY idx_resume_versions_update_time (update_time),
                                  CONSTRAINT fk_resume_versions_resume
                                      FOREIGN KEY (resume_id) REFERENCES resumes(id)
                                          ON DELETE CASCADE
                                          ON UPDATE CASCADE,
                                  CONSTRAINT fk_resume_versions_template
                                      FOREIGN KEY (template_id) REFERENCES templates(id)
                                          ON DELETE SET NULL
                                          ON UPDATE CASCADE,
                                  CONSTRAINT fk_resume_versions_created_by
                                      FOREIGN KEY (create_by) REFERENCES sys_user(id)
                                          ON DELETE SET NULL
                                         ON UPDATE CASCADE,
                                 CONSTRAINT fk_resume_versions_updated_by
                                     FOREIGN KEY (update_by) REFERENCES sys_user(id)
                                         ON DELETE SET NULL
                                         ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历版本快照表';
-- =========================
-- 回填 resumes.current_version_id 外键
-- =========================
ALTER TABLE resumes
    ADD CONSTRAINT fk_resumes_current_version
        FOREIGN KEY (current_version_id) REFERENCES resume_versions(id)
            ON DELETE SET NULL
            ON UPDATE CASCADE;
