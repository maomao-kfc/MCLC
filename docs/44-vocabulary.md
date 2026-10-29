# MCLC 项目英语词汇表（中文近音标注 + 中译 + 项目示例）

> 收录项目中高频出现的英语单词和缩写，用中文近音字标注读音，照着汉字读即可。
> 遇到不认识的单词，在这里查读音和意思。

---

## 一、编程通用词汇

| 单词 | 近音读法 | 中译 | 项目示例 |
|------|----------|------|----------|
| annotation | 安诺泰申 | 注解 | @Service、@Override 都是注解 |
| aspect | 艾斯派克特 | 切面 | AOP 切面编程 |
| bean | 宾 | Bean对象 | Spring 管理的对象都叫 Bean |
| component | 康炮能特 | 组件 | @Component 通用组件注解 |
| configuration | 康菲格瑞申 | 配置 | application.yml 配置文件 |
| constant | 康斯坦特 | 常量 | 常量类中定义的固定值 |
| converter | 康沃特 | 转换器 | GuestConverter（Entity↔VO转换） |
| dependency | 迪盆等西 | 依赖 | pom.xml 中的 Maven 依赖 |
| entity | 恩提提 | 实体 | 数据库表对应的 Java 类 |
| enum | 依诺姆 | 枚举 | MailSendStatus 枚举类 |
| exception | 依克赛普申 | 异常 | BusinessException 业务异常 |
| filter | 费尔特 | 过滤器 | JWT 过滤器链 |
| handler | 汉德勒 | 处理器 | MyDataPermissionHandler |
| interceptor | 因特赛普特 | 拦截器 | 请求拦截器 |
| mapper | 麦珀 | 映射器 | MyBatis Mapper 接口 |
| model | 莫德尔 | 模型 | model 包下的实体/DTO/VO |
| module | 莫久 | 模块 | portal/system/support 模块 |
| property | 普罗珀提 | 属性 | @ConfigurationProperties |
| repository | 瑞帕泽托瑞 | 仓库 | 数据访问层的另一种叫法 |
| resolver | 瑞佐沃 | 解析器 | 参数解析器 |
| service | 瑟维斯 | 服务 | @Service 业务逻辑层 |
| template | 泰姆普雷特 | 模板 | Velocity 代码生成模板 |
| utility | 优提勒提 | 工具 | Hutool 工具库 |
| validator | 瓦利戴特 | 校验器 | 表单校验 |
| generic | 杰内瑞克 | 泛型 | `List<T>` 中的 T |
| inherit | 因海瑞特 | 继承 | 子类继承父类 |
| override | 欧沃瑞德 | 重写 | @Override 重写父类方法 |
| overload | 欧沃洛德 | 重载 | 同名方法不同参数 |
| polymorphism | 帕利莫菲森 | 多态 | 面向对象三大特性之一 |
| encapsulation | 恩卡普苏勒申 | 封装 | 面向对象三大特性之一 |
| instantiate | 因斯坦谢特 | 实例化 | new 一个对象就是实例化 |
| serialize | 西瑞赖兹 | 序列化 | 对象转 JSON 字符串 |
| deserialize | 迪西瑞赖兹 | 反序列化 | JSON 字符串转对象 |
| asynchronous | 诶辛克若纳斯 | 异步的 | @Async 异步方法 |
| synchronous | 辛克若纳斯 | 同步的 | 同步调用 |
| callback | 考贝克 | 回调 | 支付回调 notify |
| iterator | 依特瑞特 | 迭代器 | 遍历集合 |
| lambda | 拉姆达 | Lambda表达式 | `(x) -> x.getId()` |
| immutable | 依缪特布 | 不可变的 | String 是不可变对象 |
| mutable | 缪特布 | 可变的 | 可修改的对象 |
| nullable | 纳勒布 | 可为空的 | 字段允许 null |
| refactor | 瑞法克特 | 重构 | 改善代码结构但不改行为 |
| deprecated | 戴普瑞克泰德 | 已废弃的 | 不建议再使用的 API |
| singleton | 辛格尔顿 | 单例 | Spring Bean 默认单例 |
| proxy | 普罗克西 | 代理 | AOP 动态代理 |
| reflection | 瑞弗莱克申 | 反射 | BounceEmailJob 反射调用 id() |
| scope | 斯科普 | 作用域 | Bean 的作用域 |
| thread | 斯瑞德 | 线程 | 多线程并发 |
| deadlock | 戴德洛克 | 死锁 | 两个线程互相等待 |
| race condition | 瑞斯-康迪申 | 竞态条件 | 并发访问共享数据 |
| idempotent | 艾德姆波特恩特 | 幂等的 | 重复调用结果不变 |
| transaction | 翠恩扎克申 | 事务 | @Transactional |
| rollback | 罗尔贝克 | 回滚 | 事务失败后撤销 |
| commit | 克密特 | 提交 | 事务提交 |
| migration | 麦格雷申 | 迁移 | 数据库结构变更 |
| schema | 斯基玛 | 模式/结构 | 数据库表结构 |
| pagination | 佩杰内申 | 分页 | PageResult 分页结果 |
| cascade | 卡斯凯德 | 级联 | 级联删除 |
| aggregate | 艾格里盖特 | 聚合 | 聚合函数 SUM/COUNT |
| batch | 巴奇 | 批量 | 批量插入 |
| bulk | 巴尔克 | 大批量 | bulk insert |
| threshold | 斯瑞肖尔德 | 阈值 | 对账匹配阈值 60 分 |
| payload | 佩洛德 | 载荷/数据体 | 请求体中的数据 |
| stringify | 斯特林吉法伊 | 转字符串 | JSON.stringify() |
| parse | 帕斯 | 解析 | Integer.parseInt() |
| render | 伦德尔 | 渲染 | 前端渲染页面 |
| directive | 戴瑞克提夫 | 指令 | Vue 的 v-permission 指令 |
| composable | 康炮泽布 | 组合式函数 | Vue 3 useSse() |
| emitter | 依米特 | 发射器 | 事件发射 |
| listener | 利森纳 | 监听器 | 事件监听 |
| observable | 欧布泽沃布 | 可观察对象 | 响应式数据 |
| subscribe | 萨布斯克莱布 | 订阅 | 订阅事件/消息 |

