package com.youlai.boot.portal.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.portal.model.entity.PaperSubmission;
import com.youlai.boot.portal.model.form.PaperSubmissionForm;
import com.youlai.boot.portal.model.vo.PaperSubmissionPageVO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 论文投稿对象转换器
 *
 * @author 243269112
 * @since 2026/5/6
 */
@Mapper(componentModel = "spring")
public interface PaperSubmissionConverter {

    PaperSubmissionPageVO toPageVo(PaperSubmission paperSubmission);

    Page<PaperSubmissionPageVO> toPageVo(Page<PaperSubmission> page);

    PaperSubmission toEntity(PaperSubmissionForm paperSubmissionForm);

    PaperSubmissionForm toForm(PaperSubmission entity);

    List<PaperSubmissionPageVO> toPageVo(List<PaperSubmission> bo);
}
