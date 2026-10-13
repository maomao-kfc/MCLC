# 更新日志（CHANGELOG）

> 本文件记录 MCLC 学术会议管理系统的所有功能变更、修复和优化。
> 
> 格式规范：`[类型] 简要描述`，类型包括 Feature（新功能）、Fix（修复）、Enhancement（优化）、Refactor（重构）。

---

## 2026-10-13

### [Fix] 启动前清理残留 LibreOffice 进程，修复 JODConverter profile 目录删除失败

- **目的：**
  - 上次应用异常退出（IDEA 强制 Stop / taskkill /F）时，JODConverter 启动的 soffice 子进程未被正确终止，成为孤儿进程
  - 残留进程锁住 profile 目录中的 `extensions.pmap` 文件，导致下次启动时 JODConverter 清理旧 profile 目录失败
  - 报错：`RetryTimeoutException: FileSystemException: extensions.pmap: 另一个程序正在使用此文件`（3个端口 2002/2003/2004 各一次 ERROR）

- **涉及文件：**
  - `backend/src/main/java/com/youlai/boot/YouLaiBootApplication.java` — 修改（新增 `killResidualSofficeProcesses()` 方法）

- **修改逻辑：**
  - 在 `main()` 方法中 `SpringApplication.run()` 之前调用 `killResidualSofficeProcesses()`
  - 仅 Windows 环境执行（Linux Docker 环境每次全新启动，无此问题）
  - 通过端口 2002/2003/2004 精确定位 JODConverter 管理的 soffice 进程（`netstat -ano` 查 PID → `taskkill /F` 终止），避免误杀用户正在使用的 LibreOffice 桌面实例
  - 清理失败不影响启动，JODConverter 自身有 `connect_or_kill` 回退策略

- **影响范围：**
  - 后端：消除启动时的 3 个 ERROR 日志，LibreOffice 进程池正常初始化
  - 无功能影响：PDF 导出功能不受影响

- **验证方式：**
  - 先制造残留场景（启动后 taskkill /F 强杀 Java 进程，留下 6 个孤儿 soffice 进程）
  - `mvn clean spring-boot:run` 启动，确认 profile 目录删除成功（仅 WARN 无 ERROR），3 个 LibreOffice 端口全部 Connected

- **验证结果：** 成功（`Started YouLaiBootApplication in 15.596 seconds`，无 `Could not delete` ERROR）

---

### [Fix] 删除 hotel 重复包及残留 Mapper XML 修复 Bean 名冲突启动失败

- **目的：**
  - `com.youlai.boot.hotel.controller.HotelController` 与 `com.youlai.boot.portal.controller.HotelController` 产生 Spring Bean 名冲突（`ConflictingBeanDefinitionException: bean name 'hotelController'`），项目启动失败
  - hotel 包为代码生成器产生的残留代码（@author dearth），仅内部互相引用，无外部 import，无业务逻辑；真正的酒店管理实现在 portal 包

- **涉及文件：**
  - `backend/src/main/java/com/youlai/boot/hotel/controller/HotelController.java` — 删除（生成器残留）
  - `backend/src/main/java/com/youlai/boot/hotel/service/HotelService.java` — 删除（生成器残留）
  - `backend/src/main/java/com/youlai/boot/hotel/service/impl/HotelServiceImpl.java` — 删除（生成器残留）
  - `backend/src/main/java/com/youlai/boot/hotel/mapper/HotelMapper.java` — 删除（生成器残留）
  - `backend/src/main/java/com/youlai/boot/hotel/model/dto/HotelPageQuery.java` — 删除（生成器残留）
  - `backend/src/main/java/com/youlai/boot/hotel/model/entity/Hotel.java` — 删除（生成器残留）
  - `backend/src/main/resources/mapper/HotelMapper.xml` — 删除（引用已删除的 hotel 实体类，导致 MyBatis `ClassNotFoundException`）

- **修改逻辑：**
  - 确认 hotel 包6个 Java 文件无外部 import 引用
  - 确认真正的酒店业务在 `backend/src/main/java/com/youlai/boot/portal/controller/HotelController.java`（API 路径 `/api/v1/hotel`）
  - `git rm -r -f` 删除整个 hotel 包
  - 启动后发现 `HotelMapper.xml` 仍引用已删除实体，`git rm -f` 删除该 XML
  - `mvn clean spring-boot:run` 清理 target 残留 class 后重新启动

- **影响范围：**
  - 后端：消除 Bean 名冲突和 MyBatis 类解析异常，项目可正常启动
  - 无业务影响：删除的文件不含业务逻辑，无外部引用

- **验证方式：**
  - `mvn clean spring-boot:run` 启动成功（`Started YouLaiBootApplication in 10.699 seconds`，Tomcat 运行于 8000 端口）

- **验证结果：** 成功

---

### [Fix] 删除 meetingSchedule 包下4个空壳 Java 文件修复编译失败

- **目的：**
  - `backend/src/main/java/com/youlai/boot/meetingSchedule/controller/MeetingScheduleController.java` 类名小写（meetingScheduleController）与文件名大写（MeetingScheduleController）不匹配，导致 `java: 类 meetingScheduleController 是公共的, 应在名为 meetingScheduleController.java` 编译错误，项目启动失败
  - 该文件及同包下3个 VO 文件均为无任何业务代码的空壳文件，真正的会议日程 Controller 位于 `com.youlai.boot.portal.controller.MeetingScheduleController`

- **涉及文件：**
  - `backend/src/main/java/com/youlai/boot/meetingSchedule/controller/MeetingScheduleController.java` — 删除（空壳，类名小写不匹配）
  - `backend/src/main/java/com/youlai/boot/meetingSchedule/model/vo/SessionVO.java` — 删除（空壳）
  - `backend/src/main/java/com/youlai/boot/meetingSchedule/model/vo/MeetingScheduleVO.java` — 删除（空壳）
  - `backend/src/main/java/com/youlai/boot/meetingSchedule/model/vo/AgendaItemVO.java` — 删除（空壳）

- **修改逻辑：**
  - 确认4个文件内容均为空壳（仅 package 声明+空类体），无任何字段、方法、注解
  - 确认无其他代码引用 `com.youlai.boot.meetingSchedule` 包
  - 确认真正的 Controller 在 `backend/src/main/java/com/youlai/boot/portal/controller/MeetingScheduleController.java`（API 路径 `/api/v1/meetingSchedule`）
  - `git rm -f` 删除4个空壳文件

- **影响范围：**
  - 后端：消除编译错误，项目可正常启动
  - 无业务影响：删除的文件不含任何业务代码，无外部引用

- **验证方式：**
  - `mvn compile -q` 编译通过（无错误输出）

- **验证结果：** 成功

---

### [Enhancement] 日程板块名称英文样式对齐主持人英文样式

- **目的：**
  - 会议日程页面板块名称（session-title-en）的英文样式此前为 font-size:12px 且不加粗，与中文（font-size:15px, font-weight:700）差异明显
  - 主持人英文（chair-en）此前已统一为 font-size:15px + font-weight:700，板块名称英文未同步，中英文视觉权重不一致可能让外国参会者感到不被尊重
  - 将板块名称英文样式对齐主持人英文样式，确保中英文视觉权重一致

- **涉及文件：**
  - `webpage/src/pages/Schedule.vue` — `.session-header-row .session-title-en` 样式规则

- **修改逻辑：**
  - `.session-title-en` 从 `font-size:12px; opacity:0.85; margin-top:2px` 改为 `font-size:15px; font-weight:700; opacity:0.85; margin-top:2px`
  - 修改后与 `.chair-en` 样式完全一致（font-size:15px, font-weight:700, opacity:0.85, margin-top:2px）

- **影响范围：**
  - 网页端日程页面：所有板块标题行的英文名称字号从12px增大到15px并加粗，与中文名称视觉权重一致
  - 不影响其他页面和功能

- **验证方式：**
  - 重读文件确认 `.session-title-en` 与 `.chair-en` 样式属性完全一致
  - 浏览器打开日程页面，板块名称中英文显示同字号同加粗

- **验证结果：** 文件重读确认样式一致；浏览器视觉验证未执行

---

## 2026-10-12

### [Enhancement] 配置文件模板化与敏感信息安全策略

- **目的：**
  - 将 `application-dev.yml` 和 `application-prod.yml` 中的敏感信息（数据库密码、API密钥、邮箱授权码等）从 Git 仓库中隔离
  - 创建 `.template` 模板文件供新开发者参考，所有敏感值替换为占位符（如 `YOUR_MYSQL_PASSWORD`）
  - 完善 README.md 配置文件说明章节，增加敏感字段标注和使用说明

- **涉及文件：**
  - `README.md` — 第九章配置文件说明章节重写，增加 9.1/9.2/9.3 三个子章节
  - `backend/src/main/resources/application-dev.yml.template` — 开发环境配置模板（新增）
  - `backend/src/main/resources/application-prod.yml.template` — 生产环境配置模板（新增）
  - `.gitignore` — 已在之前提交中添加 `**/application-dev.yml` 和 `**/application-prod.yml` 排除规则

- **修改逻辑：**
  - README.md 第九章重写为三个子章节：9.1 后端配置文件（含 Git 跟踪状态和模板使用方法）、9.2 后端配置段详解（含敏感字段标注 ⚠️）、9.3 前端配置文件
  - 模板文件保留完整结构和注释，所有敏感值替换为语义化占位符（`YOUR_MYSQL_PASSWORD`、`YOUR_ST_API_KEY`、`YOUR_AL_PRIVATE_KEY` 等）
  - 回调地址改为 localhost 占位符，避免暴露服务器 IP

- **影响范围：**
  - 文档：README.md 配置文件说明更完整，新开发者可快速上手
  - 安全：敏感配置文件已被 .gitignore 排除，模板文件不含任何真实密钥
  - 兼容：模板文件为 `.template` 后缀，不影响现有配置文件使用

- **验证方式：**
  - `git status` 确认 `application-dev.yml` 和 `application-prod.yml` 不在跟踪列表
  - 模板文件中搜索不到任何真实密钥、密码、IP 地址
  - README.md 配置文件说明章节内容完整

- **验证结果：** 待确认

---

## 2026-10-12

### [Feature] 论文投稿功能全栈实现（全英文过滤+单词计数+状态查询）

- **目的：**
  - 实现论文投稿完整业务流程：投稿须知确认→表单填写→文件上传→投稿提交→状态查询
  - 投稿表单强制全英文输入，通过watch+nextTick自动过滤中文字符
  - 摘要字段按Microsoft Word规则计算单词数（中文每字1词、英文按空格分词），上限500词，超限自动回退
  - 侧边栏新增投稿入口弹窗，管理端新增论文投稿管理页面

- **涉及文件：**
  - `webpage/src/pages/FirstDraft.vue` — 论文初稿投稿页面（新增）
  - `webpage/src/pages/Submit.vue` — 投稿页面（全英文过滤+单词计数增强）
  - `webpage/src/components/common/Sidebar.vue` — 侧边栏投稿弹窗
  - `webpage/src/locales/zh.js` — 中文翻译新增firstDraft命名空间
  - `webpage/src/locales/en.js` — 英文翻译新增firstDraft命名空间
  - `frontend/src/views/paperSubmission/paperSubmissionIndex/index.vue` — 管理端论文投稿管理

