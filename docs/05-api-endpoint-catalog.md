# MCLC API端点目录

> 基于代码扫描生成，共40个Controller、222个API端点
> 所有端点前缀：`/api/v1`
> 认证方式：JWT Bearer Token（部分公开接口除外）

---

## 1. 认证中心（Auth）

### 1.1 AuthController — `/api/v1/auth`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/captcha` | 获取验证码 | 公开 |
| 2 | POST | `/login` | 账号密码登录 | 公开 |
| 3 | POST | `/login/sms` | 短信验证码登录 | 公开 |
| 4 | POST | `/sms/code` | 发送登录短信验证码 | 公开 |
| 5 | DELETE | `/logout` | 退出登录 | ✔ |
| 6 | POST | `/refresh-token` | 刷新令牌 | ✔ |

### 1.2 WechatMiniappAuthController — `/api/v1/wechat/miniapp/auth`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | POST | `/silent-login` | 静默登录（微信code，未绑定返回openid） | 公开 |
| 2 | POST | `/phone-login` | 手机号快捷登录（企业认证小程序） | 公开 |
| 3 | POST | `/bind-mobile` | 绑定手机号（静默登录后绑定） | ✔ |

---

## 2. 系统管理（System）

### 2.1 UserController — `/api/v1/users`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/` | 用户分页列表 | `sys:user:list` |
| 2 | POST | `/` | 新增用户 | `sys:user:add` |
| 3 | GET | `/{userId}/form` | 获取用户表单数据 | `sys:user:list` |
| 4 | PUT | `/{userId}` | 修改用户 | `sys:user:update` |
| 5 | DELETE | `/{ids}` | 删除用户（批量） | `sys:user:delete` |
| 6 | PATCH | `/{userId}/status` | 修改用户状态 | `sys:user:update` |
| 7 | PUT | `/{userId}/password/reset` | 重置用户密码 | `sys:user:resetPwd` |
| 8 | GET | `/me` | 获取当前登录用户信息 | ✔ |
| 9 | GET | `/template` | 下载用户导入模板 | `sys:user:import` |
| 10 | POST | `/import` | 导入用户 | `sys:user:import` |
| 11 | GET | `/export` | 导出用户 | `sys:user:export` |
| 12 | GET | `/options` | 用户下拉选项 | ✔ |
| 13 | GET | `/profile` | 获取个人中心信息 | ✔ |
| 14 | PUT | `/profile` | 修改个人中心信息 | ✔ |
| 15 | PUT | `/password` | 修改个人密码 | ✔ |
| 16 | POST | `/mobile/code` | 发送手机验证码 | ✔ |
| 17 | PUT | `/mobile` | 绑定手机号 | ✔ |
| 18 | DELETE | `/mobile` | 解绑手机号 | ✔ |
| 19 | POST | `/email/code` | 发送邮箱验证码 | ✔ |
| 20 | PUT | `/email` | 绑定邮箱 | ✔ |
| 21 | DELETE | `/email` | 解绑邮箱 | ✔ |
| 22 | POST | `/registerUser` | 门户注册用户 | 公开 |

### 2.2 RoleController — `/api/v1/roles`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/` | 角色分页列表 | `sys:role:list` |
| 2 | GET | `/options` | 角色下拉选项 | ✔ |
| 3 | POST | `/` | 新增角色 | `sys:role:add` |
| 4 | GET | `/{roleId}/form` | 获取角色表单数据 | `sys:role:list` |
| 5 | PUT | `/{id}` | 修改角色 | `sys:role:update` |
| 6 | DELETE | `/{ids}` | 删除角色（批量） | `sys:role:delete` |
| 7 | PUT | `/{roleId}/status` | 修改角色状态 | `sys:role:update` |
| 8 | GET | `/{roleId}/menu-ids` | 获取角色菜单ID列表 | `sys:role:list` |
| 9 | PUT | `/{roleId}/menus` | 分配角色菜单 | `sys:role:update` |
| 10 | GET | `/{roleId}/dept-ids` | 获取角色部门ID列表 | `sys:role:list` |

