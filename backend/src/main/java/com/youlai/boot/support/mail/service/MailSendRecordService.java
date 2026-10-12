package com.youlai.boot.support.mail.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.support.mail.enums.MailBusinessType;
import com.youlai.boot.support.mail.model.entity.MailSendRecord;
import com.youlai.boot.support.mail.model.query.MailSendRecordQuery;
import com.youlai.boot.support.mail.model.vo.MailSendRecordPageVO;

/**
 * 邮件发送记录服务接口
 *
 * 核心设计理念：
 * 1. 任务表模式 —— 先插入记录(status=待发送)，再发送，最后更新状态
 * 2. 日志不覆盖 —— 每次发送都生成新记录，保留完整历史
 * 3. 可重试 —— 失败记录可手动重试，自动递增retry_count
 * 4. 可追溯 —— 记录Message-ID、耗时、错误信息，便于排查问题
 *
 * @author system
 * @since 2026/7/24
 */
public interface MailSendRecordService extends IService<MailSendRecord> {

    /**
     * 创建邮件记录并同步发送（任务表模式）
     *
     * 流程：插入记录(status=待发送) → 发送邮件 → 更新状态(成功/失败)
     * 即使发送失败，记录也已保存，不会丢失。
     *
     * @param businessType 业务类型
     * @param businessId   业务ID
     * @param receiver     收件邮箱
     * @param receiverName 收件人姓名
     * @param subject      邮件标题
     * @param content      邮件正文HTML
     * @return 邮件发送记录（包含最终状态、错误信息、Message-ID等）
     */
    MailSendRecord createAndSend(MailBusinessType businessType, Long businessId,
                                  String receiver, String receiverName,
                                  String subject, String content);

    /**
     * 重试发送失败的邮件
     *
     * 流程：检查状态(必须为失败) → 更新为重试中 → 发送 → 更新状态
     * 自动递增retry_count，超过5次不再允许重试。
     *
     * @param id 邮件记录ID
     * @return 重试后的邮件发送记录
     */
    MailSendRecord retry(Long id);

    /**
     * 取消待发送的邮件
     *
     * 仅允许取消status=待发送的记录。
     *
     * @param id 邮件记录ID
     */
    void cancel(Long id);

    /**
     * 获取邮件发送记录分页列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    Page<MailSendRecordPageVO> getMailSendRecordPage(MailSendRecordQuery query);

    /**
     * 根据业务类型和业务ID查询邮件记录列表
     *
     * 例如：查询投稿ID=321的所有邮件记录
     *
     * @param businessType 业务类型
     * @param businessId   业务ID
     * @return 邮件记录列表
     */
    java.util.List<MailSendRecord> listByBusiness(MailBusinessType businessType, Long businessId);

    /** 最大重试次数限制 */
    int MAX_RETRY_LIMIT = 5;

    /**
     * 处理退信：将邮件状态更新为"退信-投递失败"，并触发告警和自动重发
     *
     * 流程：
     * 1. 根据 Message-ID 查找对应的 mail_send_record 记录
     * 2. 将状态更新为 BOUNCE_DETECTED，记录退信原因
     * 3. 发送管理员告警邮件
     * 4. 如果启用自动重发且重发次数未超限，自动重发邮件
     *
     * @param messageId  SMTP Message-ID（退信邮件中引用的原始邮件ID）
     * @param bounceReason 退信原因（从退信邮件中提取）
     * @param bouncedRecipient 退信的收件人地址
     * @return true=成功处理退信，false=未找到对应记录
     */
    boolean handleBounce(String messageId, String bounceReason, String bouncedRecipient);

    /**
     * 根据 Message-ID 查找邮件发送记录
     *
     * @param messageId SMTP Message-ID
     * @return 对应的邮件发送记录，未找到返回 null
     */
    MailSendRecord findByMessageId(String messageId);

    /**
     * 手动标记邮件为退信-投递失败
     *
     * 管理员在邮件中心手动标记退信，用于IMAP自动检测遗漏的退信场景。
     * 标记后会记录退信原因（加[手动标记]前缀），并发送管理员告警邮件。
     *
     * @param id 邮件记录ID
     * @param bounceReason 退信原因
     * @return 更新后的邮件发送记录
     */
    MailSendRecord markBounce(Long id, String bounceReason);

    /**
     * 处理退信（增强版）：解析退信信息、写入mail_bounce_record、更新delivery_status
     *
     * 完整流程：
     * 1. 幂等检查（bounceMessageId唯一索引）
     * 2. 5级匹配策略查找对应的mail_send_record
     * 3. 写入mail_bounce_record退信记录
     * 4. 更新mail_send_record的delivery_status=BOUNCED
     * 5. 永久性退信加入黑名单
     * 6. 发送管理员告警
     * 7. 临时性退信自动重发（创建新记录+parentRecordId追踪）
     *
     * @param messageId       退信邮件的Message-ID
     * @param originalMessageId 原始发送邮件的Message-ID（从In-Reply-To/References提取）
     * @param bounceReason    退信原因
     * @param bouncedRecipient 退信收件人
     * @param bounceType      退信类型
     * @param bounceCode      退信代码
     * @param dsnStatus       DSN状态码
     * @param dsnDiagnosticCode DSN诊断码
     * @param matchStrategy   匹配策略
     * @param bounceSubject   退信邮件主题
     * @param bounceSender    退信邮件发送者
     * @return true=成功处理退信，false=未找到对应记录或已处理
     */
    boolean handleBounceEnhanced(String messageId, String originalMessageId,
                                  String bounceReason, String bouncedRecipient,
                                  String bounceType, String bounceCode,
                                  String dsnStatus, String dsnDiagnosticCode,
                                  String matchStrategy,
                                  String bounceSubject, String bounceSender);

    /**
     * 根据收件人+时间窗口查找邮件发送记录（RECEIVER_TIME_WINDOW匹配策略）
     *
     * 当无法通过Message-ID匹配退信时，按收件人地址+发送时间窗口匹配。
     * 查找条件：receiver=bouncedRecipient AND send_status=1 AND send_time在7天内
     *
     * @param receiver 收件人地址
     * @param days     时间窗口天数
     * @return 匹配的邮件记录列表，按发送时间倒序
     */
    java.util.List<MailSendRecord> findByReceiverAndTimeWindow(String receiver, int days);

    /**
     * 更新邮件的投递状态
     *
     * @param id             邮件记录ID
     * @param deliveryStatus 投递状态
     * @param bounceCode     退信代码（投递失败时）
     * @param bounceReason   退信原因（投递失败时）
     * @param bounceMessageId 退信邮件Message-ID（幂等去重）
     */
    void updateDeliveryStatus(Long id, com.youlai.boot.support.mail.enums.DeliveryStatus deliveryStatus,
                               String bounceCode, String bounceReason, String bounceMessageId);
}