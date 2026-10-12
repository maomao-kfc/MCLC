package com.youlai.boot.support.mail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.support.mail.model.entity.MailSendRecord;
import com.youlai.boot.support.mail.model.query.MailSendRecordQuery;
import com.youlai.boot.support.mail.model.vo.MailSendRecordPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 邮件发送记录 Mapper
 *
 * @author system
 * @since 2026/7/24
 */
@Mapper
public interface MailSendRecordMapper extends BaseMapper<MailSendRecord> {

    /**
     * 分页查询邮件发送记录（仅查询原始记录，重试记录通过children返回）
     */
    Page<MailSendRecordPageVO> getMailSendRecordPage(Page<MailSendRecordPageVO> page, MailSendRecordQuery query);

    /**
     * 根据根记录ID列表批量查询重试记录（用于组装树形结构）
     *
     * @param rootIds 根记录ID列表
     * @return 重试记录列表
     */
    List<MailSendRecordPageVO> getRetryRecordsByRootIds(@Param("rootIds") List<Long> rootIds);
}