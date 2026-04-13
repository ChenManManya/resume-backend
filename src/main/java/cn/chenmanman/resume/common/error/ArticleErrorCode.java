package cn.chenmanman.resume.common.error;

import lombok.Getter;

@Getter
public enum ArticleErrorCode implements ErrorCode {

    ARTICLE_NOT_FOUND(700, "文章不存在!"),
    ARTICLE_STATUS_INVALID(701, "文章状态不合法!"),
    ARTICLE_TAGS_INVALID(702, "文章标签格式不合法!"),
    ARTICLE_COVER_FILE_EMPTY(703, "文章封面文件不能为空"),
    ARTICLE_COVER_UPLOAD_FAILED(704, "文章封面上传失败"),
    ARTICLE_COVER_FILE_TYPE_INVALID(705, "文章封面仅支持 png/jpg/jpeg/gif/webp 图片格式"),
    ARTICLE_COVER_FILE_TOO_LARGE(706, "文章封面文件大小超出限制"),
    ARTICLE_TITLE_EMPTY(707, "文章标题不能为空!"),
    ARTICLE_CONTENT_EMPTY(708, "文章内容不能为空!");

    private final Integer code;
    private final String message;

    ArticleErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