### 2.3 MenuController — `/api/v1/menus`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/` | 菜单列表 | `sys:menu:list` |
| 2 | GET | `/options` | 菜单下拉选项 | ✔ |
| 3 | GET | `/routes` | 获取当前用户路由菜单 | ✔ |
| 4 | GET | `/{id}/form` | 获取菜单表单数据 | `sys:menu:list` |
| 5 | POST | `/` | 新增菜单 | `sys:menu:add` |
| 6 | PUT | `/{id}` | 修改菜单 | `sys:menu:update` |
| 7 | DELETE | `/{id}` | 删除菜单 | `sys:menu:delete` |
| 8 | PATCH | `/{menuId}` | 修改菜单局部字段 | `sys:menu:update` |

### 2.4 DeptController — `/api/v1/depts`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/` | 部门列表 | `sys:dept:list` |
| 2 | GET | `/options` | 部门下拉选项 | ✔ |
| 3 | POST | `/` | 新增部门 | `sys:dept:add` |
| 4 | GET | `/{deptId}/form` | 获取部门表单数据 | `sys:dept:list` |
| 5 | PUT | `/{deptId}` | 修改部门 | `sys:dept:update` |
| 6 | DELETE | `/{ids}` | 删除部门（批量） | `sys:dept:delete` |

### 2.5 DictController — `/api/v1/dicts`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/` | 字典分页列表 | `sys:dict:list` |
| 2 | GET | `/options` | 字典下拉选项 | ✔ |
| 3 | GET | `/{id}/form` | 获取字典表单数据 | `sys:dict:list` |
| 4 | POST | `/` | 新增字典 | `sys:dict:add` |
| 5 | PUT | `/{id}` | 修改字典 | `sys:dict:update` |
| 6 | DELETE | `/{ids}` | 删除字典（批量） | `sys:dict:delete` |
| 7 | GET | `/{dictCode}/items` | 获取字典项列表 | ✔ |
| 8 | GET | `/{dictCode}/items/options` | 获取字典项下拉选项 | ✔ |
| 9 | POST | `/{dictCode}/items` | 新增字典项 | `sys:dict:add` |
| 10 | GET | `/{dictCode}/items/{itemId}/form` | 获取字典项表单数据 | `sys:dict:list` |
| 11 | PUT | `/{dictCode}/items/{itemId}` | 修改字典项 | `sys:dict:update` |
| 12 | DELETE | `/{dictCode}/items/{itemIds}` | 删除字典项（批量） | `sys:dict:delete` |

### 2.6 ConfigController — `/api/v1/configs`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/` | 系统配置分页列表 | `sys:config:list` |
| 2 | POST | `/` | 新增系统配置 | `sys:config:add` |
| 3 | GET | `/{id}/form` | 获取配置表单数据 | `sys:config:list` |
| 4 | PUT | `/refresh` | 刷新系统配置缓存 | `sys:config:update` |
| 5 | PUT | `/{id}` | 修改系统配置 | `sys:config:update` |
| 6 | DELETE | `/{id}` | 删除系统配置 | `sys:config:delete` |

### 2.7 NoticeController — `/api/v1/notices`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/` | 通知公告分页列表 | `sys:notice:list` |
| 2 | POST | `/` | 新增通知公告 | `sys:notice:add` |
| 3 | GET | `/{id}/form` | 获取通知表单数据 | `sys:notice:list` |
| 4 | GET | `/{id}/detail` | 获取通知详情 | `sys:notice:list` |
| 5 | PUT | `/{id}` | 修改通知公告 | `sys:notice:update` |
| 6 | PUT | `/{id}/publish` | 发布通知公告 | `sys:notice:update` |
| 7 | PUT | `/{id}/revoke` | 撤回通知公告 | `sys:notice:update` |
| 8 | DELETE | `/{ids}` | 删除通知公告（批量） | `sys:notice:delete` |
| 9 | PUT | `/read-all` | 全部标记已读 | ✔ |
| 10 | GET | `/my` | 我的未读通知 | ✔ |

### 2.8 LogController — `/api/v1/logs`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/` | 操作日志分页列表 | `sys:log:list` |

### 2.9 EmailTemplateController — `/api/v1/email-templates`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/` | 邮件模板分页列表 | `sys:emailTemplate:list` |
| 2 | GET | `/form` | 获取邮件模板表单数据 | `sys:emailTemplate:list` |
| 3 | POST | `/` | 新增邮件模板 | `sys:emailTemplate:add` |
| 4 | PUT | `/{id}` | 修改邮件模板 | `sys:emailTemplate:update` |
| 5 | DELETE | `/{ids}` | 删除邮件模板（批量） | `sys:emailTemplate:delete` |

