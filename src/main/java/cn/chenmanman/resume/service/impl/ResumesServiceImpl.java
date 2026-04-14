package cn.chenmanman.resume.service.impl;

import cn.chenmanman.resume.common.PageRequest;
import cn.chenmanman.resume.common.PageResult;
import cn.chenmanman.resume.common.error.ResumesErrorCode;
import cn.chenmanman.resume.common.exception.BusinessException;
import cn.chenmanman.resume.config.SeleniumProperties;
import cn.chenmanman.resume.domain.dto.resume.CreateResumesRequestPost;
import cn.chenmanman.resume.domain.dto.resume.ExportResumePdfRequestPost;
import cn.chenmanman.resume.domain.dto.resume.ExportResumePngRequestPost;
import cn.chenmanman.resume.domain.dto.resume.UpdateResumesDraftRequestPut;
import cn.chenmanman.resume.domain.entity.resume.ResumesEntity;
import cn.chenmanman.resume.domain.entity.resume.TemplatesEntity;
import cn.chenmanman.resume.domain.vo.resume.MyResumesVO;
import cn.chenmanman.resume.domain.vo.resume.ResumePdfVO;
import cn.chenmanman.resume.domain.vo.resume.ResumesVO;
import cn.chenmanman.resume.mapper.ResumesMapper;
import cn.chenmanman.resume.mapper.TemplatesMapper;
import cn.chenmanman.resume.service.IResumesService;
import cn.chenmanman.resume.utils.BizAssert;
import cn.chenmanman.resume.utils.PdfDriverManager;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ResumesServiceImpl implements IResumesService {

    private final ResumesMapper resumesMapper;
    private final TemplatesMapper templatesMapper;
    private final ObjectMapper objectMapper;
    private final PdfDriverManager pdfDriverManager;
    private final SeleniumProperties seleniumProperties;
    private static final String PRINT_PATH = "/maker/print";

    @Transactional
    @Override
    public ResumesVO createResume(CreateResumesRequestPost request) {
        Long userId = StpUtil.getLoginIdAsLong();
        TemplatesEntity template = requireActiveTemplate(request.getTemplateId());

        ResumesEntity resume = ResumesEntity.builder()
                .userId(userId)
                .title(request.getTitle())
                .templateId(template.getId())
                .status("draft")
                .contentJson(toJsonStorage(resolveInitialContent(template)))
                .layoutJson(toJsonStorage(resolveInitialLayout(template)))
                .createBy(userId)
                .updateBy(userId)
                .build();
        resumesMapper.insert(resume);
        incrementTemplateUsage(template.getId());

        return getResumeDetail(resume.getId());
    }

    @Override
    public ResumesVO getResumeDetail(Long resumeId) {
        Long userId = StpUtil.getLoginIdAsLong();
        ResumesEntity resume = requireOwnedResume(resumeId, userId);
        return buildResumeVO(resume);
    }

    @Override
    public ResumesVO getResumeDetailNoLogin(Long resumeId) {
        ResumesEntity resume = resumesMapper.selectById(resumeId);
        return buildResumeVO(resume);
    }

    @Transactional
    @Override
    public ResumesVO updateDraft(Long resumeId, UpdateResumesDraftRequestPut request) {
        Long userId = StpUtil.getLoginIdAsLong();
        ResumesEntity resume = requireOwnedResume(resumeId, userId);
        TemplatesEntity template = requireActiveTemplate(request.getTemplateId());
        Long previousTemplateId = resume.getTemplateId();

        ResumesEntity resumeUpdate = new ResumesEntity();
        resumeUpdate.setId(resume.getId());
        resumeUpdate.setTitle(request.getTitle());
        resumeUpdate.setTemplateId(template.getId());
        resumeUpdate.setStatus("draft");
        resumeUpdate.setContentJson(toJsonStorage(request.getContentJson()));
        resumeUpdate.setLayoutJson(toJsonStorage(request.getLayoutJson()));
        resumeUpdate.setUpdateBy(userId);
        resumesMapper.updateById(resumeUpdate);

        if (previousTemplateId == null || !previousTemplateId.equals(template.getId())) {
            incrementTemplateUsage(template.getId());
        }

        return getResumeDetail(resumeId);
    }

    @Override
    public ResumePdfVO exportPdf(Long resumeId) {
        validateExportAccess(resumeId);
        ResumesEntity resumesEntity = resumesMapper.selectById(resumeId);
        String s = seleniumProperties.getUrl() + PRINT_PATH +"?resumeId="+resumeId;
        byte[] execute = pdfDriverManager.execute(driver -> doExport(driver, s));
        log.info("url: {}", s);
        ResumePdfVO resumePdfVO = new ResumePdfVO();
        resumePdfVO.setResumeName(resumesEntity.getTitle());
        resumePdfVO.setResume(execute);
        return resumePdfVO;
    }


    private byte[] doExport(ChromeDriver driver, String url) {
        driver.get(url);
        new WebDriverWait(driver, Duration.ofSeconds(40))
                .until(d -> Boolean.TRUE.equals(
                        ((JavascriptExecutor) d).executeScript("""
            return window.__PDF_READY__ === true;
        """)
                ));

        DevTools devTools = driver.getDevTools();
        devTools.createSession();

        org.openqa.selenium.devtools.v146.page.Page.PrintToPDFResponse response = devTools.send(
                org.openqa.selenium.devtools.v146.page.Page.printToPDF(
                        Optional.of(false),      // landscape
                        Optional.of(false),      // displayHeaderFooter
                        Optional.of(true),       // printBackground
                        Optional.of(1.0),        // scale
                        Optional.of(8.27),       // A4 width
                        Optional.of(11.69),      // A4 height
                        Optional.of(0.0),        // top
                        Optional.of(0.0),        // bottom
                        Optional.of(0.0),        // left
                        Optional.of(0.0),        // right
                        Optional.empty(),        // pageRanges
                        Optional.empty(),        // headerTemplate
                        Optional.empty(),        // footerTemplate
                        Optional.of(true),       // preferCSSPageSize
                        Optional.empty(),        // transferMode
                        Optional.of(false),      // generateTaggedPDF
                        Optional.of(false)       // generateDocumentOutline
                )
        );

        return Base64.getDecoder().decode(response.getData());
    }

    @Override
    public void exportPng(Long resumeId, ExportResumePngRequestPost request) {
        validateExportAccess(resumeId);
        BizAssert.fail(ResumesErrorCode.RESUME_EXPORT_NOT_SUPPORTED);
    }

    @Override
    public List<MyResumesVO> listResumesMe() {
        Long userId = StpUtil.getLoginIdAsLong();

        return resumesMapper.getResumeMeList(userId);
    }

    @Override
    public PageResult<MyResumesVO> pageResumesMe(PageRequest pageRequest) {
        Long userId = StpUtil.getLoginIdAsLong();
        PageRequest currentPageRequest = pageRequest == null ? new PageRequest() : pageRequest;
        int currentPageNum = currentPageRequest.getSafePageNum();
        int currentPageSize = currentPageRequest.getSafePageSize();

        Page<MyResumesVO> page = resumesMapper.getResumeMePage(new Page<>(currentPageNum, currentPageSize), userId);
        PageResult<MyResumesVO> pageResult = new PageResult<>();
        pageResult.setTotal(page.getTotal());
        pageResult.setPageNum(currentPageNum);
        pageResult.setPageSize(currentPageSize);
        pageResult.setList(page.getRecords());
        return pageResult;
    }

    private void validateExportAccess(Long resumeId) {
        Long userId = StpUtil.getLoginIdAsLong();
        requireOwnedResume(resumeId, userId);
    }

    private TemplatesEntity requireActiveTemplate(Long templateId) {
        TemplatesEntity template = templatesMapper.selectById(templateId);
        BizAssert.notNull(template, ResumesErrorCode.TEMPLATE_NOT_FOUND);
        BizAssert.equals(template.getIsDeleted(), 0, ResumesErrorCode.TEMPLATE_NOT_FOUND);
        BizAssert.equals(template.getIsActive(), 1, ResumesErrorCode.TEMPLATE_DISABLED);
        return template;
    }

    private void incrementTemplateUsage(Long templateId) {
        templatesMapper.update(null, Wrappers.<TemplatesEntity>lambdaUpdate()
                .eq(TemplatesEntity::getId, templateId)
                .setSql("usage_number = IFNULL(usage_number, 0) + 1"));
    }

    private ResumesEntity requireOwnedResume(Long resumeId, Long userId) {
        LambdaQueryWrapper<ResumesEntity> queryWrapper = new LambdaQueryWrapper<ResumesEntity>()
                .eq(ResumesEntity::getId, resumeId)
                .eq(ResumesEntity::getUserId, userId)
                .eq(ResumesEntity::getIsDeleted, 0)
                .last("limit 1");
        ResumesEntity resume = resumesMapper.selectOne(queryWrapper);
        BizAssert.notNull(resume, ResumesErrorCode.RESUME_NOT_FOUND);
        return resume;
    }

    private ResumesVO buildResumeVO(ResumesEntity resume) {
        return ResumesVO.builder()
                .resumeId(resume.getId())
                .title(resume.getTitle())
                .templateId(resume.getTemplateId())
                .status(resume.getStatus())
                .contentJson(fromJsonStorage(resume.getContentJson()))
                .layoutJson(fromJsonStorage(resume.getLayoutJson()))
                .build();
    }

    private Object resolveInitialContent(TemplatesEntity template) {
        return resolveTemplateJson(template.getDefaultContentJson(), template.getSchemaJson());
    }

    private Object resolveInitialLayout(TemplatesEntity template) {
        return resolveTemplateJson(template.getStyleJson(), template.getSchemaJson());
    }

    private Object resolveTemplateJson(Object preferredValue, Object fallbackValue) {
        Object resolvedValue = fromJsonStorage(preferredValue);
        if (resolvedValue != null) {
            return resolvedValue;
        }

        resolvedValue = fromJsonStorage(fallbackValue);
        if (resolvedValue != null) {
            return resolvedValue;
        }

        return Collections.emptyMap();
    }

    private String toJsonStorage(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String stringValue) {
            return stringValue;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BusinessException("JSON序列化失败");
        }
    }

    private Object fromJsonStorage(Object value) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof String stringValue)) {
            return value;
        }
        try {
            return objectMapper.readValue(stringValue, Object.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse resume json", e);
            return stringValue;
        }
    }
}