- **修改逻辑：**
  - FirstDraft.vue：新增论文初稿投稿页面，含投稿须知(v-html渲染)、同意勾选控制表单启用、全英文输入过滤(CHINESE_CHAR_REGEX=/[\u4e00-\u9fa5]/g + watch + nextTick)、摘要单词计数(countWords函数+MAX_ABSTRACT_WORDS=500)、超限回退(lastValidAbstract)、文件上传(仅pdf/docx)、投稿状态查询(邮箱+验证码)、详情/修改弹窗
  - Submit.vue：新增相同的全英文输入过滤和摘要单词计数逻辑，使用submitPaper API
  - Sidebar.vue：新增论文投稿弹窗(paperDialogVisible)，含投稿须知和投稿表单
  - locales：新增firstDraft命名空间完整中英文翻译（投稿须知文案、表单标签、验证提示、状态文案等）
  - 管理端：论文投稿列表管理（搜索、创建、删除、编辑、审核状态切换）

- **影响范围：**
  - 网页端：新增FirstDraft路由页面，Submit页面增强，侧边栏新增投稿入口
  - 管理端：论文投稿CRUD管理
  - 国际化：zh.js/en.js新增firstDraft翻译键

- **验证方式：**
  - 网页端FirstDraft页面：输入中文字符自动清除、摘要单词计数实时显示、超500词自动截断回退、文件上传仅接受pdf/docx
  - 网页端Submit页面：全英文过滤和单词计数功能同FirstDraft
  - 侧边栏投稿弹窗：点击投稿入口打开弹窗、勾选同意后表单可编辑
  - 管理端论文投稿页面：搜索/创建/编辑/删除/审核功能正常

- **验证结果：** 未执行

---

### [Enhancement] 日程类型中英文标签修正与后端自动填充映射

- **目的：**
  - 修正日程类型中英文标签为学术会议标准用语（KEYNOTE中文"专题发言"→"主旨报告"等）
  - 后端新增ITEM_TYPE_MAP枚举映射，当type/typeEn为空时根据itemType自动填充标准标签
  - 确保前后端类型标签一致，避免手动输入导致的不统一

- **涉及文件：**
  - `backend/sql/mysql/fix_keynote_type_label.sql` — 数据库标签修正脚本
  - `backend/src/main/java/com/youlai/boot/portal/service/impl/MeetingInfoServiceImpl.java` — ITEM_TYPE_MAP映射+fillTypeFromItemType自动填充

- **修改逻辑：**
  - SQL脚本修正6项标签：KEYNOTE中文"专题发言"→"主旨报告"；INVITED_SPEECH中文"邀请报告"→"专题发言"、英文"Invited Speech"→"Invited Speaker"；GENERAL_SPEECH英文"General Speech"→"Free Paper"；BREAK英文"Tea Break / Poster"→"Tea/Poster Break"；LUNCH英文"Lunch - Symposium"→"Lunchtime Symposium"；meeting_session中KEYNOTE板块title_en同步修正
  - 后端新增ITEM_TYPE_MAP静态映射（9种itemType→中英文标签对），新增fillTypeFromItemType方法在type为空时自动填充

- **影响范围：**
  - 数据库：meeting_agenda_item和meeting_session表标签数据修正
  - 后端API：日程查询返回的type/typeEn在为空时自动填充标准标签
  - 网页端：日程页面类型显示受后端返回标签影响

- **验证方式：**
  - 执行fix_keynote_type_label.sql后查询meeting_agenda_item验证标签已修正
  - 调用日程API验证type为空时自动填充正确标签

- **验证结果：** 未执行

---

### [Enhancement] 日程管理双语支持完善

- **目的：**
  - 新增日程弹窗支持中英文双语输入字段
  - 前端TypeScript类型定义完善，支持双语字段模型
  - 日程初始数据更新

- **涉及文件：**
  - `webpage/src/components/business/AddScheduleModal.vue` — 新增日程弹窗双语输入
  - `webpage/src/data/schedule.js` — 日程初始数据更新
  - `frontend/src/types/meeting/meeting.ts` — TypeScript类型定义完善

- **修改逻辑：**
  - AddScheduleModal.vue：表单新增中英文双语输入字段（类型CN/EN、主题CN/EN、讲者、单位CN/EN等）
  - schedule.js：更新日程初始数据结构
  - meeting.ts：MeetingSessionForm、AgendaItemForm等接口新增双语字段（titleEn、typeEn、timePeriodEn、locationEn、speakerNameEn等）

- **影响范围：**
  - 网页端：新增日程弹窗支持双语输入
  - 管理端：TypeScript类型定义影响会议日程相关组件

- **验证方式：**
  - 新增日程弹窗：中英文字段均可输入且独立保存
  - TypeScript编译无类型错误

- **验证结果：** 未执行

---

### [Feature] 测试日程数据脚本

- **目的：**
  - 提供覆盖所有条目类型的测试数据，用于本地开发环境验证日程UI场景

- **涉及文件：**
  - `backend/sql/mysql/test_schedule_data.sql` — 测试日程数据脚本

- **修改逻辑：**
  - 脚本先清空旧数据（meeting_session_participant→meeting_agenda_item→meeting_session→meeting_info），再插入2天会议数据
  - 覆盖板块类型：KEYNOTE、INVITED_SPEECH、DISCUSSION
  - 覆盖条目类型：OPENING、KEYNOTE、INVITED_SPEECH、GENERAL_SPEECH、BREAK、LUNCH、DISCUSSION、CLOSING
  - 包含特殊场景：待发布(TBD)、长标题、无讲者

- **影响范围：**
  - 本地开发环境：执行脚本后日程页面可展示所有类型条目

- **验证方式：**
  - 执行脚本后调用日程API验证数据完整性
  - 网页端日程页面验证所有类型条目正确渲染

- **验证结果：** 未执行

### [Fix] 论文投稿表单验证提示错误+滚动后错误消失

- **目的：**
  - 修复单位(company)字段只输入空格时，验证提示显示"company is required"而非"单位不能只输入空格"的问题
  - 修复表单验证错误3秒自动消失，用户滚动到页面下方后无法看到上方错误提示的问题

- **涉及文件：**
  - `webpage/src/pages/FirstDraft.vue` — 表单验证逻辑+模板修改

- **修改逻辑：**
  - 问题1修复：移除company字段el-form-item上的`required`属性（该属性触发Element Plus内置required校验，覆盖自定义validator的错误消息），将company验证规则从单个自定义validator拆分为`{ required: true }`规则+空格校验validator两条规则，确保空值显示"请输入所属单位/医院"、纯空格显示"单位不能只输入空格"
  - 问题2修复：移除el-form的`@validate="handleFormValidate"`事件和3秒自动清除逻辑，替换为`clearFieldValidate(prop)`（用户聚焦输入框时清除该字段错误）+`scrollToFirstError()`（提交校验失败时滚动到第一个错误字段）；给所有9个输入框添加`@focus="clearFieldValidate('propName')"`事件；修改`handleSubmit`校验失败时调用`scrollToFirstError()`平滑滚动到第一个`.el-form-item.is-error`元素

- **影响范围：**
  - 网页端FirstDraft页面：表单验证提示文案正确显示，校验失败自动滚动到错误字段

- **验证方式：**
  - 单位字段只输入空格后blur，验证提示应显示"单位不能只输入空格"而非"company is required"
  - 单位字段留空后blur，验证提示应显示"请输入所属单位/医院"
  - 多个字段校验失败后点击提交，页面自动滚动到第一个错误字段
  - 用户聚焦某个有错误提示的输入框，该字段错误提示自动清除

- **验证结果：** 未执行

### [Enhancement] 会议日程主持人中英文字号与板块名称对齐

- **目的：**
  - 会议日程列表中，主持人（Chair）的中文和英文字号、字重与板块名称不一致，视觉上不协调
  - 将主持人中英文字号和字重调整为与板块名称完全一致

- **涉及文件：**
  - `webpage/src/pages/Schedule.vue` — 日程页面样式修改

- **修改逻辑：**
  - `.session-chair`：移除父容器的`font-size: 12px`和`opacity: 0.9`（覆盖子元素样式）
  - `.chair-zh`：font-size 12px→15px，font-weight 600→700（与`.session-title`一致）
  - `.chair-en`：font-size 11px→12px，新增`font-weight: 700`（与`.session-title-en`一致）

- **影响范围：**
  - 网页端日程页面：板块标题行中主持人中英文字号和字重与板块名称统一

- **验证方式：**
  - 日程页面查看有主持人的板块，确认主持人中文15px加粗、英文12px加粗，与板块名称视觉一致

- **验证结果：** 未执行

---

### [Feature] 会前安排新增arrangeType字段，支持标题描述类型管理

- **目的：**
  - 网页端会议日程页面的"会议日程"标题和"详细议程于11月中旬公布"描述需要在管理端控制显示/隐藏
  - 统一在"会务门户管理>会前安排"中管理，与四个板块一致的管理体验
  - 通过新增arrange_type字段区分HEADER（标题描述）和BLOCK（板块）两种类型

- **涉及文件：**
  - `backend/sql/mysql/portal_arrange_add_arrange_type.sql` — 数据库迁移脚本
  - `backend/src/main/java/com/youlai/boot/portal/model/entity/Arrange.java` — 新增arrangeType字段
  - `backend/src/main/java/com/youlai/boot/portal/model/vo/ArrangePageVO.java` — 新增arrangeType字段
  - `backend/src/main/java/com/youlai/boot/portal/model/form/ArrangeForm.java` — 新增arrangeType字段
  - `backend/src/main/java/com/youlai/boot/portal/model/query/ArrangeQuery.java` — 新增arrangeType字段
  - `backend/src/main/java/com/youlai/boot/portal/service/impl/ArrangeServiceImpl.java` — getArrangePage支持arrangeType过滤
  - `frontend/src/views/portal/arrange/index.vue` — 搜索/表格/表单新增安排类型字段
  - `webpage/src/pages/Schedule.vue` — 按arrangeType分离HEADER和BLOCK数据渲染
  - `webpage/src/pages/PreMeeting.vue` — 过滤HEADER类型数据避免在板块列表中显示

- **修改逻辑：**
  - 数据库：portal_arrange表新增arrange_type varchar(20) DEFAULT 'BLOCK'列，插入HEADER默认数据（中英文各一条）
  - 后端：Entity/VO/Form/Query新增arrangeType字段，Service分页查询支持arrangeType过滤
  - 管理端：会前安排页面新增安排类型搜索、表格列和表单字段，支持HEADER/BLOCK选择
  - 网页端：Schedule.vue从listArrange中分离HEADER类型（渲染标题描述区域）和BLOCK类型（渲染板块列表），HEADER记录status=0时标题区域不显示

- **影响范围：**
  - 后端API：Arrange分页接口新增arrangeType查询参数，返回数据新增arrangeType字段
  - 管理端：会前安排CRUD支持类型区分
  - 网页端：会议日程标题描述区域受Arrange HEADER类型记录的status控制
  - 数据库：需执行迁移脚本portal_arrange_add_arrange_type.sql