### 2.10 RegistrationTypeController — `/api/v1/registrationType`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/` | 注册类型列表 | `sys:registrationType:list` |
| 2 | POST | `/` | 新增注册类型 | `sys:registrationType:add` |
| 3 | PUT | `/{id}` | 修改注册类型 | `sys:registrationType:update` |
| 4 | DELETE | `/{ids}` | 删除注册类型（批量） | `sys:registrationType:delete` |

### 2.11 StatisticsController — `/api/v1/statistics`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/visits/trend` | 访问趋势统计 | ✔ |
| 2 | GET | `/visits/overview` | 访问概览统计 | ✔ |

---

## 3. 门户业务（Portal）

### 3.1 PortalController — `/api/v1/portal`
> 门户Web端聚合接口，提供前台展示数据查询
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/getMeetingDateList` | 会议日程列表（已弃用） | 公开 |
| 2 | GET | `/getContactList` | 联系人列表 | 公开 |
| 3 | GET | `/getGuestList` | 特邀嘉宾列表 | 公开 |
| 4 | GET | `/getHotelList` | 酒店信息列表 | 公开 |
| 5 | GET | `/getQuestionList` | 问题解答列表 | 公开 |
| 6 | GET | `/getArrangeList` | 会前安排列表 | 公开 |
| 7 | GET | `/getMomentsList` | 精彩瞬间列表 | 公开 |
| 8 | GET | `/getSpeechList` | 演讲列表 | 公开 |
| 9 | GET | `/{id}/form` | 获取演讲详情 | 公开 |
| 10 | GET | `/getMeetingSettingList` | 会议设置列表 | 公开 |

### 3.2 PortalAuthController — `/api/v1/portal/auth`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | POST | `/email/code` | 发送邮箱验证码 | 公开 |
| 2 | POST | `/email/login` | 邮箱验证码登录 | 公开 |

### 3.3 RegisterUserController — `/api/v1/registerUser`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/` | 注册用户分页列表 | `sys:registerUser:list` |
| 2 | POST | `/` | 新增注册用户 | 公开(防重提交) |
| 3 | PUT | `/{id}` | 修改注册用户 | `sys:registerUser:update` |
| 4 | DELETE | `/{ids}` | 删除注册用户（批量） | `sys:registerUser:delete` |
| 5 | GET | `/{id}` | 注册用户详情 | `sys:registerUser:list` |
| 6 | POST | `/{id}/audit` | 审核注册用户 | `sys:registerUser:update` |
| 7 | POST | `/checkIn` | 签到 | ✔ |

### 3.4 PaymentController — `/api/v1/payment`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | POST | `/create-intent` | 创建支付意图（信用卡/微信/支付宝/银行转账） | ✔ |
| 2 | GET | `/status/{registrationId}` | 查询支付状态 | ✔ |
| 3 | GET | `/bank-info` | 获取银行转账信息 | ✔ |
| 4 | POST | `/confirm/{registrationId}` | 确认银行转账到账 | `sys:registerUser:update` |
| 5 | POST | `/refund/{registrationId}` | 退款 | `sys:registerUser:update` |
| 6 | GET | `/config` | 获取Stripe发布密钥 | 公开 |

### 3.5 PaperSubmissionController — `/api/v1/paperSubmission`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/` | 论文投稿分页列表 | `sys:paperSubmission:list` |
| 2 | POST | `/` | 新增论文投稿 | ✔ |
| 3 | PUT | `/{id}` | 修改论文投稿 | ✔ |
| 4 | DELETE | `/{ids}` | 删除论文投稿（批量） | ✔ |
| 5 | PUT | `/{id}/review` | 管理员审核论文 | ✔ |
| 6 | POST/PUT | `/expertReview` | 专家评审提交 | ✔ |
| 7 | GET | `/expertReview/list` | 专家评审列表 | ✔ |
| 8 | POST | `/resendEmail/{emailId}` | 重发论文邮件 | ✔ |
| 9 | GET | `/emailRecord/{emailId}` | 获取邮件发送记录 | ✔ |
| 10 | GET | `/{id}/exportPdf` | 导出论文PDF | ✔ |
| 11 | POST | `/query/sendCode` | 投稿查询-发送验证码 | 公开 |
| 12 | GET | `/query/byEmail` | 投稿查询-按邮箱查询 | 公开 |
| 13 | GET | `/query/detail` | 投稿查询-详情 | 公开 |
| 14 | PUT | `/query/update` | 投稿查询-更新 | 公开 |