---

## 二、邮件系统词汇

| 单词 | 近音读法 | 中译 | 项目示例 |
|------|----------|------|----------|
| bounce | 帮斯 | 退信/弹回 | BounceEmailJob 退信检测任务 |
| recipient | 瑞西皮恩特 | 收件人 | receiver 字段 |
| sender | 森德尔 | 发件人 | 发送方地址 |
| mailbox | 梅尔宝克斯 | 邮箱 | MAILBOX_FULL 邮箱已满 |
| relay | 瑞雷 | 中继/转发 | RELAY_DENIED 中继拒绝 |
| blacklist | 布莱克利斯特 | 黑名单 | mail_bounce_blacklist 表 |
| checkpoint | 切克泡英特 | 检查点/游标 | mail_imap_checkpoint 表 |
| retry | 瑞揣 | 重试 | RETRYING 重试中状态 |
| permanent | 珀玛能特 | 永久的 | 永久退信加黑名单 |
| temporary | 泰姆珀瑞 | 临时的 | 临时退信可重试 |
| diagnostic | 戴艾格诺斯提克 | 诊断的 | Diagnostic-Code DSN字段 |
| spam | 斯帕姆 | 垃圾邮件 | SPAM_REJECTED 拒收 |
| reject | 瑞杰克特 | 拒绝 | 550 拒收 |
| deliver | 迪利沃 | 投递/送达 | DELIVERED 已送达 |
| delivery | 迪利沃瑞 | 投递 | delivery_status 投递状态 |
| undelivered | 昂迪利沃德 | 未投递的 | Undelivered Mail 退信主题 |
| postmaster | 普斯特玛斯特 | 邮件管理员 | postmaster@ 发送者 |
| daemon | 迪门 | 守护进程 | MAILER-DAEMON 退信发送者 |
| multipart | 莫提帕特 | 多部分 | multipart/report DSN结构 |
| charset | 恰赛特 | 字符集 | UTF-8 字符集 |
| attachment | 依泰奇门特 | 附件 | 邮件附件 |
| inbox | 因宝克斯 | 收件箱 | IMAP INBOX 文件夹 |
| forward | 佛沃德 | 转发 | 转发邮件 |
| subject | 萨布杰克特 | 主题 | 邮件主题行 |
| body | 宝迪 | 正文 | 邮件正文 |
| header | 海德尔 | 邮件头 | Message-ID 邮件头 |
| verify | 维瑞法伊 | 验证 | 验证码邮件 |
| notification | 诺提菲凯申 | 通知 | 通知邮件 |
| encode | 恩科德 | 编码 | Base64 编码 |
| decode | 迪科德 | 解码 | Base64 解码 |
| protocol | 普罗托考 | 协议 | SMTP/IMAP 协议 |
| port | 泡特 | 端口 | SMTP 端口 465 |
| authentic | 奥森提克 | 真实的 | SMTP 认证 |
| cursor | 克尔瑟 | 游标 | IMAP UID 游标 |
| scan | 斯坎 | 扫描 | IMAP 扫描收件箱 |
| alert | 阿勒特 | 告警 | 退信告警通知 |
| queue | 克尤 | 队列 | 邮件发送队列 |
| schedule | 斯凯朱 | 调度/排程 | XXL-Job 定时调度 |
| trigger | 特里格 | 触发 | 触发邮件发送 |
| suppress | 萨普瑞斯 | 抑制/屏蔽 | 抑制重复发送 |
| reputation | 瑞普尤泰申 | 信誉 | 发件域名信誉 |
| throttle | 斯罗特 | 限流/节流 | 发送频率限制 |

