package com.youlai.boot.portal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlai.boot.portal.model.entity.PaperSubmissionEmail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 论文投稿发送邮件记录Mapper
 *
 * @author 2432691112
 * @since 2026/5/13
 */
@Mapper
public interface PaperSubmissionEmailMapper extends BaseMapper<PaperSubmissionEmail> {
}