### 3.6 MeetingScheduleController — `/api/v1/meetingSchedule`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/allMeetingSchedule` | 获取全部会议日程 | ✔ |
| 2 | GET | `/page` | 会议日程分页列表 | ✔ |
| 3 | POST | `/session` | 新增会议Session | ✔ |
| 4 | PUT | `/session/{id}` | 修改会议Session | ✔ |
| 5 | DELETE | `/session/{id}` | 删除会议Session | ✔ |
| 6 | PUT | `/{meetingId}/status` | 修改会议状态 | ✔ |
| 7 | PUT | `/meeting/{meetingId}` | 修改会议信息 | ✔ |
| 8 | DELETE | `/{meetingId}` | 删除会议 | ✔ |

### 3.7 MeetingSettingController — `/api/v1/meetingSetting`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/` | 会议设置分页列表 | ✔ |
| 2 | POST | `/` | 新增会议设置 | ✔ |
| 3 | PUT | `/{id}` | 修改会议设置 | ✔ |
| 4 | DELETE | `/{ids}` | 删除会议设置（批量） | ✔ |
| 5 | PUT | `/{meetingSettingId}/status` | 修改会议设置状态 | ✔ |

### 3.8 GuestController — `/api/v1/guest`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/` | 特邀嘉宾分页列表 | ✔ |
| 2 | POST | `/` | 新增特邀嘉宾 | ✔ |
| 3 | PUT | `/{id}` | 修改特邀嘉宾 | ✔ |
| 4 | DELETE | `/{ids}` | 删除特邀嘉宾（批量） | ✔ |
| 5 | PUT | `/{guestId}/status` | 修改嘉宾状态 | ✔ |

### 3.9 HotelController — `/api/v1/hotel`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/` | 酒店信息分页列表 | ✔ |
| 2 | POST | `/` | 新增酒店信息 | ✔ |
| 3 | PUT | `/{id}` | 修改酒店信息 | ✔ |
| 4 | DELETE | `/{ids}` | 删除酒店信息（批量） | ✔ |
| 5 | PUT | `/{hotelId}/status` | 修改酒店状态 | ✔ |

### 3.10 ContactController — `/api/v1/contact`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/` | 联系人分页列表 | ✔ |
| 2 | POST | `/` | 新增联系人 | ✔ |
| 3 | PUT | `/{id}` | 修改联系人 | ✔ |
| 4 | DELETE | `/{ids}` | 删除联系人（批量） | ✔ |
| 5 | PUT | `/{contactId}/status` | 修改联系人状态 | ✔ |

### 3.11 ArchitectureController — `/api/v1/architecture`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/` | 会议架构分页列表 | ✔ |
| 2 | POST | `/` | 新增会议架构 | ✔ |
| 3 | PUT | `/{id}` | 修改会议架构 | ✔ |
| 4 | DELETE | `/{ids}` | 删除会议架构（批量） | ✔ |
| 5 | PUT | `/{architectureId}/status` | 修改架构状态 | ✔ |

### 3.12 ArrangeController — `/api/v1/arrange`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/` | 会前安排分页列表 | ✔ |
| 2 | POST | `/` | 新增会前安排 | ✔ |
| 3 | PUT | `/{id}` | 修改会前安排 | ✔ |
| 4 | DELETE | `/{ids}` | 删除会前安排（批量） | ✔ |
| 5 | PUT | `/{arrangeId}/status` | 修改安排状态 | ✔ |

### 3.13 MomentsController — `/api/v1/moments`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/` | 精彩瞬间分页列表 | ✔ |
| 2 | POST | `/` | 新增精彩瞬间 | ✔ |
| 3 | PUT | `/{id}` | 修改精彩瞬间 | ✔ |
| 4 | DELETE | `/{ids}` | 删除精彩瞬间（批量） | ✔ |
| 5 | PUT | `/{MomentsId}/status` | 修改瞬间状态 | ✔ |