---

## 三、支付与对账词汇

| 单词 | 近音读法 | 中译 | 项目示例 |
|------|----------|------|----------|
| reconciliation | 瑞康西利诶申 | 对账 | PaymentReconciliationServiceImpl |
| refund | 瑞芳德 | 退款 | refund() 退款方法 |
| payment | 佩门特 | 支付/付款 | payment_orders 表 |
| intent | 因泰恩特 | 意图/支付单 | Stripe PaymentIntent |
| webhook | 韦布胡克 | 网络钩子 | Stripe Webhook 回调 |
| callback | 考贝克 | 回调 | 支付回调 notify |
| credit card | 克瑞迪特-卡德 | 信用卡 | Stripe 信用卡支付 |
| bank transfer | 班克-翠恩斯弗 | 银行转账 | BankTransferProvider |
| invoice | 因沃伊斯 | 发票 | 账单信息 |
| receipt | 瑞西特 | 收据 | 支付凭证 |
| voucher | 瓦乌切尔 | 凭证 | PaymentProof 支付凭证 |
| proof | 普鲁夫 | 凭证/证明 | proof_status 凭证状态 |
| amount | 阿芒特 | 金额 | payment_amount 应付金额 |
| currency | 克伦西 | 货币 | CNY/USD |
| discount | 迪斯坎特 | 折扣 | discount_configuration 表 |
| coupon | 库庞 | 优惠券 | 抵扣码 |
| settle | 塞特 | 结算 | 结算流程 |
| settlement | 塞特尔门特 | 结算 | - |
| charge | 恰吉 | 扣款 | Stripe charge |
| authorize | 奥瑟瑞兹 | 授权 | 预授权扣款 |
| capture | 凯普彻 | 捕获/确认扣款 | 授权后确认扣款 |
| void | 沃依德 | 撤销 | 撤销交易 |
| partial | 帕歇 | 部分的 | 部分退款 |
| overdue | 欧沃杜 | 逾期的 | 逾期未付 |
| outstanding | 奥特斯丹丁 | 未结的 | 未结金额 |
| balance | 贝伦斯 | 余额 | 账户余额 |
| ledger | 勒杰 | 账本 | 银行流水账 |
| statement | 斯泰特门特 | 对账单 | 银行对账单 |
| remittance | 瑞米滕斯 | 汇款 | 银行汇款 |
| transaction | 翠恩扎克申 | 交易 | 交易号 |
| merchant | 玛臣特 | 商户 | 支付商户号 |
| gateway | 盖特韦 | 网关 | 支付网关 |
| signature | 西格纳彻 | 签名 | 支付回调验签 |
| verify | 维瑞法伊 | 验证 | 验签 |
| match | 麦奇 | 匹配 | 对账匹配 |
| manual | 麦纽 | 手动的 | 人工匹配 manual_matched |
| auto | 奥托 | 自动的 | 自动匹配 auto_matched |
| unmatched | 昂麦奇特 | 未匹配的 | unmatched 对账记录 |
| batch | 巴奇 | 批次 | 对账批次号 |
| import | 依姆泡特 | 导入 | Excel 导入 |
| export | 依克斯泡特 | 导出 | Excel 导出 |
| offline | 奥夫莱因 | 线下的 | 线下登记 offline_paid |

