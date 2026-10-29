# MCLC 数据库Schema文档

> 数据库: `meeting_admin` | MySQL 8.0.45 | 字符集: utf8mb4 | 表数量: 45

## 目录

- [1. 邮件子系统 (4表)](#1-邮件子系统)
- [2. 会议日程子系统 (5表)](#2-会议日程子系统)
- [3. 论文投稿子系统 (2表)](#3-论文投稿子系统)
- [4. 支付子系统 (1表)](#4-支付子系统)
- [5. 门户CMS (10表)](#5-门户cms)
- [6. 注册管理 (2表)](#6-注册管理)
- [7. 问卷调查 (1表)](#7-问卷调查)
- [8. 团购折扣 (1表)](#8-团购折扣)
- [9. 签到管理 (1表)](#9-签到管理)
- [10. 系统管理 (16表)](#10-系统管理)
- [11. 代码生成 (2表)](#11-代码生成)

---

## 1. 邮件子系统

### 1.1 mail_send_record (邮件发送记录)

**核心邮件追踪表，记录所有邮件的发送、投递、退信状态和重试链**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| message_id | varchar(255) | 邮件唯一Message-ID（用于退信匹配） |
| business_type | varchar(50) | 业务类型（见MailBusinessType枚举） |
| business_id | varchar(100) | 业务关联ID |
| recipient | varchar(255) | 收件人邮箱 |
| recipient_name | varchar(100) | 收件人姓名 |
| sender | varchar(255) | 发件人邮箱 |
| subject | varchar(500) | 邮件主题 |
| content | longtext | 邮件内容 |
| cc | varchar(500) | 抄送 |
| bcc | varchar(500) | 密送 |
| send_status | tinyint | **发送状态（状态机核心）**: 0-待发送, 1-已提交, 2-发送失败, 3-重试中, 4-已取消, 5-已退信, 6-等待重试, 7-重试成功, 8-重试失败 |
| delivery_status | tinyint | 投递状态: 0-未知, 1-已投递, 2-延迟, 3-失败 |
| record_type | varchar(20) | 记录类型: ORIGINAL-原始, RETRY-重试 |
| parent_record_id | bigint | 父记录ID（重试链：指向原始记录） |
| root_record_id | bigint | 根记录ID（重试链：指向链头） |
| retry_sequence | int | 重试序号（第几次重试） |
| retry_count | int | 已重试次数 |
| max_retry_count | int | 最大重试次数 |
| consume_time | bigint | 消耗时长(ms) |
| error_message | text | 错误信息 |
| bounce_message_id | varchar(255) | 退信Message-ID |
| send_time | datetime | 发送时间 |
| delivery_time | datetime | 投递时间 |
| bounce_time | datetime | 退信时间 |
| template_id | bigint | 关联邮件模板ID |
| template_params | json | 模板参数 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| is_deleted | tinyint | 逻辑删除 |

**索引**: uk_root_retry(root_record_id, retry_sequence), idx_business(business_type, business_id), idx_message_id(message_id), idx_bounce_message_id(bounce_message_id), idx_recipient(recipient), idx_send_status(send_status), idx_create_time(create_time)

**状态机流转**:
```
待发送(0) → 已提交(1) → [成功: 结束]
                     → [失败: 发送失败(2)]
                     → [退信: 已退信(5)]
发送失败(2) → 等待重试(6) → 重试中(3) → 重试成功(7) / 重试失败(8)
已退信(5) → 等待重试(6) → 重试中(3) → 重试成功(7) / 重试失败(8)
```

### 1.2 mail_bounce_record (退信记录)

**IMAP退信解析结果，记录每封退信邮件的详细DSN信息和匹配策略**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| bounce_message_id | varchar(255) UK | 退信邮件Message-ID |
| original_message_id | varchar(255) | 原始邮件Message-ID |
| original_recipient | varchar(255) | 原始收件人 |
| bounced_recipient | varchar(255) | 退信收件人 |
| bounce_type | varchar(50) | 退信类型: DNS_ERROR/USER_NOT_FOUND/MAILBOX_UNAVAILABLE/SPAM_REJECTED/MAILBOX_FULL/RELAY_DENIED/OTHER |
| bounce_reason | text | 退信原因 |
| matched_record_id | bigint | 匹配到的发送记录ID |
| match_strategy | varchar(50) | 匹配策略: MESSAGE_ID/IN_REPLY_TO/REFERENCES/RECEIVER_TIME_WINDOW/UNMATCHED |
| match_confidence | int | 匹配置信度(0-100) |
| processed | tinyint | 是否已处理: 0-未处理, 1-已处理 |
| dsn_status | varchar(100) | DSN状态码 |
| dsn_diagnostic_code | varchar(500) | DSN诊断码 |
| dsn_action | varchar(50) | DSN动作: failed/delayed/delivered/relayed/expanded |
| final_recipient | varchar(255) | 最终收件人 |
| remote_mta | varchar(255) | 远程MTA |
| diagnostic_code | text | 诊断码详情 |
| raw_content | longtext | 原始退信内容 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

**索引**: uk_bounce_message_id, idx_original_message_id, idx_matched_record_id, idx_bounce_type, idx_bounced_recipient, idx_processed, idx_create_time, idx_original_recipient, idx_bounce_recipient

### 1.3 mail_bounce_blacklist (退信黑名单)

**自动/手动管理的邮箱黑名单，阻止向已知无效地址发送邮件**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| email | varchar(255) UK | 黑名单邮箱 |
| source | varchar(20) | 来源: AUTO-自动添加, MANUAL-手动添加 |
| bounce_count | int | 累计退信次数 |
| last_bounce_type | varchar(50) | 最近退信类型 |
| last_bounce_time | datetime | 最近退信时间 |
| reason | varchar(500) | 加入原因 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| is_deleted | tinyint | 逻辑删除 |

### 1.4 mail_imap_checkpoint (IMAP检查点)

**IMAP增量扫描状态，记录每次扫描的位置以避免重复处理**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| folder_name | varchar(100) | IMAP文件夹名 |
| username | varchar(100) | 邮箱用户名 |
| uid_validity | bigint | UID有效性标识 |
| last_uid | bigint | 最后处理的UID |
| last_check_count | int | 上次检查邮件数 |
| last_bounce_count | int | 上次退信数 |
| last_check_time | datetime | 上次检查时间 |
| execution_status | varchar(20) | 执行状态 |
| error_message | text | 错误信息 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

**唯一索引**: uk_folder_username(folder_name, username)

---

## 2. 会议日程子系统

> 采用**列级国际化**策略，中英文字段在同一记录中（`_en`后缀）

### 2.1 meeting_info (会议天数)

**会议的每一天作为一个记录，是日程层级的第一级**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| name | varchar(255) | 天名称（中文） |
| name_en | varchar(255) | 天名称（英文） |
| meeting_date | date | 会议日期 |
| venue | varchar(255) | 场馆（中文） |
| venue_en | varchar(255) | 场馆（英文） |
| status | tinyint | 状态 |
| sort_order | int | 排序 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| is_deleted | tinyint | 逻辑删除 |

### 2.2 meeting_session (分会场/时段)

**日程层级第二级，归属 meeting_info，包含开场/主题报告/讨论/休息等类型**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| meeting_id | bigint FK | 关联meeting_info.id，CASCADE删除 |
| title | varchar(255) | 标题（中文） |
| title_en | varchar(255) | 标题（英文） |
| chairperson | varchar(255) | 主持人（中文） |
| chairperson_en | varchar(255) | 主持人（英文） |
| panelists | text | 嘉宾（中文） |
| panelists_en | text | 嘉宾（英文） |
| item_type | varchar(50) | 类型枚举: OPENING/KEYNOTE/INVITED_SPEECH/DISCUSSION/BREAK/LUNCH/CLOSING/OTHER |
| start_time | datetime | 开始时间 |
| end_time | datetime | 结束时间 |
| sort_order | int | 排序 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| is_deleted | tinyint | 逻辑删除 |

### 2.3 meeting_agenda_item (议程项)

**日程层级第三级，归属 meeting_session，包含具体演讲/报告**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| session_id | bigint FK | 关联meeting_session.id，CASCADE删除 |
| type | varchar(255) | 类型（中文） |
| type_en | varchar(255) | 类型（英文） |
| topic | varchar(500) | 主题（中文） |
| topic_en | varchar(500) | 主题（英文） |
| item_type | varchar(50) | 类型枚举 |
| speaker_id | bigint FK | 关联meeting_speaker.id，SET NULL删除 |
| speaker_name | varchar(100) | 演讲者姓名（冗余） |
| speaker_name_en | varchar(100) | 演讲者姓名英文（冗余） |
| speaker_org | varchar(255) | 演讲者机构（冗余） |
| speaker_org_en | varchar(255) | 演讲者机构英文（冗余） |
| is_break | tinyint | 是否为休息项 |
| start_time | datetime | 开始时间 |
| end_time | datetime | 结束时间 |
| sort_order | int | 排序 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| is_deleted | tinyint | 逻辑删除 |

### 2.4 meeting_speaker (演讲者目录)

**演讲者信息库，被 agenda_item 和 session_participant 引用**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| name | varchar(100) | 姓名（中文） |
| name_en | varchar(100) | 姓名（英文） |
| avatar_url | varchar(500) | 头像URL |
| title | varchar(100) | 职称（中文） |
| title_en | varchar(100) | 职称（英文） |
| affiliation | varchar(255) | 机构（中文） |
| affiliation_en | varchar(255) | 机构（英文） |
| brief_intro | text | 简介（中文） |
| brief_intro_en | text | 简介（英文） |
| detail_intro | longtext | 详细介绍（中文） |
| detail_intro_en | longtext | 详细介绍（英文） |
| research_fields | varchar(500) | 研究领域（中文） |
| research_fields_en | varchar(500) | 研究领域（英文） |
| sort_order | int | 排序 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| is_deleted | tinyint | 逻辑删除 |

### 2.5 meeting_session_participant (讨论嘉宾)

**会议讨论环节的参与者，关联 session 和 speaker**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| session_id | bigint FK | 关联meeting_session.id，CASCADE删除 |
| speaker_id | bigint FK | 关联meeting_speaker.id，CASCADE删除 |
| name | varchar(100) | 姓名（冗余） |
| name_en | varchar(100) | 姓名英文（冗余） |
| role | varchar(20) | 角色: MODERATOR/PANELIST/OTHER |
| sort_order | int | 排序 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| is_deleted | tinyint | 逻辑删除 |

**层级关系**:
```
meeting_info (Day)
  └── meeting_session (Session/Block) [FK: meeting_id → meeting_info.id, CASCADE]
        └── meeting_agenda_item (Item) [FK: session_id → meeting_session.id, CASCADE]
              └── meeting_speaker [FK: speaker_id → meeting_speaker.id, SET NULL]
        └── meeting_session_participant [FK: session_id, speaker_id, CASCADE]
```

---

## 3. 论文投稿子系统

### 3.1 paper_submission (论文投稿) 

**论文投稿主表，关联邮件发送和折扣码生成**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| paper_title | varchar(500) | 论文标题 |
| first_author | varchar(100) | 第一作者 |
| contact_number | varchar(50) | 联系电话 |
| contact_email | varchar(255) | 联系邮箱 |
| abstract | longtext | 摘要 |
| keywords | varchar(500) | 关键词 |
| appendix | varchar(500) | 附件路径 |
| paper_status | tinyint | 状态: 1-待审核, 2-通过, 3-拒绝 |
| reject_reason | varchar(1000) | 拒绝原因 |
| expert_emails | text | 专家邮箱列表 |
| expert_url | text | 专家审批链接 |
| email_status | tinyint | 邮件状态: 0-未发送, 1-已发送, 2-发送失败 |
| discount_code | varchar(100) | 折扣码（审核通过后生成） |
| deduction_status | tinyint | 抵扣状态: 0-未使用, 1-已使用 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| is_deleted | tinyint | 逻辑删除 |

### 3.2 paper_submission_email (论文评审邮件)

**发送给专家的评审邀请邮件记录**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| submission_id | bigint FK | 关联paper_submission.id |
| review_result | tinyint | 评审结果: 0-待评审, 1-通过, 2-拒绝 |
| send_status | tinyint | 发送状态: 1-已发送, 2-发送失败 |
| expert_name | varchar(100) | 专家姓名 |
| expert_email | varchar(255) | 专家邮箱 |
| url | varchar(500) | 评审链接 |
| send_error_msg | text | 发送错误信息 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

**工作流**:
```
投稿 → 发送专家邀请邮件 → 专家审批(通过/拒绝) → 更新paper_status → 生成discount_code
```

---

## 4. 支付子系统

### 4.1 payment_orders (支付订单)

**统一支付订单表，支持4种支付方式**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| registration_id | bigint | 关联注册记录ID |
| payment_method | varchar(20) | 支付方式: credit_card(Stripe)/wechat/alipay/bank_transfer |
| order_no | varchar(100) | 订单号 |
| transaction_id | varchar(255) | 第三方交易ID |
| amount | decimal(10,2) | 金额 |
| currency | varchar(10) | 币种，默认CNY |
| status | varchar(20) | 状态: pending/paid/refunded/closed |
| qr_code_url | varchar(500) | 二维码URL（微信/支付宝） |
| pay_url | varchar(500) | H5支付URL |
| extra_data | json | 扩展数据 |
| expire_time | datetime | 过期时间 |
| paid_time | datetime | 支付时间 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| is_deleted | tinyint | 逻辑删除 |

---

## 5. 门户CMS

> 采用**行级国际化**策略，通过 `country_type` 字段区分中英文（ZH/EN）

### 5.1 portal_meeting_setting (会议设置)

**全局会议配置，控制注册/日程/论文投稿等功能的开关和时间窗口**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| title | varchar(255) | 会议标题 |
| start_time | datetime | 会议开始时间 |
| end_time | datetime | 会议结束时间 |
| registration_deadline | datetime | 注册截止时间 |
| registration_enabled | tinyint | 是否开启注册 |
| schedule_enabled | tinyint | 是否开启日程 |
| general_reg_start | datetime | 常规注册开始时间 |
| general_reg_end | datetime | 常规注册结束时间 |
| early_bird_reg_start | datetime | 早鸟注册开始时间 |
| early_bird_reg_end | datetime | 早鸟注册结束时间 |
| student_reg_start | datetime | 学生注册开始时间 |
| student_reg_end | datetime | 学生注册结束时间 |
| paper_submission_deadline | datetime | 论文投稿截止时间 |
| check_time | datetime | 签到时间 |
| conference_date_start | datetime | 会议日期开始 |
| conference_date_end | datetime | 会议日期结束 |
| title_image | varchar(500) | 标题图片 |
| hqap_image | varchar(500) | HQAP图片 |
| schedule_title | varchar(255) | 日程标题 |
| schedule_description | text | 日程描述 |
| show_schedule | tinyint | 是否显示日程 |
| sponsor | text | 主办方 |
| organizer | text | 承办方 |
| country_type | varchar(10) | 语言类型: ZH/EN |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| is_deleted | tinyint | 逻辑删除 |

### 5.2 portal_guest (特邀嘉宾)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| name | varchar(100) | 姓名 |
| image | varchar(500) | 头像 |
| position | varchar(255) | 职位 |
| type | tinyint | 类型 |
| country_type | varchar(10) | 语言: ZH/EN |
| create_time/update_time/is_deleted | - | 审计字段 |

### 5.3 portal_hotel (酒店信息)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| hotel_name | varchar(255) | 酒店名称 |
| phone | varchar(50) | 电话 |
| contact | varchar(100) | 联系人 |
| image | varchar(500) | 图片 |
| address | varchar(500) | 地址 |
| hotel_desc | text | 描述 |
| url | varchar(500) | 链接 |
| country_type | varchar(10) | 语言: ZH/EN |

### 5.4 portal_question (FAQ常见问题)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| title | varchar(255) | 问题标题 |
| question_detail | text | 问题详情 |
| remark | varchar(255) | 备注 |
| country_type | varchar(10) | 语言: ZH/EN |

### 5.5 portal_contact (联系方式)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| title | varchar(255) | 标题 |
| contact_name | varchar(100) | 联系人 |
| contact_phone | varchar(50) | 电话 |
| contact_mail | varchar(100) | 邮箱 |
| country_type | varchar(10) | 语言: ZH/EN |

### 5.6 portal_meeting_date / portal_meeting_period (旧版日程)

> 已被 meeting_info/session/agenda_item 替代，保留兼容

**portal_meeting_date**: meet_name, meet_time, meet_status, time_period_id, meet_address, country_type

**portal_meeting_period**: time_period_name, person, time, type, question, teacher, unit

### 5.7 portal_moments (会议瞬间)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| name | varchar(100) | 名称 |
| moments_image | varchar(500) | 图片 |
| type | tinyint | 类型: 1-瞬间, 2-领导 |
| position | int | 排序 |
| country_type | varchar(10) | 语言: ZH/EN |

### 5.8 portal_speech (演讲精选)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| title | varchar(255) | 标题 |
| name | varchar(100) | 演讲者 |
| motto | varchar(500) | 格言 |
| images | varchar(500) | 图片 |
| introduction | text | 简介 |
| content | longtext | 内容 |
| video | varchar(500) | 视频URL |
| reference | varchar(500) | 参考资料 |
| position | int | 排序 |
| cover | varchar(500) | 封面 |
| country_type | varchar(10) | 语言: ZH/EN |

**索引**: idx_country_isdel_sort(country_type, is_deleted, position)

### 5.9 portal_architecture (组织架构)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| image_header | varchar(500) | 头部图片 |
| name | varchar(100) | 姓名 |
| job | varchar(100) | 职位 |
| sponsor | tinyint | 是否主办方 |
| organizer | tinyint | 是否承办方 |
| country_type | varchar(10) | 语言: ZH/EN |

### 5.10 portal_arrange (会前安排)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| arrange_content | text | 安排内容 |
| arrange_room | varchar(255) | 房间 |
| country_type | varchar(10) | 语言: ZH/EN |

---

## 6. 注册管理

### 6.1 register_user_information (注册用户信息)

**核心注册表，包含个人信息、账单、签证、支付、审核等完整字段**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| connect_id | varchar(255) | 关联ID（第三方登录） |
| name | varchar(255) | 名 |
| surname | varchar(255) | 姓 |
| email | varchar(255) | 邮箱 |
| organization | varchar(255) | 机构 |
| mobile_phone | varchar(255) | 手机号 |
| street | varchar(255) | 街道 |
| postal_code | varchar(255) | 邮编 |
| status | varchar(255) | 状态 |
| country | varchar(255) | 国家 |
| bill_organization | varchar(255) | 账单组织 |
| bill_vat | varchar(255) | 账单增值税号 |
| bill_procurement | varchar(255) | 账单采购订单号 |
| bill_street | varchar(255) | 账单街道 |
| bill_postal_code | varchar(255) | 账单邮编 |
| bill_city | varchar(255) | 账单城市 |
| bill_status | varchar(255) | 账单状态 |
| bill_country | varchar(255) | 账单国家 |
| registration_type | varchar(255) | 注册类型 |
| registration_fee | varchar(255) | 注册费 |
| registration_date | datetime | 注册日期 |
| edit_time | datetime | 修改日期 |
| payment_method | varchar(255) | 付款方式: 1-信用卡, 2-银行转账, 3-微信, 4-支付宝 |
| visa_name | varchar(255) | 签证名 |
| visa_surname | varchar(255) | 签证姓 |
| visa_passport | varchar(255) | 签证护照号 |
| visa_issue_time | datetime | 签证签发日 |
| visa_end_time | datetime | 签证结束日 |
| visa_time | datetime | 签证出生日期 |
| visa_area | varchar(255) | 签证出生地 |
| payment_status | varchar(20) | 支付状态: pending/paid/refunded/failed |
| stripe_payment_intent_id | varchar(100) | Stripe支付意图ID |
| payment_amount | varchar(20) | 实际支付金额 |
| visa_is_need | varchar(255) | 是否需要签证: 0-不需要, 1-需要 |
| deduction_code | varchar(255) | 抵扣码 |
| review_status | varchar(255) | 审核状态: 0-未审核, 1-通过, 2-拒绝 |
| reject_reason | varchar(1000) | 拒绝原因 |
| pic_bank | varchar(255) | 银行转账凭证图片 |
| actual_payment | varchar(255) | 实际付款金额 |
| register_label | varchar(255) | 注册类型label |

### 6.2 registration_type (注册类型)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| field_name | varchar(255) | 字段名称 |
| normal_valid_date | datetime | 常规有效期 |
| deadline_date | datetime | 截止日期 |
| normal_amount | varchar(255) | 常规金额 |
| deadline_amount | varchar(255) | 截止日期金额 |
| sort | int | 排序 |
| country_type | varchar(255) | 语言: ZH/EN |

---

## 7. 问卷调查

### 7.1 questionnaire_survey (问卷调查)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| registration_category | varchar(255) | 注册类别 |
| document | varchar(255) | 证件类型 |
| which_member | varchar(255) | 会员类型 |
| code | varchar(255) | 会员码 |
| is_match_drug | varchar(255) | 是否药物匹配 |
| dietary | varchar(255) | 饮食要求 |
| special_requirements | varchar(255) | 特殊需求 |
| is_first | varchar(255) | 是否首次参会 |
| gender | varchar(255) | 性别 |
| occupation | varchar(255) | 职业 |
| experience | varchar(255) | 经验 |
| field_of_interest | varchar(255) | 兴趣领域 |
| create_time/update_time/is_deleted | - | 审计字段 |

---

## 8. 团购折扣

### 8.1 discount_configuration (折扣配置)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| min_people | int | 最少人数 |
| max_people | int | 最多人数 |
| discount_rate | decimal(5,2) | 折扣率 |
| create_time/update_time/is_deleted | - | 审计字段 |

---

## 9. 签到管理

### 9.1 user_check_in (用户签到)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| check_id | bigint | 注册表ID（关联register_user_information） |
| check_time | varchar(64) | 签到时间 |
| check_name | varchar(128) | 签到姓名 |
| check_number | varchar(32) | 签到手机号 |
| check_email | varchar(128) | 签到邮箱 |
| check_status | varchar(255) | 签到状态 |

**索引**: idx_check_id(check_id)

---

## 10. 系统管理

### 10.1 sys_user (系统用户)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| username | varchar(64) | 用户名 |
| nickname | varchar(64) | 昵称 |
| gender | tinyint | 性别: 1-男, 2-女, 0-保密 |
| password | varchar(100) | 密码（加密） |
| dept_id | int | 部门ID |
| avatar | varchar(255) | 头像 |
| mobile | varchar(20) | 手机号 |
| status | tinyint | 状态: 1-正常, 0-禁用 |
| email | varchar(128) | 邮箱 |
| register_type | tinyint | 注册类型: 0-个人, 1-团队 |
| create_time/create_by/update_time/update_by/is_deleted | - | 审计字段 |

### 10.2 sys_role (系统角色)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| name | varchar(64) UK | 角色名称 |
| code | varchar(32) UK | 角色编码 |
| sort | int | 排序 |
| status | tinyint | 状态: 1-正常, 0-停用 |
| data_scope | tinyint | 数据权限: 1-所有, 2-部门及子部门, 3-本部门, 4-本人, 5-自定义 |
| create_by/create_time/update_by/update_time/is_deleted | - | 审计字段 |

### 10.3 sys_menu (系统菜单)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| parent_id | bigint | 父菜单ID |
| tree_path | varchar(255) | 父节点ID路径 |
| name | varchar(64) | 菜单名称 |
| type | char(1) | 类型: C-目录, M-菜单, B-按钮 |
| route_name | varchar(255) | 路由名称 |
| route_path | varchar(128) | 路由路径 |
| component | varchar(128) | 组件路径（相对src/views/） |
| perm | varchar(128) | 按钮权限标识 |
| always_show | tinyint | 目录单子路由是否始终显示 |
| keep_alive | tinyint | 是否开启页面缓存 |
| visible | tinyint | 显示状态: 1-显示, 0-隐藏 |
| sort | int | 排序 |
| icon | varchar(64) | 菜单图标 |
| redirect | varchar(128) | 跳转路径 |
| params | json | 路由参数 |
| create_time/update_time | - | 审计字段 |

> 注意：sys_menu 没有 is_deleted 字段，删除为物理删除

### 10.4 sys_dept (部门)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| name | varchar(100) | 部门名称 |
| code | varchar(100) UK | 部门编号 |
| parent_id | bigint | 父节点ID |
| tree_path | varchar(255) | 父节点ID路径 |
| sort | smallint | 排序 |
| status | tinyint | 状态: 1-正常, 0-禁用 |
| create_by/create_time/update_by/update_time/is_deleted | - | 审计字段 |

### 10.5 sys_role_menu / sys_role_dept / sys_user_role (关联表)

**sys_role_menu**: role_id + menu_id (UK)
**sys_role_dept**: role_id + dept_id (UK)
**sys_user_role**: user_id + role_id (PK)

### 10.6 sys_dict / sys_dict_item (数据字典)

**sys_dict**: dict_code(UK), name, status, remark + 审计字段
**sys_dict_item**: dict_code(关联sys_dict), value, label, tag_type, status, sort, remark + 审计字段

### 10.7 sys_config (系统配置)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| config_name | varchar(50) | 配置名称 |
| config_key | varchar(50) | 配置key |
| config_value | varchar(100) | 配置值 |
| remark | varchar(255) | 备注 |
| create_time/create_by/update_time/update_by/is_deleted | - | 审计字段 |

### 10.8 sys_email_template (邮件模板)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| email_title | varchar(255) | 邮件标题 |
| email_content | longtext | 邮件内容（富文本编辑器JSON） |
| email_type | varchar(255) | 模板类型 |
| sort | int | 排序 |
| status | tinyint | 状态: 1-启用, 0-禁用 |
| remark | varchar(255) | 备注 |
| create_time/update_time/is_deleted | - | 审计字段 |

### 10.9 sys_notice / sys_user_notice (通知公告)

**sys_notice**: title, content, type(字典notice_type), level(字典notice_level), target_type(1:全体/2:指定), target_user_ids, publisher_id, publish_status(0:未发布/1:已发布/-1:已撤回), publish_time, revoke_time + 审计字段

**sys_user_notice**: notice_id, user_id, is_read(0:未读/1:已读), read_time + 审计字段

### 10.10 sys_log (操作日志)

> 使用MyISAM引擎，非InnoDB

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| module | varchar(50) | 日志模块 |
| request_method | varchar(64) | 请求方式 |
| request_params | text | 请求参数 |
| response_content | mediumtext | 返回参数 |
| content | varchar(255) | 日志内容 |
| request_uri | varchar(255) | 请求路径 |
| method | varchar(255) | 方法名 |
| ip | varchar(45) | IP地址 |
| province/city | varchar(100) | 省份/城市 |
| execution_time | bigint | 执行时间(ms) |
| browser/browser_version/os | varchar(100) | 浏览器/系统信息 |
| create_by/create_time | - | 审计字段 |

**索引**: idx_create_time

### 10.11 sys_user_social (第三方账号绑定)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| user_id | bigint | 用户ID |
| platform | varchar(20) | 平台: WECHAT_MINI/WECHAT_MP/ALIPAY/QQ/APPLE |
| openid | varchar(64) | 平台openid |
| unionid | varchar(64) | 微信unionid |
| nickname | varchar(64) | 第三方昵称 |
| avatar | varchar(255) | 第三方头像 |
| session_key | varchar(128) | 微信session_key |
| verified | tinyint | 是否已验证 |
| create_time/update_time | - | 审计字段 |

**索引**: uk_platform_openid(platform, openid), idx_user_id, idx_unionid

---

## 11. 代码生成

### 11.1 gen_table / gen_table_column

**gen_table**: 表名、包名、模块名、业务名、类名、路径等代码生成配置
**gen_table_column**: 列名、类型、Java类型、字段名、注释、表单类型、查询类型等列配置

---

## ER关系总结

```
┌─────────────────────────────────────────────────────────────────┐
│                     邮件子系统                                    │
│  mail_send_record ←──(parent/root_record_id)──→ mail_send_record│
│       ↓ (matched_record_id)                                     │
│  mail_bounce_record ──→ mail_bounce_blacklist                   │
│  mail_imap_checkpoint (独立，记录IMAP扫描位置)                    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                     会议日程                                      │
│  meeting_info ──→ meeting_session ──→ meeting_agenda_item        │
│                      ↓                ↓ (speaker_id, SET NULL)  │
│              meeting_session_participant  meeting_speaker        │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                     论文投稿                                      │
│  paper_submission ──→ paper_submission_email                    │
│       ↓ (discount_code)                                         │
│  register_user_information (deduction_code)                     │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                     支付系统                                      │
│  payment_orders (registration_id → register_user_information)   │
│  策略模式: Stripe/Wechat/Alipay/BankTransfer                    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                     RBAC权限                                      │
│  sys_user ──→ sys_user_role ──→ sys_role                        │
│  sys_role ──→ sys_role_menu ──→ sys_menu                        │
│  sys_role ──→ sys_role_dept ──→ sys_dept                        │
│  sys_user ──→ sys_user_social (第三方登录)                       │
│  sys_user ──→ sys_user_notice ──→ sys_notice                    │
└─────────────────────────────────────────────────────────────────┘
```