- **验证方式：**
  - 管理端会前安排页面新增HEADER类型记录，设置status=0后网页端标题描述区域隐藏
  - 管理端会前安排页面按安排类型筛选HEADER/BLOCK记录
  - 网页端Schedule页面和PreMeeting页面板块列表不显示HEADER类型数据

- **验证结果：** 未执行（待数据库迁移后验证）

---

## 2026-08-12

### [Enhancement] 日程页面8项UI优化

- **目的：**
  - 日程1和日程2之间缺少视觉分隔
  - 日程1和日程2间距过小，视觉不够清晰
  - topic为"待发布"的数据需要特殊样式区分
  - 底部"注:最终日程以现场为准"红色过于醒目
  - 开幕式板块背景需要紫色渐变
  - 中餐条目背景需要统一为紫色系
  - 茶歇/壁报需要图标增强辨识度
  - 大会发言需要特殊格式突出显示

- **涉及文件：**
  - `webpage/src/pages/Schedule.vue` — 模板+样式8项修改
  - `backend/sql/mysql/test_schedule_data.sql` — 新增测试数据脚本

- **修改逻辑：**
  - 需求1：v-for改为template包裹，非最后一天后添加`.day-divider`分割线（紫色2px，透明度0.3）
  - 需求2：`.schedule-day`的margin-bottom从40px增大到60px
  - 需求3：`.td-topic`增加条件class `topic-pending`，当topic为"待发布"或topicEn为"TBD"时应用浅灰色(#bbb)斜体
  - 需求4：`.day-footer`颜色从#ff0000(红色)改为#666(深灰色)
  - 需求5：`.opening-full`背景从#4A90D9(蓝色)改为linear-gradient(90deg, #6A3E96, #8A68AC)紫色渐变
  - 需求6：`.lunch-full`背景从#FEE796(黄色)改为#8A68AC(紫色)
  - 需求7：茶歇/壁报special-full-row内增加☕图标span，`.break-icon`样式margin-right:6px
  - 需求8：大会发言td-merged-general-speech文本从"大会发言"改为"— 大会发言 —"

- **影响范围：**
  - 前台日程展示：所有8项UI变更
  - 测试数据：新增test_schedule_data.sql脚本覆盖所有条目类型

- **验证方式：**
  - 前台日程页面：两日之间有紫色分割线，间距增大
  - 前台日程页面：待发布topic显示为浅灰色斜体
  - 前台日程页面：底部注释为深灰色
  - 前台日程页面：开幕式背景紫色渐变、中餐背景紫色、茶歇有☕图标、大会发言"— 大会发言 —"格式

- **验证结果：**
  - 后端mvn package：待执行
  - 前端pnpm build：待执行
  - 网页端pnpm build：待执行
  - 业务链路：未执行端到端验证

- **备注：**
  - 所有修改仅涉及前端webpage，无后端变更
  - 测试数据脚本backend/sql/mysql/test_schedule_data.sql可用于本地环境初始化

### [Fix] 日程表格多项样式修正+日期星期重复修复

- **目的：**
  - 开幕式板块标题行文字未垂直水平居中
  - 茶歇/壁报背景颜色应为黑色（原为灰色#BEBEBE）
  - 条目topic在讲者/单位占两行时未垂直居中靠左对齐（如id:272"接触镜并发症的演变"）
  - 日程表格日期显示重复星期（如"1/8星期五 星期五"），双语版本星期未翻译

- **涉及文件：**
  - `webpage/src/pages/Schedule.vue` — 开幕式session-header居中样式+茶歇背景改黑色+topic垂直居中
  - `backend/src/main/java/com/youlai/boot/portal/service/impl/MeetingInfoServiceImpl.java` — setDate去掉中文星期拼接

- **修改逻辑：**
  - 需求1：session-header-cell增加条件class `session-header-center`，当session.itemType==='OPENING'时justify-content:center居中
  - 需求2：.break-full背景从#BEBEBE改为#000000（黑色）
  - 需求3：.td-topic增加display:flex;align-items:center，当同行讲者/单位占两行时topic垂直居中靠左对齐
  - 需求4：后端vo.setDate()去掉getChineseWeek()拼接，date字段只返回"1/8"格式，星期由week/weekEn字段单独展示，支持双语

- **影响范围：**
  - 前台日程展示：开幕式标题居中、茶歇/壁报黑色背景、topic垂直居中、日期不再重复星期
  - 后端API：allMeetingSchedule和page接口date字段格式变更（去掉中文星期后缀）
  - 管理端：分页查询date字段同步变更

- **验证方式：**
  - 前台日程页面：开幕式板块标题居中显示
  - 前台日程页面：茶歇/壁报行背景为黑色
  - 前台日程页面：讲者/单位占两行时topic垂直居中
  - 前台日程页面：日期显示"1/8 星期五"不重复，切换英文显示"1/8 FRIDAY"

- **验证结果：**
  - 后端mvn package：待执行
  - 前端pnpm build：待执行
  - 网页端pnpm build：待执行
  - 业务链路：未执行端到端验证

- **备注：**
  - 后端date字段格式变更属于接口变更，前端已通过week/weekEn字段独立展示星期，兼容双语

---

## 2026-08-11

### [Fix] 讲者/单位数据丢失+特殊条目独占一行+大会发言白色背景

- **目的：**
  - 管理端编辑条目输入讲者姓名和单位后，点击保存数据丢失，网页端和管理端都看不到
  - 开幕式/闭幕式/茶歇/中餐/大会发言应独占一行（单单元格），当前显示有两个单元格
  - 大会发言背景颜色应为白色

- **涉及文件：**
  - `backend/src/main/java/com/youlai/boot/portal/model/entity/MeetingAgendaItem.java` — 新增speakerName/speakerNameEn/speakerOrg/speakerOrgEn字段
  - `backend/src/main/java/com/youlai/boot/portal/service/impl/MeetingInfoServiceImpl.java` — createSession/updateSession保存讲者字段
  - `backend/src/main/resources/mapper/MeetingAgendaItemMapper.xml` — BaseResultMap新增4个字段映射，SQL查询使用COALESCE优先取条目自身讲者字段
  - `backend/sql/mysql/meeting_agenda_item_add_speaker_fields.sql` — 数据库迁移脚本：添加4个讲者字段列
  - `webpage/src/pages/Schedule.vue` — 特殊条目改为单单元格独占一行，大会发言背景改为白色

- **修改逻辑：**
  - 根因：meeting_agenda_item表没有speaker_name/speaker_org等列，前端输入的讲者信息无法保存
  - 实体MeetingAgendaItem新增4个字段，Mapper BaseResultMap添加映射
  - SQL查询使用COALESCE(i.speaker_name, s.name)优先取条目自身字段，回退到JOIN嘉宾表
  - createSession/updateSession中创建条目时设置speakerName/speakerNameEn/speakerOrg/speakerOrgEn
  - 前台special-full-row从双单元格(时间+内容)改为单单元格独占一行
  - 大会发言.general-speech-full背景从#F5A623(橙色)改为#fff(白色)

- **影响范围：**
  - 管理端条目编辑：讲者姓名/单位中英文数据正确保存和回显
  - 前台日程展示：特殊条目(开幕式/闭幕式/茶歇/中餐/大会发言)独占一行单单元格
  - 数据库：需执行迁移脚本添加4个字段

- **验证方式：**
  - 管理端编辑条目→输入讲者姓名和单位→保存→重新打开→数据保留
  - 前台日程页面：特殊条目独占一行，无时间列和内容列区分
  - 大会发言背景白色

- **验证结果：**
  - 后端mvn package：成功
  - 前端pnpm build：成功
  - 网页端pnpm build：成功
  - 数据库迁移：待线上执行
  - 业务链路：未执行端到端验证

- **备注：**
  - 必须在线上数据库执行meeting_agenda_item_add_speaker_fields.sql后重新部署

### [Fix] 线上meeting_session表缺少item_type列导致SQLSyntaxErrorException

- **目的：**
  - 线上管理端点击会议日程报错：Unknown column 'item_type' in 'field list'
  - Java实体MeetingSession已有itemType字段映射item_type列，但线上数据库缺少该列

- **涉及文件：**
  - `backend/sql/mysql/meeting_session_add_item_type.sql` — 新增迁移脚本

- **修改逻辑：**
  - ALTER TABLE meeting_session ADD COLUMN item_type varchar(30) NULL DEFAULT NULL AFTER start_time
  - 列定义与mclc.sql中meeting_agenda_item.item_type保持一致

- **影响范围：**
  - meeting_session表结构，需在线上数据库执行迁移脚本

- **验证方式：**
  - 执行迁移脚本后，管理端会议日程页面正常加载
  - 后端mvn package：成功

- **验证结果：**
  - 后端mvn package：成功
  - 数据库迁移：待线上执行

- **备注：**
  - 必须在线上数据库执行meeting_session_add_item_type.sql后重新部署

### [Feature] 会议日程管理全链路优化：编辑弹窗+条目排序+茶歇/中餐条目化+前台UI重构

- **目的：**
  - 管理端会议操作列"禁用"改为"编辑"，新增会议编辑弹窗（名称/英文名/日期/会场/排序/状态）
  - 板块类型下拉移除BREAK/LUNCH，仅保留SESSION_TYPE_OPTIONS；BREAK/LUNCH改为条目类型
  - 条目itemType变更时自动填充type/typeEn，后端添加安全网防NOT NULL报错
  - 条目排序(sortOrder)保存/读取修复：AgendaItemVO添加sortOrder字段，编辑弹窗映射修复
  - 前台日程表格重构：同日板块合并表格、列对齐、BREAK/LUNCH条目独占整行含时间列
  - 条目按sortOrder顺序渲染，getItemRenderBlocks函数交错处理BREAK/LUNCH与正常条目
  - 移除Controller所有@PreAuthorize注解解决"访问未授权"

- **涉及文件：**
  - `frontend/src/types/meeting/meeting.ts` — 新增SESSION_TYPE_OPTIONS(过滤BREAK/LUNCH)、MeetingEditForm接口
  - `frontend/src/api/portal/meeting.ts` — 新增updateMeeting API
  - `frontend/src/views/portal/meeting/index.vue` — 编辑弹窗+SESSION_TYPE_OPTIONS+onItemTypeChange+sortOrder映射修复+span-method
  - `webpage/src/pages/Schedule.vue` — 模板重构：同日合并表格+getItemRenderBlocks+special-full-row+列对齐+垂直居中
  - `backend/src/main/java/com/youlai/boot/portal/model/vo/AgendaItemVO.java` — 新增sortOrder字段
  - `backend/src/main/java/com/youlai/boot/portal/model/vo/SessionVO.java` — 新增sortOrder字段
  - `backend/src/main/java/com/youlai/boot/portal/model/vo/MeetingScheduleVO.java` — 新增meetingDate/sortOrder字段
  - `backend/src/main/java/com/youlai/boot/portal/model/form/AgendaItemForm.java` — 条目表单
  - `backend/src/main/java/com/youlai/boot/portal/controller/MeetingScheduleController.java` — PUT /meeting/{id}+移除@PreAuthorize
  - `backend/src/main/java/com/youlai/boot/portal/service/impl/MeetingInfoServiceImpl.java` — updateMeeting+fillTypeFromItemType+meetingDate/sortOrder填充
  - `backend/src/main/resources/mapper/MeetingAgendaItemMapper.xml` — sort_order字段映射

- **修改逻辑：**
  - 前端新增SESSION_TYPE_OPTIONS过滤BREAK/LUNCH，板块类型下拉改用它，条目类型下拉保留ITEM_TYPE_OPTIONS
  - 前端onItemTypeChange：条目类型变更时自动从ITEM_TYPE_OPTIONS映射填充type/typeEn
  - 后端fillTypeFromItemType：保存条目时type为空则从ITEM_TYPE_MAP自动填充中英文名称
  - AgendaItemVO+Mapper XML添加sortOrder字段映射，修复编辑弹窗sortOrder硬编码为1的bug
  - 前台getItemRenderBlocks：按sortOrder顺序将条目处理为special块(BREAK/LUNCH)和timeGroup块(正常条目)
  - 前台special-full-row：BREAK/LUNCH条目独占整行(含12%时间列)，时间空时也填充背景色
  - 前台列对齐：time-group-items内td宽度按88%容器比例重算与表头5列严格对齐
  - 前台垂直居中：td-type/td-speaker/td-affiliation添加flex垂直居中

- **影响范围：**
  - 会议日程管理CRUD全链路
  - 前台日程展示页面布局与渲染逻辑
  - 条目排序数据完整性

- **验证方式：**
  - 管理端编辑板块→修改条目排序→保存→重新打开→排序值保留
  - 管理端添加BREAK/LUNCH条目→不填type→保存成功(自动填充)
  - 前台日程页面：BREAK/LUNCH条目按sortOrder位置显示、独占整行、时间列填充背景色
  - 前台表格列对齐、单行内容垂直居中

- **验证结果：**
  - 后端mvn package：成功
  - 前端pnpm build：成功
  - 网页端pnpm build：成功
  - 业务链路：未执行端到端验证

- **备注：**
  - 数据库meeting_agenda_item表的type字段为NOT NULL，后端fillTypeFromItemType确保不报错

### [Fix] sortOrder编辑还原+条目数据丢失+新增GENERAL_SPEECH条目类型

- **目的：**
  - 管理端编辑条目排序值后点击确定，再次打开弹窗排序值还原为1
  - 添加20+条条目数据，实际只显示13条（部分条目丢失）
  - 新增"大会发言"(GENERAL_SPEECH)条目类型，前台同时间段合并显示

- **涉及文件：**
  - `frontend/src/types/meeting/meeting.ts` — AgendaItemVO新增sortOrder/speakerId字段，AgendaItemForm新增speakerId字段，ITEM_TYPE_OPTIONS新增GENERAL_SPEECH，SESSION_TYPE_OPTIONS过滤GENERAL_SPEECH
  - `frontend/src/views/portal/meeting/index.vue` — handleEditSession映射条目时传递id和speakerId
  - `backend/src/main/java/com/youlai/boot/portal/model/form/AgendaItemForm.java` — 新增speakerId字段
  - `backend/src/main/java/com/youlai/boot/portal/service/impl/MeetingInfoServiceImpl.java` — ITEM_TYPE_MAP新增GENERAL_SPEECH映射，createSession/updateSession中创建条目时设置speakerId
  - `backend/src/main/resources/mapper/MeetingAgendaItemMapper.xml` — AgendItemVOMap新增speaker_id→speakerId映射，SQL查询新增i.speaker_id字段
  - `webpage/src/pages/Schedule.vue` — isItemSpecialType/getSpecialTypeText/getSpecialTypeClass新增GENERAL_SPEECH支持，CSS新增.general-speech-full样式

- **修改逻辑：**
  - sortOrder还原根因：前端AgendaItemVO接口缺少sortOrder字段，item.sortOrder始终为undefined，?? 1回退到1
  - 条目丢失根因：handleEditSession映射时未传递id和speakerId，updateSession先删后增时数据不完整
  - 修复：AgendaItemVO添加sortOrder/speakerId字段，handleEditSession映射时传递id/speakerId/sortOrder
  - 后端AgendaItemForm添加speakerId字段，createSession/updateSession中设置item.setSpeakerId()
  - Mapper XML AgendItemVOMap新增speaker_id映射，SQL查询新增i.speaker_id
  - GENERAL_SPEECH作为条目级特殊类型，前台渲染为独占整行的special块，显示时间列+内容列"大会发言"
  - ITEM_TYPE_MAP新增Map.entry("GENERAL_SPEECH", new String[]{"大会发言", "General Speech"})

- **影响范围：**
  - 管理端会议日程编辑弹窗：sortOrder正确回显，条目数据完整保存
  - 前台日程展示：GENERAL_SPEECH条目独占整行，橙色背景(#F5A623)

- **验证方式：**
  - 管理端编辑板块→修改条目排序值→保存→重新打开→排序值保留
  - 管理端添加20+条条目→保存→重新打开→所有条目完整显示
  - 管理端选择GENERAL_SPEECH条目类型→自动填充"大会发言"/"General Speech"
  - 前台日程页面：GENERAL_SPEECH条目独占整行，显示时间+橙色背景"大会发言"

- **验证结果：**
  - 后端mvn package：成功
  - 前端pnpm build：成功
  - 网页端pnpm build：成功
  - 业务链路：未执行端到端验证

## 2026-08-10

### [Refactor] 邮件退信重试机制全面改造：扁平关联+树形展示+条件告警+事务解耦

- **目的：**
  - 原始邮件退信后最多自动重试2次，重试记录通过root_record_id扁平关联原始记录（非递归），重试记录不能再触发重试
  - 管理员告警仅在同一原始邮件第一次退信时发送一次，后续重试失败不再通知
  - 邮件中心改为树形展示（原始邮件为父节点，重试记录为子节点），重试记录不出现在一级列表
  - 退信后状态必须正确显示"投递失败/退信"而非"已提交至邮件服务器"
  - 幂等性：同一退信事件不重复创建重试记录、不重复发送管理员告警
  - 事务边界：管理员通知与重试必须解耦，邮件发送IO不能长时间占用数据库事务

- **涉及文件：**
  - `backend/sql/mysql/mail_send_record_retry_increment.sql` — 新增数据库迁移脚本
  - `backend/src/main/java/com/youlai/boot/support/mail/model/entity/MailSendRecord.java` — 新增recordType/retrySequence/rootRecordId字段
  - `backend/src/main/java/com/youlai/boot/support/mail/enums/MailSendStatus.java` — 新增RETRY_PENDING(6)/RETRY_SUCCESS(7)/RETRY_FAILED(8)枚举
  - `backend/src/main/java/com/youlai/boot/support/mail/model/vo/MailSendRecordPageVO.java` — 新增recordType/retrySequence/rootRecordId/children字段
  - `backend/src/main/java/com/youlai/boot/support/mail/model/vo/MailSendRecordDetailVO.java` — 新增recordType/retrySequence/rootRecordId字段
  - `backend/src/main/resources/mapper/support/MailSendRecordMapper.xml` — 分页查询过滤非ORIGINAL记录+新字段映射+getRetryRecordsByRootIds
  - `backend/src/main/java/com/youlai/boot/support/mail/mapper/MailSendRecordMapper.java` — 新增getRetryRecordsByRootIds方法
  - `backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java` — 核心改造：handleBounceEnhanced事务拆分+扁平重试+条件告警+markBounce适配+retry适配+树形组装
  - `frontend/src/api/portal/mailSendRecord.ts` — PageVO和DetailVO新增recordType/retrySequence/rootRecordId/children字段
  - `frontend/src/views/mailRecord/index.vue` — 树形展示+新状态映射+类型列+操作按钮适配+详情弹窗增强

- **修改逻辑：**
  - 数据库：新增record_type(VARCHAR(20))/retry_sequence(INT)/root_record_id(BIGINT)三字段，uk_root_retry唯一约束，idx_root_record_id/idx_record_type索引，回填现有数据
  - Entity：MailSendRecord新增recordType(String)/retrySequence(Integer)/rootRecordId(Long)
  - 枚举：MailSendStatus新增RETRY_PENDING(6,"待重试")/RETRY_SUCCESS(7,"重试成功")/RETRY_FAILED(8,"重试失败")
  - VO：PageVO新增children字段用于树形展示，DetailVO新增recordType/retrySequence/rootRecordId
  - Mapper.xml：分页查询增加`(record_type='ORIGINAL' OR record_type IS NULL OR parent_record_id IS NULL)`过滤，send_status CASE增加6/7/8映射，新增getRetryRecordsByRootIds查询
  - Service：handleBounceEnhanced全面重构——移除方法级@Transactional改用TransactionTemplate编程式事务；事务内：幂等检查+5级匹配+写bounce_record+更新delivery_status+黑名单+创建重试记录；事务外：管理员告警(仅第一次)+邮件发送；仅ORIGINAL记录触发重试，RETRY记录永不触发重试；root_record_id始终指向原始记录ID
  - Service：markBounce支持RETRY_SUCCESS(7)状态标记退信，重试记录退信用RETRY_FAILED(8)，仅原始记录手动标记时发送管理员告警
  - Service：retry支持RETRY_FAILED(8)状态重试
  - Service：getMailSendRecordPage树形组装——批量查询children按rootRecordId分组设置children
  - 前端：表格树形展示row-key="id"+tree-props，新增"类型"列(原始/重试#N标签)，状态筛选增加6/7/8选项，综合状态函数增加RETRY_PENDING/RETRY_SUCCESS/RETRY_FAILED处理，操作按钮适配新状态，详情弹窗增加记录类型和rootRecordId

- **影响范围：**
  - 邮件退信自动重试全链路
  - 管理员退信告警机制
  - 邮件中心列表展示和操作
  - 邮件发送记录数据结构

- **验证方式：**
  - 后端Maven编译验证通过
  - 端到端业务链路待部署后验证

- **验证结果：**
  - Maven编译：成功
  - 端到端验证：未执行（需部署后验证）

- **备注：**
  - 需执行数据库迁移脚本`mail_send_record_retry_increment.sql`
  - 退信检测链路(IMAP+BounceParser)未修改，仅改造退信后处理逻辑

## 2026-08-07

### [Fix] 邮件重发成功后 sendErrorMsg 未清除 + 邮件中心/黑名单页面自动刷新

- **目的：**
  - 邮件重发成功后，`paper_submission_email.send_error_msg` 字段未随 `send_status` 一起更新为 null，导致重发成功后仍残留旧错误信息
  - 邮件中心和黑名单页面使用了 `<keep-alive>` 缓存，从菜单再次进入时 `onMounted` 不触发，数据不刷新

- **涉及文件：**
  - `backend/src/main/java/com/youlai/boot/portal/model/entity/PaperSubmissionEmail.java` — sendErrorMsg 字段添加 `@TableField(updateStrategy = FieldStrategy.ALWAYS)`
  - `frontend/src/views/mailRecord/index.vue` — 添加 `onActivated` 钩子自动刷新
  - `frontend/src/views/mailRecord/blacklist.vue` — 添加 `onActivated` 钩子自动刷新

- **修改逻辑：**
  - `PaperSubmissionEmail.sendErrorMsg`：MyBatis-Plus 默认字段策略为 `NOT_NULL`，`updateById()` 时 null 值字段不会出现在 UPDATE SQL 中。添加 `@TableField(updateStrategy = FieldStrategy.ALWAYS)` 后，null 值也会写入数据库，确保重发成功时 `setSendErrorMsg(null)` 能正确清除旧错误信息
  - `mailRecord/index.vue`：引入 `onActivated` 钩子，在组件从 `<keep-alive>` 缓存激活时调用 `handleSearch()` 刷新列表
  - `mailRecord/blacklist.vue`：同上，引入 `onActivated` 钩子自动刷新黑名单列表

- **影响范围：**
  - `PaperSubmissionServiceImpl.resendReviewEmail()` 中 `emailRecord.setSendErrorMsg(null)` 现在能正确生效
  - 邮件中心页面每次从菜单进入都会刷新数据
  - 黑名单页面每次从菜单进入都会刷新数据

- **验证方式：**
  - 后端编译验证
  - 前端打包验证
  - 线上验证：重发失败邮件后检查 `paper_submission_email.send_error_msg` 是否被清除为 null

### [Fix] MailBounceRecord.references 字段映射错误导致退信检测 SQL 报 Unknown column

- **目的：**
  - 退信检测定时任务 `BounceEmailJob` 执行时，查询 `mail_bounce_record` 表报 `Unknown column 'references' in 'field list'` 错误。根因：数据库实际列名为 `references_header`（避免 MySQL 保留关键字），但实体类 `@TableField` 映射到了 `` `references` ``，导致 SQL 查询不存在的列

- **涉及文件：**
  - `backend/src/main/java/com/youlai/boot/support/mail/model/entity/MailBounceRecord.java` — `references` 字段 `@TableField` 从 `` value = "`references`" `` 改为 `value = "references_header"`

- **修改逻辑：**
  - `MailBounceRecord.references`：数据库迁移脚本 `mail_bounce_record_enhanced.sql` 中列名定义为 `references_header`（避免 MySQL 保留关键字 `REFERENCES`），实体类 `@TableField` 应映射到实际列名 `references_header`，而非使用反引号转义的 `` `references` ``

- **影响范围：**
  - `MailBounceRecordServiceImpl.findByBounceMessageId()` — 修复后 SELECT 查询使用正确列名 `references_header`
  - `BounceEmailJob.processBounceEmail()` — 退信检测流程恢复正常，退信记录可正常写入和查询
  - 所有涉及 `mail_bounce_record` 表的 CRUD 操作均受影响

- **验证方式：**
  - 后端编译验证
  - 线上验证：退信检测定时任务不再报 `BadSqlGrammarException` / `Unknown column`，退信记录正常入库

### [Fix] mail_bounce_record_enhanced.sql 迁移脚本非幂等导致重复执行报 Duplicate column

- **目的：**
  - 执行 `ALTER TABLE mail_bounce_record ADD COLUMN references_header ...` 报错 `1060 - Duplicate column name 'references_header'`，因为该列已存在于数据库中。原脚本未做列存在性检查，重复执行必然报错

- **涉及文件：**
  - `backend/sql/mysql/mail_bounce_record_enhanced.sql` — 全部 ALTER TABLE ADD COLUMN / ADD INDEX 改为幂等存储过程调用

- **修改逻辑：**
  - 新增 `add_column_if_not_exists` 存储过程：通过 `INFORMATION_SCHEMA.COLUMNS` 检查列是否存在，不存在才执行 ADD COLUMN
  - 新增 `add_index_if_not_exists` 存储过程：通过 `INFORMATION_SCHEMA.STATISTICS` 检查索引是否存在，不存在才执行 ADD INDEX
  - 所有 10 个 ADD COLUMN 和 2 个 ADD INDEX 均改为 CALL 调用，脚本可安全重复执行
  - 脚本末尾清理临时存储过程

- **影响范围：**
  - 迁移脚本可安全重复执行，已执行过的列/索引会自动跳过
  - 数据库结构无变化，仅脚本执行逻辑变更

- **验证方式：**
  - 重复执行脚本不再报 `Duplicate column name` 或 `Duplicate key name` 错误

- **目的：**
  - 退信检测定时任务 `BounceEmailJob` 执行时，查询 `mail_bounce_record` 表报 SQL 语法错误：`references` 是 MySQL 保留关键字（用于外键约束 `REFERENCES`），MyBatis-Plus 生成 SQL 时未加反引号，导致 `SELECT ... references, bounce_date ...` 被MySQL解析器误认为关键字

- **涉及文件：**
  - `backend/src/main/java/com/youlai/boot/support/mail/model/entity/MailBounceRecord.java` — `references` 字段添加 `@TableField(value = "\`references\`")`

- **修改逻辑：**
  - `MailBounceRecord.references`：添加 `@TableField(value = "\`references\`")` 注解，MyBatis-Plus 生成 SQL 时将使用反引号转义：`` `references` ``，避免 MySQL 将其解析为保留关键字

- **影响范围：**
  - `MailBounceRecordServiceImpl.findByBounceMessageId()` — 修复后 SELECT 查询不再报语法错误
  - `BounceEmailJob.processBounceEmail()` — 退信检测流程恢复正常，退信记录可正常写入和查询
  - 所有涉及 `mail_bounce_record` 表的 CRUD 操作均受影响

- **验证方式：**
  - 后端编译验证
  - 线上验证：退信检测定时任务不再报 `BadSqlGrammarException`，退信记录正常入库

### [Refactor] MailBounceRecord.references 字段重命名为 referencesHeader，消除 MySQL 保留字隐患

- **目的：**
  - `references` 是 MySQL 保留关键字（`REFERENCES`，用于外键约束），作为 Java 属性名时，MyBatis-Plus 生成的 SELECT 别名仍为 `references`，在某些查询上下文中可能触发保留字冲突。将 Java 字段名从 `references` 重命名为 `referencesHeader`，从根本上消除隐患

- **涉及文件：**
  - `backend/src/main/java/com/youlai/boot/support/mail/model/entity/MailBounceRecord.java` — `private String references` → `private String referencesHeader`
  - `backend/src/main/java/com/youlai/boot/support/mail/model/dto/BounceParseResult.java` — `private String references` → `private String referencesHeader`
  - `backend/src/main/java/com/youlai/boot/support/mail/parser/BounceParser.java` — `setReferences()` → `setReferencesHeader()`，参数名 `references` → `referencesHeader`

- **修改逻辑：**
  - `MailBounceRecord.references` → `referencesHeader`：`@TableField(value = "references_header")` 映射不变，仅 Java 属性名从 `references` 改为 `referencesHeader`，MyBatis-Plus 生成的 SELECT 别名也将变为 `referencesHeader`，彻底避免 MySQL 保留字冲突
  - `BounceParseResult.references` → `referencesHeader`：DTO 字段同步重命名，保持实体与 DTO 命名一致
  - `BounceParser.parse()`：方法参数名 `references` → `referencesHeader`，setter 调用 `setReferences()` → `setReferencesHeader()`，方法内局部变量引用同步更新

- **影响范围：**
  - `BounceParser.parse()` — 参数名和 setter 调用已更新
  - `BounceEmailJob` — 未直接调用 `setReferences()/getReferences()`，无需修改（其中 `references` 是局部变量 `message.getHeader("References")`，与实体字段无关）
  - `extractLastMessageIdFromReferences()` — 私有静态方法，参数名 `references` 是局部变量，与 MySQL 保留字无关，无需修改

- **验证方式：**
  - 后端编译验证
  - 线上验证：退信检测定时任务正常执行，退信记录正常入库

---

## 2026-08-06

### [Refactor] 退信解析链路重构 Phase 1-2：数据模型独立 + BounceParser DSN标准重写

- **目的：**
  - 退信解析链路原有架构存在严重缺陷：BounceParser内部类耦合、DSN标准字段未解析、DNS错误分类粗放、收件人提取仅靠正文正则、Header字段未采集
  - 重构目标：DSN标准字段优先解析、退信通知与原始邮件严格分离、DNS分类细化、收件人提取三级优先级、Header提取原始Message-ID
  - 验收标准：真实网易退信必须正确解析为 bounceSender=Postmaster@163.com, bounceRecipient=registrationmclc@163.com, originalRecipient=maojiangjun@faxmai.com

- **Phase 1 数据模型重构：**
  - `BounceParseResult` 从 `BounceParser` 内部类提取为独立顶级DTO，新增 dsnStatus/dsnDiagnosticCode/bounceSender/bounceRecipient/originalRecipient/originalSender/bounceMessageId/inReplyTo/references/matchType/matchConfidence 字段
  - `BounceType` 枚举新增 DOMAIN_NOT_FOUND(永久)/DNS_TEMPORARY_FAILURE(临时)/DNS_ERROR(默认临时) 三级DNS分类，替代原单一DNS_ERROR
  - 新增 `BounceMatchResult` DTO 封装匹配结果（matchType/matchConfidence/matchedRecord/bounceParseResult）
  - 新增 `MailBounceRecord` 实体 + `mail_bounce_record.sql` 增量脚本（退信记录独立表）
  - 新增 `mail_imap_checkpoint.sql` 增量脚本（IMAP检查点表，避免重复扫描）
  - 新增 `mail_bounce_blacklist.sql` 增量脚本（退信黑名单表）

- **Phase 2 BounceParser重写：**
  - DSN标准字段优先解析：Final-Recipient > Original-Recipient > Body Fallback 三级收件人提取
  - RFC 3463 DSN状态码分类：5.1.2→DOMAIN_NOT_FOUND, 4.4.4→DNS_TEMPORARY_FAILURE, 5.4.4→DNS_ERROR
  - 退信通知与原始邮件严格分离：bounceSender/bounceRecipient vs originalRecipient/originalSender
  - 8参数parse()方法：新增 bounceRecipient/inReplyTo/references/bounceMessageId 参数
  - Header提取原始Message-ID：In-Reply-To取Message-ID、References取最后一个Message-ID（修复原取第一个的bug）
  - 6级匹配策略：MESSAGE_ID(100) → IN_REPLY_TO(95) → REFERENCES(90) → BOUNCE_RECIPIENT(70) → ORIGINAL_RECIPIENT(60) → RECEIVER_TIME_WINDOW(40)
  - 向后兼容：@Deprecated旧4参数parse()保留 + getBouncedRecipient()桥接方法返回originalRecipient

- **涉及文件：**
  - `backend/src/main/java/com/youlai/boot/support/mail/parser/BounceParser.java` — 完整重写（~500行）
  - `backend/src/main/java/com/youlai/boot/support/mail/model/dto/BounceParseResult.java` — 内部类提取为顶级DTO
  - `backend/src/main/java/com/youlai/boot/support/mail/model/dto/BounceMatchResult.java` — 新增匹配结果DTO
  - `backend/src/main/java/com/youlai/boot/support/mail/enums/BounceType.java` — 新增DNS三级分类枚举
  - `backend/src/main/java/com/youlai/boot/support/mail/model/entity/MailBounceRecord.java` — 新增退信记录实体
  - `backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java` — 3处BounceParser.BounceParseResult引用更新为顶级DTO
  - `backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java` — 2处BounceParser.BounceParseResult引用更新+import添加
  - `backend/sql/mysql/mail_bounce_record.sql` — 新增退信记录表
  - `backend/sql/mysql/mail_imap_checkpoint.sql` — 新增IMAP检查点表
  - `backend/sql/mysql/mail_bounce_blacklist.sql` — 新增退信黑名单表
  - `frontend/src/api/portal/mailBounceRecord.ts` — MailBounceRecord接口新增10个字段类型定义
  - `frontend/src/views/mailRecord/index.vue` — 退信详情弹窗重构为分类展示+新增匹配置信度进度条+新增匹配策略标签

- **影响范围：**
  - 退信解析完整链路：BounceParser → BounceEmailJob → MailSendRecordServiceImpl
  - BounceType枚举新增3个值，原有枚举值不变，向后兼容
  - 旧接口parse(subject, sender, contentType, textContent)保留并标记@Deprecated，确保向后兼容
  - 前端退信详情弹窗按后端实体分类重构：退信分类/匹配信息/原始邮件/退信通知/DSN字段/Header辅助/处理状态
  - 前端新增匹配置信度进度条展示（matchConfidence 0-100）
  - 前端新增收件人信息来源标签（recipientSource）
  - 前端匹配策略映射新增 FINAL_RECIPIENT_TIME_WINDOW/ORIGINAL_RECIPIENT_TIME_WINDOW

- **验证方式：**
  - mvn compile编译通过
  - BounceParser单元测试：DSN标准字段解析、DNS分类、收件人三级优先级、Header提取
  - 真实网易退信样本验证（待Phase 8实施）

- **验证结果：**
  - 后端编译：成功
  - 端到端业务链路：待Phase 3-8实施后验证

- **备注：**
  - 本次为退信链路8阶段重构的Phase 1-2，仅完成数据模型和解析器重写
  - Phase 3-8需依次实施：发送链路→IMAP Scanner→BounceMatcher→Service→自动重发→测试
  - BounceParser旧4参数parse()标记@Deprecated但保留，避免破坏现有调用链
  - DNS分类细化：DOMAIN_NOT_FOUND(5.1.2)为永久失败不应重试，DNS_TEMPORARY_FAILURE(4.4.4)为临时故障可重试

---

## 2026-08-05

### [Fix] 邮件退信检测定时任务未启动修复（@EnableScheduling缺失）

- **目的：**
  - mail_send_record中business_id=36的数据在邮件服务器实际被退信，但数据库显示send_status=1（已提交至邮件服务器），退信检测未工作
  - 测试案例：yuanwenyi0239@126.com（550 User not found）、xxx@faxmai.com（DNS query error: faxmai.com）

- **根因分析：**
  - `YouLaiBootApplication`缺少`@EnableScheduling`注解，导致Spring Boot不会调度任何`@Scheduled`定时任务
  - `BounceEmailJob.detectBounceEmails()`的`@Scheduled(cron)`注解完全无效，IMAP退信检测从未执行
  - `AsyncConfig`只有`@EnableAsync`，同样缺少`@EnableScheduling`
  - 退信检测完整代码链路（IMAP连接→增量扫描→退信识别→BounceParser解析→5级匹配→双状态更新→黑名单→告警→自动重发）均存在但因定时任务未启动而从未运行

- **修复内容：**
  - `YouLaiBootApplication`添加`@EnableScheduling`注解，激活Spring定时任务调度
  - `BounceEmailJob`添加`@PostConstruct init()`启动日志，输出完整IMAP配置信息（host/port/cron/策略等），便于确认定时任务是否已注册
  - `BounceEmailJob.isBounceEmail()`添加debug级别策略匹配日志（发送者匹配/主题匹配/ContentType匹配）
  - `BounceEmailJob.detectBounceEmails()`主循环添加INFO级别退信识别日志

- **涉及文件：**
  - `backend/src/main/java/com/youlai/boot/YouLaiBootApplication.java` — 添加@EnableScheduling
  - `backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java` — @PostConstruct启动日志+退信识别日志

- **影响范围：**
  - 所有@Scheduled定时任务（BounceEmailJob退信检测、及其他可能存在的定时任务）
  - 邮件退信检测完整链路：IMAP连接→扫描→识别→解析→匹配→更新→告警

- **验证方式：**
  - mvn compile编译通过
  - 部署后启动日志应出现"====== 退信检测定时任务已注册 ======"及完整配置信息
  - 5分钟后日志应出现"退信检测定时任务开始执行..."
  - IMAP连接成功后应出现"退信检测：IMAP连接成功，host=imap.163.com"
  - 退信识别后应出现"退信检测：识别到退信邮件"
  - 匹配成功后应出现"退信匹配成功[MESSAGE_ID/IN_REPLY_TO/RECEIVER_TIME_WINDOW]"
  - 状态更新后mail_send_record.send_status=5(BOUNCE_DETECTED), delivery_status=2(BOUNCED)

- **验证结果：**
  - 后端编译：成功
  - 生产端到端：待部署验证

- **生产部署前必须确认：**
  - 数据库已执行`mail_send_record_delivery_increment.sql`（delivery_status等字段）
  - 数据库已执行`mail_bounce_record.sql`（退信记录表）
  - 数据库已执行`mail_imap_checkpoint.sql`（IMAP检查点表）
  - 数据库已执行`mail_bounce_blacklist.sql`（黑名单表）
  - application-prod.yml中spring.mail.imap.enabled=true已配置

- **备注：**
  - SMTP发送成功≠邮件最终送达，必须通过IMAP退信检测确认最终投递状态
  - 前端退信状态展示已完整（send_status=5→"退信-投递失败"，delivery_status=2→"已退信"）

### [Fix] 163邮箱IMAP "Unsafe Login"根因修复：发送IMAP ID命令（RFC 2971）

- **目的：**
  - 生产部署后IMAP连接报错"A3 NO SELECT Unsafe Login. Please contact kefu@188.com for help"，退信检测无法工作
  - 排除授权码错误（已确认是授权码）、IMAP服务未开启（已确认开启）、IP白名单（已添加）后，问题仍然存在

- **根因分析：**
  - **163/126邮箱不遵循标准IMAP规范**，要求客户端在连接后发送IMAP ID命令（RFC 2971）标识自身身份
  - 不发送ID命令的客户端会被网易邮箱视为"不安全登录"，返回"Unsafe Login"错误
  - 这是网易邮箱的安全策略，与授权码、IP白名单无关
  - 参考：CSDN多篇文章确认此问题（JavaMail读取网易邮箱报NO SELECT Unsafe Login）

- **修复内容：**
  - `BounceEmailJob`新增`sendImapId(Folder)`方法，在打开文件夹前发送IMAP ID命令
  - 使用反射调用`IMAPFolder.id(Map)`方法，避免对`angus-mail`的编译时依赖（其scope为runtime）
  - ID命令参数：name=MCLC-BounceChecker, version=1.0, vendor=MCLC, contact=配置的邮箱账号
  - `connectImap()`中`AuthenticationFailedException`的"Unsafe Login"日志更新根因描述为IMAP ID命令
  - `detectBounceEmails()`在`folder.open()`前调用`sendImapId(folder)`

- **涉及文件：**
  - `backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java` — 新增sendImapId()方法+调用点+日志修正

- **影响范围：**
  - IMAP连接流程：连接后、打开文件夹前新增ID命令发送步骤
  - 不影响非163/126邮箱（ID命令对支持RFC 2971的服务器无害，对不支持的服务器会跳过）

- **验证方式：**
  - mvn compile编译通过
  - 部署后日志应出现"退信检测：已发送IMAP ID命令（RFC 2971）"
  - 随后应出现"退信检测：IMAP连接成功，host=imap.163.com"

- **验证结果：**
  - 后端编译：成功
  - 生产端到端：待部署验证

---

## 2026-08-04

### [Fix] 演讲锦集视频上传失败修复（nginx超时+前端超时+OSS路径优化）

- **目的：**
  - 生产环境演讲锦集上传专家视频失败，表现为404和MultipartException，根因为nginx代理超时+50x.html缺失+前端超时过短+视频存储路径错误

- **根因分析：**
  - nginx `/dev-api/` location缺少proxy_read_timeout等超时配置（默认60s），视频上传到OSS耗时超过60s导致504
  - nginx error_page 50x指向的50x.html文件不存在，504回退为默认404页面
  - 前端Axios文件上传超时仅300秒，195MB视频上传未完成客户端断开，后端收到ClientAbortException: EOFException
  - AliyunFileService硬编码路径`speakers/avatars/`，视频文件也存到avatars目录

- **修复内容：**
  - nginx `/dev-api/`添加超时配置：proxy_connect_timeout/send_timeout/read_timeout 3600s, proxy_buffering off, proxy_request_buffering off
  - 创建50x.html错误页：`/home/meeting-deploy/nginx-portal/html/50x.html`
  - 前端文件上传超时从300秒增加到3600秒（1小时），与nginx/backend超时对齐
  - AliyunFileService新增resolvePathPrefix()：video/*→`speakers/videos/`，其他→`speakers/avatars/`
  - AliyunFileService新增uploadLargeFile()：>10MB文件使用OSS分片上传（5MB分片，3并发，支持断点续传）

- **涉及文件：**
  - `frontend/src/api/file.ts` — upload/uploadFile超时300s→3600s
  - `backend/src/main/java/com/youlai/boot/module/file/service/impl/AliyunFileService.java` — 视频路径+分片上传
  - 服务器 `/home/meeting-deploy/nginx-portal/conf/default.conf` — /dev-api/超时配置
  - 服务器 `/home/meeting-deploy/nginx-portal/html/50x.html` — 错误页

- **影响范围：**
  - 所有文件上传接口（/api/v1/files）
  - nginx代理的所有/dev-api/请求

- **验证方式：**
  - nginx -t配置测试通过，nginx -s reload重载成功
  - 后端mvn compile编译通过，mvn package打包通过
  - 前端pnpm build编译通过
  - 生产环境端到端上传195MB视频测试

- **验证结果：**
  - nginx配置：成功
  - 后端编译：成功
  - 前端编译：成功
  - 生产端到端：待验证

---

## 2026-08-03

### [Feature] 邮件退信检测与投递状态追踪全链路改造

- **目的：**
  - 实现完整邮件生命周期追踪：SMTP成功≠投递成功，需独立delivery_status字段+独立mail_bounce_record退信表+增量IMAP扫描+幂等处理+永久失败禁止重发+前端退信详情展示

- **核心设计：**
  - 双状态设计：send_status（SMTP阶段）+ delivery_status（投递阶段：UNKNOWN/DELIVERED/BOUNCED/DELIVERING）
  - 退信处理7步流程：幂等检查→5级匹配→写mail_bounce_record→更新双状态→永久退信加黑名单→告警→临时退信自动重发+parentRecordId追踪
  - 5级匹配策略：MESSAGE_ID→IN_REPLY_TO→REFERENCES→RECEIVER_TIME_WINDOW→UNMATCHED
  - BounceParser通用解析器：15+正则模式，DSN状态码>SMTP响应码>DNS关键词>正文关键词四级分类
  - 增量扫描：IMAP UID + mail_imap_checkpoint表记录last_uid+uid_validity+UIDVALIDITY变化自动重置
  - 幂等保证：mail_bounce_record的bounce_message_id唯一索引+saveIfAbsent双重检查
  - 分布式锁：RedissonClient实现mail:bounce-check锁

- **后端涉及文件：**
  - `backend/sql/mysql/mail_bounce_record.sql` — 退信记录表DDL
  - `backend/sql/mysql/mail_imap_checkpoint.sql` — IMAP检查点表DDL
  - `backend/sql/mysql/mail_send_record_delivery_increment.sql` — mail_send_record新增delivery_status等字段DDL
  - `backend/src/main/java/com/youlai/boot/support/mail/enums/BounceType.java` — 退信类型枚举(7种)
  - `backend/src/main/java/com/youlai/boot/support/mail/enums/DeliveryStatus.java` — 投递状态枚举(4种)
  - `backend/src/main/java/com/youlai/boot/support/mail/model/entity/MailBounceRecord.java` — 退信记录实体
  - `backend/src/main/java/com/youlai/boot/support/mail/model/entity/MailImapCheckpoint.java` — IMAP检查点实体
  - `backend/src/main/java/com/youlai/boot/support/mail/model/query/MailBounceRecordQuery.java` — 退信记录查询条件
  - `backend/src/main/java/com/youlai/boot/support/mail/model/query/MailSendRecordQuery.java` — 发送记录查询条件(新增deliveryStatus)
  - `backend/src/main/java/com/youlai/boot/support/mail/model/vo/MailSendRecordPageVO.java` — 分页VO(新增deliveryStatus/bounceCode/bounceType等)
  - `backend/src/main/java/com/youlai/boot/support/mail/model/vo/MailSendRecordDetailVO.java` — 详情VO(新增deliveryStatus/bounceMessageId/imapCheckCount等)
  - `backend/src/main/java/com/youlai/boot/support/mail/model/vo/BounceParseResult.java` — 退信解析结果VO
  - `backend/src/main/java/com/youlai/boot/support/mail/mapper/MailBounceRecordMapper.java` — 退信记录Mapper
  - `backend/src/main/java/com/youlai/boot/support/mail/mapper/MailImapCheckpointMapper.java` — 检查点Mapper
  - `backend/src/main/java/com/youlai/boot/support/mail/service/MailBounceRecordService.java` — 退信记录Service接口
  - `backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailBounceRecordServiceImpl.java` — 退信记录Service实现(含分页查询8条件)
  - `backend/src/main/java/com/youlai/boot/support/mail/service/MailImapCheckpointService.java` — 检查点Service接口
  - `backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailImapCheckpointServiceImpl.java` — 检查点Service实现
  - `backend/src/main/java/com/youlai/boot/support/mail/parser/BounceParser.java` — 通用退信解析器(静态工具类)
  - `backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java` — 核心改造(handleBounceEnhanced 7步流程+updateDeliveryStatus)
  - `backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java` — 退信检测Job(增量扫描+分布式锁+BounceParser+handleBounceEnhanced)
  - `backend/src/main/java/com/youlai/boot/support/mail/controller/MailBounceRecordController.java` — 退信记录管理接口(4个端点)
  - `backend/src/main/java/com/youlai/boot/support/mail/controller/MailSendRecordController.java` — 发送记录接口(新增markBounce端点)

- **前端涉及文件：**
  - `frontend/src/api/portal/mailBounceRecord.ts` — 退信记录API(新建)
  - `frontend/src/api/portal/mailSendRecord.ts` — 发送记录API(新增deliveryStatus等字段)
  - `frontend/src/views/mailRecord/index.vue` — 邮件中心页面(投递状态列+投递状态筛选+退信详情弹窗+详情弹窗投递信息)

- **影响范围：**
  - 邮件发送记录表新增delivery_status/bounce_code/bounce_type/bounce_permanent/bounce_message_id/imap_check_count/last_imap_check_time/parent_record_id字段
  - 新增mail_bounce_record退信记录表
  - 新增mail_imap_checkpoint IMAP检查点表
  - BounceEmailJob退信检测从旧handleBounce升级为handleBounceEnhanced+5级匹配
  - 前端邮件中心页面新增投递状态列和退信详情功能

- **验证方式：**
  - 按以下顺序执行3个SQL迁移脚本：
    1. `backend/sql/mysql/mail_send_record_delivery_increment.sql` — mail_send_record表新增delivery_status等9个字段+3个索引+存量数据迁移（send_status=5→delivery_status=2，send_status=1→delivery_status=0）
    2. `backend/sql/mysql/mail_bounce_record.sql` — 创建mail_bounce_record退信记录表（含bounce_message_id唯一索引）
    3. `backend/sql/mysql/mail_imap_checkpoint.sql` — 创建mail_imap_checkpoint IMAP检查点表
  - 启动后端，发送测试邮件，触发退信检测Job
  - 前端邮件中心查看投递状态列、退信详情弹窗

- **验证结果：**
  - 待验证

### [Fix] 黑名单列表分页接口及前端数据展示修复

- **目的：**
  - 修复黑名单测试数据前端不显示的问题

- **问题原因：**
  - 后端`/list`接口返回`Result<List>`无分页，忽略前端传的pageNum/pageSize参数
  - 前端用`res.data?.records`取数据，但项目PageResult格式是`data.list`，且request.ts已解包response.data

- **涉及文件：**
  - `backend/src/main/java/com/youlai/boot/support/mail/service/MailBounceBlacklistService.java`
  - `backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailBounceBlacklistServiceImpl.java`
  - `backend/src/main/java/com/youlai/boot/support/mail/controller/MailBounceBlacklistController.java`
  - `frontend/src/views/mailRecord/blacklist.vue`

- **修改逻辑：**
  - Service接口添加`Page<MailBounceBlacklist> listPage(Integer pageNum, Integer pageSize)`分页方法
  - ServiceImpl实现分页查询，按createTime降序排列
  - Controller的`/list`改为接受pageNum/pageSize参数，返回`PageResult<MailBounceBlacklist>`
  - 前端blacklist.vue：`res.data?.records`→`res.list`，`res.data?.total`→`res.total`

- **影响范围：**
  - 黑名单管理页面列表展示和分页功能

- **验证方式：**
  - 启动后端，访问前端黑名单管理页面，确认测试数据正常显示且分页可用

- **验证结果：**
  - 待验证

### [Feature] 邮件退信黑名单测试数据

- **目的：**
  - 为黑名单管理页面提供测试数据，验证列表展示、分页、手动添加、移除等功能

- **涉及文件：**
  - `backend/sql/mysql/mail_bounce_blacklist.sql`

- **修改逻辑：**
  - 在 mail_bounce_blacklist.sql 末尾追加5条测试数据INSERT语句
  - 包含AUTO自动检测来源3条（163邮箱User not found、QQ邮箱recipient rejected、sina邮箱host not found）
  - 包含AUTO自动检测1条（hotmail邮箱delivery error）
  - 包含MANUAL手动添加1条（126邮箱手动加入黑名单）
  - bounce_count、last_bounce_time等字段覆盖不同场景

- **影响范围：**
  - 黑名单管理页面数据展示

- **验证方式：**
  - 执行SQL后在前端黑名单管理页面查看数据

- **验证结果：**
  - 未执行

- **备注：**
  - 测试数据仅用于开发/测试环境，生产环境请勿执行

---

## 2026-07-31

### [Feature] 投稿状态查询功能（Portal端）

- **目的：**
  - Portal 论文投稿页面（FirstDraft.vue）新增"投稿状态查询"模块
  - 查询流程：输入邮箱 → 发送验证码 → 输入验证码 → 查询投稿列表 → 查看详情/修改投稿

- **后端新增4个公开接口（无需登录）：**
  - `POST /api/v1/paperSubmission/query/sendCode` — 发送查询验证码（校验邮箱有投稿记录后才发送）
  - `GET /api/v1/paperSubmission/query/byEmail` — 按邮箱+验证码查询投稿列表
  - `GET /api/v1/paperSubmission/query/detail` — 查询投稿详情（需验证邮箱归属）
  - `PUT /api/v1/paperSubmission/query/update` — 投稿者修改投稿信息（仅待审核状态 paperStatus=1 可修改）

- **验证码机制：** Redis key `captcha:paper_query:{email}`，6位随机码，5分钟有效，一次性使用
- **允许修改的字段：** paperTitle、firstAuthor、contactNumber、paperAbstract、keywords、appendix
- **不可修改的字段：** name、surname、contactEmail（作为身份标识）
- **前端新增 `submit.js` API 接口文件**（sendQueryCode、queryPapersByEmail、getPaperDetail、updatePaperBySubmitter）
- **国际化文案同步更新**（zh.js / en.js 的 firstDraft.query/queryTable/detail/edit/status 部分）

- **涉及文件：**
  - `backend/src/main/java/com/youlai/boot/portal/controller/PaperSubmissionController.java` — 新增4个查询接口
  - `backend/src/main/java/com/youlai/boot/portal/service/impl/PaperSubmissionServiceImpl.java` — 查询验证码+查询列表+详情+修改逻辑
  - `webpage/src/api/submit.js` — 新增4个API接口
  - `webpage/src/pages/FirstDraft.vue` — 新增投稿状态查询模块
  - `webpage/src/locales/zh.js` — 中文文案
  - `webpage/src/locales/en.js` — 英文文案

### [Fix] 邮箱大小写不敏感匹配（Bug修复）

- **目的：**
  - 修复 `sendQueryCode` 和 `queryByEmail` 中邮箱查询使用 `.eq()` 精确匹配，导致数据库存储大写邮箱但用户输入小写时查询不到的问题

- **修改逻辑：**
  - 改为 `.apply("LOWER(contact_email) = {0}", emailTrimmed)` 实现数据库层不区分大小写匹配
  - `getDetailByEmail` 和 `updateBySubmitter` 使用 Java 端 `.equals()` 比较，已做 `toLowerCase()` 处理，无需修改

- **涉及文件：**
  - `backend/src/main/java/com/youlai/boot/portal/service/impl/PaperSubmissionServiceImpl.java`

### [Fix] 前端响应拦截器优化（Bug修复）

- **目的：**
  - 修复 `webpage/src/utils/request.js` 响应拦截器未判断业务 code，导致后端返回 BusinessException 时前端仍提示"成功"

- **修改逻辑：**
  - 新增 `code !== '00000'` 时 `Promise.reject(new Error(res.msg))`，让调用方 catch 正确处理业务异常
  - 修复输入错误邮箱发送验证码时提示"邮件发送成功"的问题，现在正确提示"未找到您的投稿记录，请检查邮箱是否输入正确"

- **涉及文件：**
  - `webpage/src/utils/request.js`

### [Enhancement] 错误提示优化

- **目的：**
  - `sendQueryCode` 邮箱无投稿记录时提示从"该邮箱暂无投稿记录"改为"未找到您的投稿记录，请检查邮箱是否输入正确"

- **涉及文件：**
  - `backend/src/main/java/com/youlai/boot/portal/service/impl/PaperSubmissionServiceImpl.java`

### [Feature] 退信自动回收（IMAP定时任务）

- **目的：**
  - 新增 `BounceEmailJob` 定时任务，通过 IMAP 协议连接发件邮箱收件箱，扫描未读退信邮件

- **核心功能：**
  - 退信识别策略：发送者模式匹配 + 主题关键词匹配 + Content-Type multipart/report 检测
  - Message-ID 提取策略：In-Reply-To → References → 正文正则匹配（三级降级）
  - 退信处理：更新记录状态为"退信-投递失败(5)" → 发送管理员告警邮件 → 自动重发（新建记录策略，保留原退信记录）
  - 新增 `ImapProperties` 配置类，支持 IMAP 连接参数、cron 表达式、自动重发开关等外部化配置
  - 新增 `mail_send_record_bounce_increment.sql` 增量脚本（bounce_reason 字段 + idx_message_id 索引）

- **涉及文件：**
  - `backend/src/main/java/com/youlai/boot/support/mail/job/BounceEmailJob.java`
  - `backend/src/main/java/com/youlai/boot/support/mail/config/ImapProperties.java`
  - `backend/sql/mysql/mail_send_record_bounce_increment.sql`

### [Enhancement] 邮件发送状态文案修改

- **目的：**
  - 状态1从"发送成功"改为"已提交至邮件服务器"，避免误导排查（SMTP 250 OK 仅表示服务器已接收）
  - 新增状态5"退信-投递失败"（BOUNCE_DETECTED），前后端状态同步

- **涉及文件：**
  - `backend/src/main/java/com/youlai/boot/support/mail/enums/MailSendStatus.java`
  - 后端 Mapper XML
  - 前端状态映射、搜索下拉

### [Feature] 邮箱双输入验证

- **目的：**
  - Portal 投稿页面（Submit.vue）：作者邮箱增加"确认邮箱"输入框，两次输入必须一致
  - Admin 论文投稿管理（contributionIndex）：专家邮箱增加"确认邮箱"输入框，两次输入必须一致
  - 国际化文案同步更新（zh.js / en.js）

- **涉及文件：**
  - `webpage/src/pages/Submit.vue`
  - `frontend/src/views/contribution/contributionIndex/index.vue`
  - `webpage/src/locales/zh.js`
  - `webpage/src/locales/en.js`

### [Feature] 邮件发送记录模块（邮件中心）

- **目的：**
  - 后端新增 `MailSendRecordController` 接口（分页查询、详情、重试、取消、按业务查询）
  - 后端新增 `MailSendRecordService` / `MailSendRecordServiceImpl`（任务表模式：先落库再发送）
  - 后端新增 `MailSendStatus` 枚举（6种状态：待发送/已提交至邮件服务器/发送失败/重试中/已取消/退信-投递失败）
  - 后端新增 `MailBusinessType` 枚举（论文审稿通知/论文审核通过/论文审核拒绝/注册成功通知等）
  - 前端新增邮件中心页面（mailRecord/index.vue）：邮件列表、状态筛选、详情弹窗、重试/取消操作
  - 前端新增 `mailSendRecord.ts` API 接口文件

- **涉及文件：**
  - `backend/src/main/java/com/youlai/boot/support/mail/controller/MailSendRecordController.java`
  - `backend/src/main/java/com/youlai/boot/support/mail/service/MailSendRecordService.java`
  - `backend/src/main/java/com/youlai/boot/support/mail/service/impl/MailSendRecordServiceImpl.java`
  - `backend/src/main/java/com/youlai/boot/support/mail/enums/MailSendStatus.java`
  - `backend/src/main/java/com/youlai/boot/support/mail/enums/MailBusinessType.java`
  - `frontend/src/views/mailRecord/index.vue`
  - `frontend/src/api/portal/mailSendRecord.ts`

### [Fix] 修复 MailSendRecordPageVO 缺少 bounceReason 字段

- **目的：**
  - PageVO 新增 bounceReason 字段，Mapper XML 分页查询新增 bounce_reason 列
  - 修复前端列表页退信原因 tooltip 无法显示的问题

- **涉及文件：**
  - `backend/src/main/java/com/youlai/boot/support/mail/model/vo/MailSendRecordPageVO.java`
  - 后端 Mapper XML

---

## 2026-07-23

### [Fix] 线上PDF导出问题修复

- **目的：**
  - 修复DOCX转PDF失败：`Cannot run program "soffice": error=2`，soffice找不到
  - 修复OpenPDF中文字体加载失败：`未找到任何中文字体！PDF中中文将无法显示`

- **修改逻辑：**
  - Dockerfile移除`--no-install-recommends`，确保LibreOffice安装完整推荐依赖
  - Dockerfile添加fontconfig包和`fc-cache -f`刷新字体缓存
  - Dockerfile添加构建时验证步骤（soffice版本、中文字体列表、字体文件路径）
  - DocxToPdfServiceImpl增加`/usr/bin/soffice`回退路径和详细日志
  - PaperPdfGeneratorServiceImpl字体路径从4个扩展到12个，覆盖Noto CJK所有可能的.ttc/.otf格式

- **涉及文件：**
  - `backend/Dockerfile`
  - `backend/src/main/java/com/youlai/boot/portal/service/impl/DocxToPdfServiceImpl.java`
  - `backend/src/main/java/com/youlai/boot/portal/service/impl/PaperPdfGeneratorServiceImpl.java`

### [Fix] 审稿邮件功能修复

- **目的：**
  - 新增重发审稿邮件功能：管理端审核状态弹窗添加"重发邮件"按钮，支持对发送失败或未收到的邮件重新发送
  - 后端新增 `POST /api/v1/paperSubmission/resendEmail/{emailId}` 接口，复用已有邮件记录重发
  - 修复邮件被删除问题：`expertReview`接口添加已审核校验，防止重复审核导致状态被覆盖
  - ReviewFile页面加载时查询审核状态，已审核则显示结果而非操作按钮，防止QQ邮箱链接预览触发审核操作

- **涉及文件：**
  - `backend/src/main/java/com/youlai/boot/portal/controller/PaperSubmissionController.java`
  - `backend/src/main/java/com/youlai/boot/portal/service/impl/PaperSubmissionServiceImpl.java`
  - `frontend/src/views/contribution/contributionIndex/index.vue`
  - `webpage/src/pages/ReviewFile.vue`

### [Enhancement] 投稿页面全英文输入

- **目的：**
  - 论文投稿页面（FirstDraft.vue）所有文本字段禁止输入中文字符（论文标题、姓名、第一作者、摘要、关键词）
  - 联系电话和联系邮箱不过滤（天然英文/数字格式）
  - 实现方式：watch + nextTick 监听字段值变化，检测到中文自动清除并弹出提示
  - 同步修改 Submit.vue 和 Sidebar.vue 中的投稿表单

- **涉及文件：**
  - `webpage/src/pages/FirstDraft.vue`
  - `webpage/src/pages/Submit.vue`
  - `webpage/src/components/common/Sidebar.vue`

### [Enhancement] 论文附件上传限制

- **目的：**
  - 禁止上传 .doc 格式文件，仅允许 .pdf 和 .docx（后端未实现 doc 转 pdf）
  - 更新前端校验和国际化提示文案

- **涉及文件：**
  - `webpage/src/pages/Submit.vue`
  - `webpage/src/locales/zh.js`
  - `webpage/src/locales/en.js`

### [Enhancement] 论文附件必填星号去掉

- **目的：**
  - Submit.vue 论文附件 el-form-item 添加 :required="false"
  - Sidebar.vue 论文附件 el-form-item 移除 required 属性

- **涉及文件：**
  - `webpage/src/pages/Submit.vue`
  - `webpage/src/components/common/Sidebar.vue`

### [Fix] 页面刷新不再跳转首页

- **目的：**
  - 移除 App.vue 中 performance.getEntriesByType('reload') 检测刷新后跳转首页的逻辑
  - 刷新后保持在当前路由页面（如 /first-draft 刷新后仍在 /first-draft）

- **涉及文件：**
  - `webpage/src/App.vue`

### [Fix] 审稿邮件三个问题修复

- **目的：**
  - 修复重发邮件后审核状态不更新：`resendReviewEmail`方法添加`paper_submission.emailStatus`同步更新逻辑
  - 修复每次审核都新增邮件记录：`updatePaperSubmissionStatus`方法添加`existingCount>0`校验，已发送过则拒绝重复发送
  - 修复邮件链接502和QQ邮箱预览触发审核：`expertReview`接口从PUT改为POST，防止链接预览自动触发GET请求
  - ReviewFile页面`onMounted`时查询`emailRecord`判断已审核状态，已审核显示结果标签而非操作按钮
  - 管理端审核状态弹窗添加发送状态列和重发邮件按钮，重发后自动刷新列表

- **涉及文件：**
  - `backend/src/main/java/com/youlai/boot/portal/service/impl/PaperSubmissionServiceImpl.java`
  - `backend/src/main/java/com/youlai/boot/portal/controller/PaperSubmissionController.java`
  - `frontend/src/views/contribution/contributionIndex/index.vue`
  - `webpage/src/pages/ReviewFile.vue`

---

## 2026-07-22

### [Enhancement] PDF导出功能增强

- **目的：**
  - 封面页改为4行2列表格排版（论文标题、姓名、联系电话、论文摘要）
  - 修复OpenPDF中文字体渲染问题：优先使用字体文件路径加载（simsun.ttc/msyh.ttc），回退到字体名称
  - 新增一键导出功能：选中多条后批量导出PDF，导出完成自动取消选中
  - 新增防重复点击：按记录ID隔离3秒冷却期，不同记录互不影响
  - 新增批量导出限流：不同记录间隔0.5秒防止服务端过载
  - DOCX转PDF从JODConverter进程池改为ProcessBuilder命令行调用，解决Windows超时和僵尸进程问题

- **涉及文件：**
  - `backend/src/main/java/com/youlai/boot/portal/service/impl/PaperPdfGeneratorServiceImpl.java`
  - `backend/src/main/java/com/youlai/boot/portal/service/impl/DocxToPdfServiceImpl.java`
  - `frontend/src/views/contribution/contributionIndex/index.vue`

---

## 2026-07-21

### [Feature] PDF导出功能上线

- **目的：**
  - 管理端论文投稿页面新增"导出PDF"功能
  - 支持PDF附件直接导出、DOCX附件自动转PDF、无附件生成信息页
  - 有附件时封面页+附件内容合并为一个PDF
  - 前端使用axios blob方式下载，文件名格式为`姓名.pdf`
  - 后端使用JODConverter+LibreOffice转DOCX为PDF，OpenPDF生成信息页
  - 论文摘要字段从VARCHAR改为TEXT，支持超长摘要

- **涉及文件：**
  - `backend/src/main/java/com/youlai/boot/portal/controller/PaperSubmissionController.java`
  - `backend/src/main/java/com/youlai/boot/portal/service/impl/PaperPdfGeneratorServiceImpl.java`
  - `backend/src/main/java/com/youlai/boot/portal/service/impl/DocxToPdfServiceImpl.java`
  - `backend/sql/mysql/paper_submission_alter_columns.sql`
  - `frontend/src/views/contribution/contributionIndex/index.vue`
  - `frontend/src/api/portal/paperSubmission.ts`