---

## 四、注册与会议业务词汇

| 单词 | 近音读法 | 中译 | 项目示例 |
|------|----------|------|----------|
| registration | 瑞吉斯特瑞申 | 注册 | RegisterUserServiceImpl |
| enroll | 恩罗尔 | 报名/登记 | Enroll 报名步骤 |
| consent | 肯森特 | 同意/知情同意 | Consent 知情同意步骤 |
| signup | 辛纳普 | 注册/签约 | Signup 注册步骤 |
| confirm | 肯弗姆 | 确认 | Confirm 确认步骤 |
| attendee | 阿坦迪 | 参会者 | 参会人员 |
| participant | 帕提西盆特 | 参与者 | session_participant 表 |
| speaker | 斯皮克尔 | 演讲者 | meeting_speaker 表 |
| guest | 盖斯特 | 嘉宾 | portal_guest 表 |
| agenda | 阿真达 | 议程 | meeting_agenda_item 表 |
| session | 塞申 | 分会场/会话 | meeting_session 分会场 |
| schedule | 斯凯朱 | 日程 | 会议日程页面 |
| venue | 维纽 | 场地 | 会议场地 |
| hotel | 霍泰尔 | 酒店 | portal_hotel 表 |
| visa | 维扎 | 签证 | 签证信息字段 |
| invitation | 因维泰申 | 邀请 | 邀请函 |
| questionnaire | 克斯提奥内尔 | 问卷 | questionnaire_survey 表 |
| survey | 瑟维 | 调查问卷 | 注册附加问卷 |
| check-in | 切克因 | 签到 | user_check_in 表 |
| submit | 萨布密特 | 提交 | 论文投稿提交 |
| submission | 萨布米申 | 投稿/提交 | paper_submission 表 |
| abstract | 艾布斯特瑞克特 | 摘要 | 论文摘要 |
| paper | 佩珀 | 论文 | 论文投稿模块 |
| review | 瑞维尤 | 审稿/审核 | 专家审稿 |
| approve | 阿普鲁夫 | 批准/通过 | 审核通过 |
| reject | 瑞杰克特 | 拒绝 | 审核拒绝 |
| discount code | 迪斯坎特-科德 | 抵扣码 | 论文通过后生成的抵扣码 |
| category | 凯特格里 | 类别 | registration_category 注册类别 |
| occupation | 奥库佩申 | 职业 | 问卷职业字段 |
| gender | 真德尔 | 性别 | 问卷性别字段 |
| nationality | 纳沙纳勒提 | 国籍 | 国籍字段 |
| department | 迪帕特门特 | 部门 | sys_dept 部门表 |
| organization | 奥格纳泽申 | 组织/机构 | 组织字段 |
| accommodation | 阿科莫戴申 | 住宿 | 住宿安排 |
| banquet | 班克维特 | 宴会 | 会议晚宴 |
| keynote | 基诺特 | 主旨演讲 | keynote speaker 主讲嘉宾 |
| poster | 泊斯特 | 海报展示 | poster session |
| deadline | 戴德莱因 | 截止日期 | 投稿截止 |
| reminder | 瑞麦因德尔 | 提醒 | 会议提醒邮件 |

