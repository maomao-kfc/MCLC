package com.youlai.boot.portal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.portal.model.entity.PaperSubmission;
import com.youlai.boot.portal.model.query.PaperSubmissionQuery;
import com.youlai.boot.portal.model.vo.PaperSubmissionPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 论文投稿Mapper
 *
 * @author 243269112
 * @since 2026/5/6
 */
@Mapper
public interface PaperSubmissionMapper extends BaseMapper<PaperSubmission> {

    Page<PaperSubmissionPageVO> selectPaperSubmissionPage(
            Page<PaperSubmissionPageVO> page,
            @Param("query") PaperSubmissionQuery query
    );
}
