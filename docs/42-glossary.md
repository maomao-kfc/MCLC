# MCLC 项目术语表

| 术语 | 英文/代码 | 解释 | 出现位置 |
|------|-----------|------|----------|
| 方案B | Plan B | 支付凭证上传+人工审核方案（因协会无法提供回调） | PaymentProofServiceImpl |
| 凭证 | proof / payment_proof | 用户上传的支付截图 | payment_proof 表 |
| 凭证状态 | proof_status | null→proof_submitted→paid/rejected/offline_paid | register_user_information 表 |
| OCR初筛 | OCR | Tess4j识别凭证截图中的金额/交易号 | TesseractOcrServiceImpl |
| PSM 11 | Page Segmentation Mode | Tesseract稀疏文本模式，适合多区域截图 | TesseractOcrServiceImpl |
| dHash | difference Hash | 感知哈希，检测图片视觉相似度 | PaymentProofServiceImpl |
| 对账 | reconciliation | 协会银行流水与注册记录匹配 | PaymentReconciliationServiceImpl |
| 匹配引擎 | findBestMatch | 姓名30+手机25+金额25+时间20评分模型 | PaymentReconciliationServiceImpl |
| 自动匹配阈值 | AUTO_MATCH_THRESHOLD | ≥60分自动匹配，<60人工确认 | PaymentReconciliationServiceImpl |
| 两封邮件机制 | - | 第一封待核对+第二封注册成功 | PaymentProofServiceImpl |
| 9态状态机 | MailSendStatus | PENDING/SUCCESS/FAILED/RETRYING/CANCELLED/BOUNCE_DETECTED/RETRY_PENDING/RETRY_SUCCESS/RETRY_FAILED | MailSendStatus.java |
| 任务表模式 | Outbox Pattern | 先落库再发送，记录不丢失 | MailSendRecordServiceImpl |
| 父子重试模型 | - | record_type=ORIGINAL/RETRY+retry_sequence+root_record_id | mail_send_record 表 |
| 退信 | bounce | 邮件投递失败被退回 | BounceEmailJob |
| DSN | Delivery Status Notification | 退信通知标准格式 | BounceParser |
| RFC 3463 | - | 增强邮件状态码标准（如5.1.1用户不存在） | BounceType.java |
| 永久退信 | permanent bounce | 收件人不存在/域名不存在等，加入黑名单 | BounceType(permanent=true) |
| 临时退信 | temporary bounce | 邮箱已满/频率限制等，可重试 | BounceType(permanent=false) |
| 黑名单 | blacklist | 永久退信地址，发送前拦截 | mail_bounce_blacklist 表 |
| IMAP游标 | checkpoint | 断点续传，记录最后处理的邮件UID | mail_imap_checkpoint 表 |
| SMTP 250 | - | 邮件服务器接受响应，≠最终投递成功 | MailServiceImpl |
| RBAC | Role-Based Access Control | 用户→角色→菜单→权限 | system 模块 |
| type=B | Button | youlai-boot只加载按钮类型菜单的perm | MenuServiceImpl |
| perm | permission | 权限标识如 portal:paymentProof:review | sys_menu 表 |
| 数据权限 | DataPermission | 按部门/本人范围过滤数据 | MyDataPermissionHandler |
| JWT | JSON Web Token | 无状态认证令牌 | AuthServiceImpl |
| SSE | Server-Sent Events | 服务器单向推送（字典同步/在线人数） | WebSocketConfig |
| i18n | internationalization | 中英双语国际化 | webpage/src/locales/ |
| 代码生成器 | codegen | 根据表结构生成前后端CRUD代码 | module/codegen |
| JODConverter | - | DOCX→PDF转换（依赖LibreOffice） | DocxToPdfServiceImpl |
| MinIO | - | 对象存储（文件上传） | MinioFileService |
| Redisson | - | Redis Java客户端（分布式锁/缓存） | RedisConfig |
| Caffeine | - | 本地缓存（高频读如字典） | CaffeineConfig |
| XXL-Job | - | 分布式定时任务调度 | XxlJobConfig |
| MapStruct | - | 编译期对象映射（Entity↔VO/Form） | portal/converter/ |
| Knife4j | - | OpenAPI文档增强 | OpenApiConfig |
| youlai-boot | - | 项目基于的开源脚手架框架 | 整个项目 |
| 三端架构 | - | 后端API+管理端(frontend)+门户端(webpage) | 项目结构 |
| 9步注册 | - | Signup→Contact→Survey→Skills→Enroll→Expert→Consent→Confirm→Final | webpage/src/pages/register/ |
| 我的注册 | MyRegistration | 用户查询注册状态的页面 | MyRegistration.vue |
| 注册待核对模板 | - | sys_email_template id=7，第一封邮件 | sys_email_template 表 |
| 注册通过模板 | - | sys_email_template id=6，第二封邮件 | sys_email_template 表 |