---

## 五、安全与权限词汇

| 单词 | 近音读法 | 中译 | 项目示例 |
|------|----------|------|----------|
| authentication | 奥森提凯申 | 认证（你是谁） | 登录认证 |
| authorization | 奥瑟瑞泽申 | 授权（你能干什么） | 权限校验 |
| credential | 克瑞登歇 | 凭证 | 登录凭证 |
| token | 托肯 | 令牌 | JWT Token |
| session | 塞申 | 会话 | 用户会话 |
| cookie | 库基 | Cookie | 浏览器存储 |
| role | 罗尔 | 角色 | sys_role 角色表 |
| permission | 珀米申 | 权限 | perm 权限标识 |
| menu | 梅纽 | 菜单 | sys_menu 菜单表 |
| encrypt | 恩克里普特 | 加密 | 密码加密 |
| decrypt | 迪克里普特 | 解密 | 解密数据 |
| hash | 海什 | 哈希/散列 | BCrypt 哈希 |
| salt | 索尔特 | 盐值 | 加密盐值 |
| certificate | 瑟提菲凯特 | 证书 | SSL 证书 |
| injection | 因杰克申 | 注入 | SQL 注入 |
| exploit | 依克斯普罗依特 | 漏洞利用 | 安全漏洞 |
| vulnerability | 瓦尔纳拉比勒提 | 漏洞 | 安全漏洞 |
| brute force | 布鲁特-佛斯 | 暴力破解 | 暴力破解密码 |
| privilege | 普里维利吉 | 特权/权限 | 提权攻击 |
| payload | 佩洛德 | 攻击载荷 | XSS payload |

---

## 六、数据库与缓存词汇

| 单词 | 近音读法 | 中译 | 项目示例 |
|------|----------|------|----------|
| index | 因戴克斯 | 索引 | 数据库索引 |
| primary key | 普瑞梅里-基 | 主键 | id 主键 |
| foreign key | 佛伦-基 | 外键 | 关联表外键 |
| constraint | 肯斯特瑞恩特 | 约束 | uk_root_retry 唯一约束 |
| unique | 尤尼克 | 唯一的 | UNIQUE 索引 |
| isolation | 艾索勒申 | 隔离 | 事务隔离级别 |
| dirty read | 德提-瑞德 | 脏读 | 并发读问题 |
| phantom read | 范特姆-瑞德 | 幻读 | 并发读问题 |
| cache | 凯什 | 缓存 | Redis 缓存 |
| eviction | 依维克申 | 驱逐/淘汰 | 缓存淘汰策略 |
| expire | 依克斯帕伊尔 | 过期 | 缓存过期时间 |
| persistence | 珀西斯坦斯 | 持久化 | 数据持久化 |
| snapshot | 斯耐普肖特 | 快照 | Redis 快照 |
| pool | 普尔 | 连接池 | Druid 连接池 |
| query | 奎瑞 | 查询 | SQL 查询 |
| clause | 克洛兹 | 子句 | WHERE 子句 |
| join | 乔因 | 连接查询 | LEFT JOIN |
| union | 尤尼恩 | 合并查询 | UNION ALL |
| subquery | 萨布奎瑞 | 子查询 | 嵌套查询 |
| trigger | 特里格 | 触发器 | 数据库触发器 |
| procedure | 普罗西杰 | 存储过程 | SQL 存储过程 |
| view | 维尤 | 视图 | 数据库视图 |
| sequence | 西昆斯 | 序列 | 自增序列 |
| dialect | 戴厄莱克特 | 方言 | MyBatis 方言 |
| shard | 夏德 | 分片 | 分库分表 |
| replica | 瑞普利卡 | 副本 | 主从复制副本 |
| master | 玛斯特 | 主库 | 主从架构 |
| slave | 斯莱夫 | 从库 | 读写分离 |