### 3.14 SpeechController — `/api/v1/speech`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/` | 演讲集锦分页列表 | ✔ |
| 2 | GET | `/{id}/form` | 获取演讲详情 | ✔ |
| 3 | POST | `/` | 新增演讲集锦 | ✔ |
| 4 | PUT | `/{id}` | 修改演讲集锦 | ✔ |
| 5 | DELETE | `/{ids}` | 删除演讲集锦（批量） | ✔ |
| 6 | PUT | `/{SpeechId}/status` | 修改演讲状态 | ✔ |

### 3.15 QuestionController — `/api/v1/question`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/` | 问题解答分页列表 | ✔ |
| 2 | POST | `/` | 新增问题解答 | ✔ |
| 3 | PUT | `/{id}` | 修改问题解答 | ✔ |
| 4 | DELETE | `/{ids}` | 删除问题解答（批量） | ✔ |
| 5 | PUT | `/{hotelId}/status` | 修改问题状态 | ✔ |

### 3.16 DiscountConfigurationController — `/api/v1/discountConfiguration`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/` | 折扣配置分页列表 | ✔ |
| 2 | POST | `/` | 新增折扣配置 | ✔ |

### 3.17 MeetDateController — `/api/v1/meetDate`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/` | 会议日期列表 | ✔ |
| 2 | GET | `/{id}` | 获取会议日期详情 | ✔ |
| 3 | POST | `/` | 新增会议日期 | ✔ |
| 4 | PUT | `/{id}` | 修改会议日期 | ✔ |
| 5 | DELETE | `/{ids}` | 删除会议日期（批量） | ✔ |

### 3.18 UserCheckInController — `/api/v1/userCheckIn`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/` | 签到记录分页列表 | ✔ |
| 2 | DELETE | `/{ids}` | 删除签到记录（批量） | ✔ |

### 3.19 StripeWebhookController — `/api/v1/stripe`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | POST | `/webhook` | Stripe Webhook回调 | 公开(签名验证) |

### 3.20 AlipayCallbackController — `/api/v1/payment/alipay`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | POST | `/notify` | 支付宝异步通知回调 | 公开(签名验证) |

### 3.21 WechatCallbackController — `/api/v1/payment/wechat`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | POST | `/notify` | 微信支付异步通知回调 | 公开(签名验证) |

---

## 4. 邮件子系统（Mail）

### 4.1 MailSendRecordController — `/api/v1/mailRecord`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/page` | 邮件发送记录分页列表 | ✔ |
| 2 | GET | `/{id}` | 获取邮件发送记录详情 | ✔ |
| 3 | POST | `/retry/{id}` | 重试发送邮件 | ✔ |
| 4 | POST | `/cancel/{id}` | 取消发送邮件 | ✔ |
| 5 | GET | `/business` | 按业务查询邮件列表 | ✔ |
| 6 | POST | `/markBounce/{id}` | 标记邮件为退信 | ✔ |

### 4.2 MailBounceRecordController — `/api/v1/mailBounceRecord`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/page` | 退信记录分页列表 | ✔ |
| 2 | GET | `/{id}` | 获取退信记录详情 | ✔ |
| 3 | GET | `/bySendRecord/{matchedRecordId}` | 按发送记录查退信 | ✔ |
| 4 | GET | `/unprocessed` | 获取未处理退信记录 | ✔ |

### 4.3 MailBounceBlacklistController — `/api/v1/mailBlacklist`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/list` | 黑名单列表 | ✔ |
| 2 | GET | `/check` | 检查邮箱是否在黑名单 | ✔ |
| 3 | POST | `/add` | 添加到黑名单 | ✔ |
| 4 | POST | `/remove/{id}` | 从黑名单移除 | ✔ |

---

## 5. 通用模块（Module）

### 5.1 FileController — `/api/v1/files`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | POST | `/` | 上传文件 | ✔ |
| 2 | DELETE | `/` | 删除文件 | ✔ |

### 5.2 CodegenController — `/api/v1/codegen`
| # | Method | Path | Summary | Auth |
|---|--------|------|---------|------|
| 1 | GET | `/table` | 获取代码生成表列表 | `sys:codegen:list` |
| 2 | GET | `/{tableName}/config` | 获取表配置信息 | `sys:codegen:list` |
| 3 | POST | `/{tableName}/config` | 保存表配置信息 | `sys:codegen:add` |
| 4 | DELETE | `/{tableName}/config` | 删除表配置信息 | `sys:codegen:delete` |
| 5 | GET | `/{tableName}/preview` | 预览生成代码 | `sys:codegen:list` |
| 6 | GET | `/{tableName}/download` | 下载生成代码 | `sys:codegen:list` |