---

## 七、部署与运维词汇

| 单词 | 近音读法 | 中译 | 项目示例 |
|------|----------|------|----------|
| deploy | 迪普罗依 | 部署 | docker-compose 部署 |
| container | 肯泰纳 | 容器 | Docker 容器 |
| image | 依米吉 | 镜像 | Docker 镜像 |
| volume | 瓦尤姆 | 数据卷 | Docker 挂载卷 |
| port mapping | 泡特-麦平 | 端口映射 | 8989→8000 |
| environment | 恩瓦伊伦门特 | 环境 | SPRING_PROFILES_ACTIVE |
| reverse proxy | 瑞沃斯-普罗克西 | 反向代理 | Nginx 反向代理 |
| load balance | 洛德-贝伦斯 | 负载均衡 | - |
| health check | 海尔斯-切克 | 健康检查 | 服务健康检查 |
| monitor | 玛尼特 | 监控 | Spring Boot Admin |
| downtime | 唐泰姆 | 停机时间 | 服务不可用时间 |
| scaling | 斯凯林 | 扩缩容 | 水平扩容 |
| backup | 贝克阿普 | 备份 | 数据库备份 |
| restore | 瑞斯托 | 恢复 | 数据恢复 |
| pipeline | 派普莱因 | 流水线 | CI/CD 流水线 |
| registry | 瑞吉斯特里 | 镜像仓库 | Docker Registry |
| compose | 康炮兹 | 编排 | docker-compose.yml |
| daemon | 迪门 | 守护进程 | dockerd 守护进程 |
| nginx | 恩金-依克斯 | Nginx服务器 | 反向代理服务器 |
| certbot | 瑟特宝特 | 证书工具 | HTTPS 证书申请 |
| cron | 克罗恩 | 定时任务表达式 | XXL-Job cron 表达式 |

---

## 八、常见缩写速查

| 缩写 | 全称 | 近音读法 | 中译 |
|------|------|----------|------|
| API | Application Programming Interface | 诶-皮-艾 | 应用编程接口 |
| CRUD | Create Read Update Delete | 克拉德 | 增删改查 |
| DTO | Data Transfer Object | 迪-提-欧 | 数据传输对象 |
| VO | View Object | 维-欧 | 视图对象 |
| PO | Persistent Object | 皮-欧 | 持久化对象 |
| ORM | Object-Relational Mapping | 奥-阿-艾姆 | 对象关系映射 |
| AOP | Aspect-Oriented Programming | 诶-欧-皮 | 面向切面编程 |
| IOC | Inversion of Control | 艾-欧-西 | 控制反转 |
| DI | Dependency Injection | 迪-艾 | 依赖注入 |
| JWT | JSON Web Token | 杰沃特 | JSON网络令牌 |
| RBAC | Role-Based Access Control | 阿-贝-诶-西 | 基于角色的访问控制 |
| SMTP | Simple Mail Transfer Protocol | 艾斯-艾姆-提-皮 | 简单邮件传输协议 |
| IMAP | Internet Message Access Protocol | 艾-迈普 | 互联网消息访问协议 |
| POP3 | Post Office Protocol 3 | 帕普-斯瑞 | 邮局协议第3版 |
| DSN | Delivery Status Notification | 迪-艾斯-恩 | 投递状态通知 |
| DNS | Domain Name System | 迪-恩-艾斯 | 域名系统 |
| SSL | Secure Sockets Layer | 艾斯-艾斯-艾尔 | 安全套接层 |
| TLS | Transport Layer Security | 提-艾尔-艾斯 | 传输层安全 |
| OCR | Optical Character Recognition | 欧-西-阿 | 光学字符识别 |
| PDF | Portable Document Format | 皮-迪-艾夫 | 便携式文档格式 |
| SDK | Software Development Kit | 艾斯-迪-凯 | 软件开发工具包 |
| SSE | Server-Sent Events | 艾斯-艾斯-依 | 服务器推送事件 |
| REST | Representational State Transfer | 瑞斯特 | 表述性状态转移 |
| JSON | JavaScript Object Notation | 杰森 | JS对象表示法 |
| XML | eXtensible Markup Language | 艾克斯-艾姆-艾尔 | 可扩展标记语言 |
| YAML | YAML Ain't Markup Language | 亚姆 | 配置文件格式 |
| SQL | Structured Query Language | 西奎尔 | 结构化查询语言 |
| DDL | Data Definition Language | 迪-迪-艾尔 | 数据定义语言 |
| DML | Data Manipulation Language | 迪-艾姆-艾尔 | 数据操作语言 |
| TTL | Time To Live | 提-提-艾尔 | 存活时间/过期时间 |
| LRU | Least Recently Used | 艾尔-阿-尤 | 最近最少使用 |
| FIFO | First In First Out | 菲佛 | 先进先出 |
| UI | User Interface | 尤-艾 | 用户界面 |
| CI/CD | Continuous Integration/Delivery | 西-艾-西-迪 | 持续集成/持续交付 |
| PSM | Page Segmentation Mode | 皮-艾斯-艾姆 | 页面分割模式（OCR） |
| RFC | Request for Comments | 阿-艾夫-西 | 互联网标准文档 |
| UID | Unique Identifier | 尤-艾-迪 | 唯一标识符 |
| UUID | Universally Unique Identifier | 尤-尤-艾-迪 | 通用唯一标识符 |
| MD5 | Message Digest 5 | 艾姆-迪-法伊夫 | 消息摘要算法 |
| IP | Internet Protocol | 艾-皮 | 网络协议/地址 |
| URL | Uniform Resource Locator | 尤-阿-艾尔 | 统一资源定位符 |
| HTTP | HyperText Transfer Protocol | 诶奇-提-提-皮 | 超文本传输协议 |
| HTTPS | HTTP Secure | 诶奇-提-提-皮-艾斯 | 安全HTTP |
| i18n | internationalization | 艾-阿-提恩-恩 | 国际化 |
| l10n | localization | 艾尔-腾 | 本地化 |
| k8s | Kubernetes | 凯-艾茨 | 容器编排平台 |
| CORS | Cross-Origin Resource Sharing | 考斯 | 跨域资源共享 |
| CSRF | Cross-Site Request Forgery | 西-依-阿斯-艾夫 | 跨站请求伪造 |
| XSS | Cross-Site Scripting | 依克斯-依斯-依斯 | 跨站脚本攻击 |

---

## 使用建议

1. **看代码遇到不认识的词**：先查本词汇表，按模块找（邮件→第二节，支付→第三节）
2. **看文档遇到缩写**：直接查第八节缩写速查表
3. **近音读法只是辅助**：中文近音无法100%还原英语发音，建议配合词典APP听真人发音，多听几遍就能记住
4. **最值得优先记住的10个词**（项目最高频且容易读错）：
   - reconciliation 瑞康西利诶申（对账）
   - bounce 帮斯（退信）
   - idempotent 艾德姆波特恩特（幂等）
   - authentication 奥森提凯申（认证）
   - authorization 奥瑟瑞泽申（授权）
   - interceptor 因特赛普特（拦截器）
   - repository 瑞帕泽托瑞（仓库）
   - threshold 斯瑞肖尔德（阈值）
   - diagnostic 戴艾格诺斯提克（诊断）
   - questionnaire 克斯提奥内尔（问卷）