---

## 6. 统计汇总

### 按模块统计
| 模块 | Controller数 | 端点数 | 公开端点数 |
|------|-------------|--------|-----------|
| 认证中心 (Auth) | 2 | 9 | 5 |
| 系统管理 (System) | 11 | 86 | 0 |
| 门户业务 (Portal) | 21 | 105 | 18 |
| 邮件子系统 (Mail) | 3 | 14 | 0 |
| 通用模块 (Module) | 2 | 8 | 0 |
| **合计** | **40** | **222** | **23** |

### 公开接口清单（无需认证）
| # | 模块 | 端点 | 说明 |
|---|------|------|------|
| 1 | Auth | `GET /api/v1/auth/captcha` | 验证码 |
| 2 | Auth | `POST /api/v1/auth/login` | 账号密码登录 |
| 3 | Auth | `POST /api/v1/auth/login/sms` | 短信登录 |
| 4 | Auth | `POST /api/v1/auth/sms/code` | 发送短信验证码 |
| 5 | Auth | `POST /api/v1/wechat/miniapp/auth/silent-login` | 微信静默登录 |
| 6 | Auth | `POST /api/v1/wechat/miniapp/auth/phone-login` | 微信手机号登录 |
| 7 | Portal | `POST /api/v1/portal/auth/email/code` | 邮箱验证码 |
| 8 | Portal | `POST /api/v1/portal/auth/email/login` | 邮箱登录 |
| 9 | Portal | `GET /api/v1/portal/*` | 门户展示数据（10个端点） |
| 10 | Portal | `POST /api/v1/registerUser` | 注册（防重提交） |
| 11 | Portal | `GET /api/v1/payment/config` | Stripe发布密钥 |
| 12 | Portal | `POST /api/v1/paperSubmission/query/*` | 投稿查询（4个端点） |
| 13 | Portal | `POST /api/v1/stripe/webhook` | Stripe回调（签名验证） |
| 14 | Portal | `POST /api/v1/payment/alipay/notify` | 支付宝回调（签名验证） |
| 15 | Portal | `POST /api/v1/payment/wechat/notify` | 微信回调（签名验证） |
| 16 | System | `POST /api/v1/users/registerUser` | 门户注册 |

### 权限标识模式
系统管理模块使用 `sys:资源:操作` 三段式权限标识：
- `sys:user:list/add/update/delete/import/export/resetPwd`
- `sys:role:list/add/update/delete`
- `sys:menu:list/add/update/delete`
- `sys:dept:list/add/update/delete`
- `sys:dict:list/add/update/delete`
- `sys:config:list/add/update/delete`
- `sys:notice:list/add/update/delete`
- `sys:log:list`
- `sys:emailTemplate:list/add/update/delete`
- `sys:registrationType:list/add/update/delete`
- `sys:registerUser:list/update/delete`
- `sys:paperSubmission:list`
- `sys:codegen:list/add/delete`

门户业务模块多数端点仅需登录认证（✔），不使用细粒度权限控制。

---

## 7. 设计模式与约定

### 7.1 RESTful约定
- **列表查询**：`GET /` 返回分页数据（PageResult）
- **表单数据**：`GET /{id}/form` 返回编辑表单所需数据
- **新增**：`POST /` 请求体为 Form 对象
- **修改**：`PUT /{id}` 请求体为 Form 对象
- **删除**：`DELETE /{ids}` 支持批量删除（逗号分隔）
- **状态切换**：`PUT /{id}/status` 修改启用/禁用状态

### 7.2 统一响应格式
```json
{
  "code": "00000",
  "msg": "success",
  "data": {}
}
```
分页响应：
```json
{
  "code": "00000",
  "msg": "success",
  "data": {
    "records": [],
    "total": 0,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 7.3 特殊端点
- **防重提交**：`@RepeatSubmit` 注解（如注册用户POST）
- **操作日志**：`@Log` 注解记录关键操作
- **文件上传**：`POST /api/v1/files` multipart/form-data
- **PDF导出**：`GET /api/v1/paperSubmission/{id}/exportPdf` 返回流
- **支付回调**：3个Webhook端点，通过签名验证而非JWT

> 文档生成时间：2026-08-14 | 基于代码扫描，未经人工校验