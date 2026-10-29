# MCLC 会议日程子系统技术文档

> 4级层级结构(MeetingInfo→Session→AgendaItem+Participant)、9种日程类型自动填充、先删后增更新策略

## 1. 架构总览

会议日程子系统采用 **4级层级结构**，管理大会日程的完整生命周期：

```
MeetingInfo (会议)
  └── MeetingSession (板块/场次)
        ├── MeetingAgendaItem (议程项)
        └── MeetingSessionParticipant (参与者)
```

### 核心设计原则

| 原则 | 实现方式 |
|------|----------|
| 层级数据组装 | MeetingScheduleVO 递归嵌套 VO |
| 国际化双轨制 | 列式 _en 后缀 + COALESCE 降级 |
| 更新策略 | 先删后增(delete all → re-insert) |
| 类型自动填充 | ITEM_TYPE_MAP 枚举→中文/英文标签映射 |
| 级联删除 | deleteMeeting: session→item+participant→session→meeting |

### 关键文件索引

| 层级 | 实体 | VO | Form | Service | Controller |
|------|------|----|------|---------|------------|
| L1 会议 | MeetingInfo | MeetingScheduleVO | - | MeetingInfoServiceImpl | MeetingScheduleController |
| L2 板块 | MeetingSession | SessionVO | MeetingSessionForm | MeetingSessionServiceImpl | 同上 |
| L3 议程项 | MeetingAgendaItem | AgendaItemVO | AgendaItemForm | MeetingAgendaItemServiceImpl | 同上 |
| L3 参与者 | MeetingSessionParticipant | ParticipantVO | ParticipantForm | MeetingSessionParticipantServiceImpl | 同上 |

## 2. 数据模型

### 2.1 MeetingInfo (会议)

**包路径**: `com.youlai.boot.portal.model.entity.MeetingInfo`
**表名**: `meeting_info`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| name | String | 会议名称(中文) |
| name_en | String | 会议名称(英文) |
| meeting_date | LocalDate | 会议日期 |
| venue | String | 场地(中文) |
| venue_en | String | 场地(英文) |
| sort_order | Integer | 排序权重 |
| status | Integer | 状态(0=禁用,1=启用) |

### 2.2 MeetingSession (板块/场次)

**包路径**: `com.youlai.boot.portal.model.entity.MeetingSession`
**表名**: `meeting_session`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| meeting_id | Long | 外键→meeting_info.id |
| name | String | 板块名称(中文) |
| name_en | String | 板块名称(英文) |
| session_date | LocalDate | 板块日期 |
| start_time | LocalTime | 开始时间 |
| end_time | LocalTime | 结束时间 |
| sort_order | Integer | 排序权重 |

### 2.3 MeetingAgendaItem (议程项)

**包路径**: `com.youlai.boot.portal.model.entity.MeetingAgendaItem`
**表名**: `meeting_agenda_item`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| session_id | Long | 外键→meeting_session.id |
| item_type | Integer | 议程类型(1-9枚举) |
| title | String | 标题(中文) |
| title_en | String | 标题(英文) |
| start_time | LocalTime | 开始时间 |
| end_time | LocalTime | 结束时间 |
| speaker_name | String | 演讲者姓名 |
| speaker_name_en | String | 演讲者姓名(英文) |
| speaker_title | String | 演讲者头衔 |
| speaker_title_en | String | 演讲者头衔(英文) |
| speaker_org | String | 演讲者机构 |
| speaker_org_en | String | 演讲者机构(英文) |
| sort_order | Integer | 排序权重 |

### 2.4 MeetingSessionParticipant (参与者)

**包路径**: `com.youlai.boot.portal.model.entity.MeetingSessionParticipant`
**表名**: `meeting_session_participant`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| session_id | Long | 外键→meeting_session.id |
| name | String | 参与者姓名(中文) |
| name_en | String | 参与者姓名(英文) |
| title | String | 头衔(中文) |
| title_en | String | 头衔(英文) |
| org | String | 机构(中文) |
| org_en | String | 机构(英文) |
| sort_order | Integer | 排序权重 |

### 2.5 ER关系图

```
meeting_info (1) ──→ (N) meeting_session
meeting_session (1) ──→ (N) meeting_agenda_item
meeting_session (1) ──→ (N) meeting_session_participant
```

## 3. itemType 枚举与自动填充

### 3.1 9种议程类型

`MeetingInfoServiceImpl` 中定义了 `ITEM_TYPE_MAP`，将 itemType 枚举值映射为中英文标签：

| itemType | 中文标签 | 英文标签 | 用途 |
|----------|---------|---------|------|
| 1 | 开幕式 | Opening Ceremony | 大会开幕 |
| 2 | 主题演讲 | Keynote Speech | 主旨演讲 |
| 3 | 特邀报告 | Invited Talk | 特邀学术报告 |
| 4 | 分论坛 | Forum | 分论坛讨论 |
| 5 | 圆桌论坛 | Round Table | 圆桌讨论 |
| 6 | 茶歇 | Tea Break | 茶歇休息 |
| 7 | 午餐 | Lunch | 午餐时间 |
| 8 | 闭幕式 | Closing Ceremony | 大会闭幕 |
| 9 | 其他 | Other | 其他类型 |

### 3.2 自动填充机制

创建议程项时，若 `title` 为空，系统根据 `itemType` 自动从 `ITEM_TYPE_MAP` 填充 title/title_en：

```java
// MeetingInfoServiceImpl - createSchedule 内部逻辑
if (item.getTitle() == null || item.getTitle().isEmpty()) {
    String[] labels = ITEM_TYPE_MAP.get(item.getItemType());
    if (labels != null) {
        item.setTitle(labels[0]);      // 中文标签
        item.setTitleEn(labels[1]);    // 英文标签
    }
}
```

**设计意图**: 茶歇、午餐等固定类型无需手动输入标题，选择类型后自动生成。

## 4. VO层级组装

### 4.1 MeetingScheduleVO (顶层)

```java
public class MeetingScheduleVO {
    private Long id;                    // meeting_info.id
    private String name;                // 会议名称
    private String nameEn;
    private LocalDate meetingDate;
    private String venue;
    private String venueEn;
    private Integer sortOrder;
    private Integer status;
    private List<SessionVO> sessions;   // 嵌套板块列表
}
```

### 4.2 SessionVO (板块层)

```java
public class SessionVO {
    private Long id;
    private Long meetingId;
    private String name;
    private String nameEn;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer sortOrder;
    private List<AgendaItemVO> items;           // 议程项列表
    private List<ParticipantVO> participants;   // 参与者列表
}
```

### 4.3 AgendaItemVO / ParticipantVO (叶子层)

```java
// AgendaItemVO - 议程项
public class AgendaItemVO {
    private Long id;
    private Long sessionId;
    private Integer itemType;
    private String title;
    private String titleEn;
    private LocalTime startTime;
    private LocalTime endTime;
    private String speakerName;       // COALESCE降级
    private String speakerNameEn;
    private String speakerTitle;
    private String speakerTitleEn;
    private String speakerOrg;
    private String speakerOrgEn;
    private Integer sortOrder;
}

// ParticipantVO - 参与者
public class ParticipantVO {
    private Long id;
    private Long sessionId;
    private String name;
    private String nameEn;
    private String title;
    private String titleEn;
    private String org;
    private String orgEn;
    private Integer sortOrder;
}
```

## 5. API接口

**Controller**: `com.youlai.boot.portal.controller.MeetingScheduleController`
**基础路径**: `/api/v1/meeting-schedule`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /list | 获取所有会议日程(层级VO) | 公开 |
| GET | /{id} | 获取单个会议日程详情 | 公开 |
| POST | / | 创建完整会议日程 | 需认证 |
| PUT | /session/{sessionId} | 更新板块(含议程项+参与者) | 需认证 |
| DELETE | /meeting/{meetingId} | 删除会议(级联) | 需认证 |

### 5.1 创建日程请求体

```json
{
  "name": "2027 MCLC 大会",
  "nameEn": "2027 MCLC Conference",
  "meetingDate": "2027-08-15",
  "venue": "北京国际会议中心",
  "venueEn": "Beijing International Convention Center",
  "sortOrder": 1,
  "status": 1,
  "sessions": [
    {
      "name": "开幕式板块",
      "nameEn": "Opening Session",
      "sessionDate": "2027-08-15",
      "startTime": "09:00",
      "endTime": "12:00",
      "sortOrder": 1,
      "items": [
        {
          "itemType": 1,
          "startTime": "09:00",
          "endTime": "09:30"
        }
      ],
      "participants": [
        {
          "name": "张三",
          "nameEn": "San Zhang",
          "sortOrder": 1
        }
      ]
    }
  ]
}
```

## 6. 核心业务流程

### 6.1 创建日程 (createSchedule)

**方法**: `MeetingInfoServiceImpl.createSchedule()`

```
1. 构建 MeetingInfo → save → 获取 meetingId
2. 遍历 sessions:
   a. 构建 MeetingSession → save → 获取 sessionId
   b. 遍历 items:
      - itemType 自动填充 title/title_en (若title为空)
      - 构建 MeetingAgendaItem → save
   c. 遍历 participants:
      - 构建 MeetingSessionParticipant → save
3. 返回 true
```

### 6.2 更新板块 (updateSession)

**方法**: `MeetingInfoServiceImpl.updateSession()`

**核心策略: 先删后增**

```
1. 验证 sessionId 存在
2. 更新 MeetingSession 基础字段
3. 先删: 删除该 session 下所有 agenda_items
4. 先删: 删除该 session 下所有 participants
5. 后增: 遍历新 items → 逐条 save
6. 后增: 遍历新 participants → 逐条 save
7. 返回 true
```

**设计考量**: 避免复杂的 diff 算法，通过全量替换保证数据一致性。适用于日程数据量小、更新频率低的场景。

### 6.3 删除会议 (deleteMeeting)

**方法**: `MeetingInfoServiceImpl.deleteMeeting()`

**级联删除顺序**:

```
1. 查询 meeting 下所有 sessions
2. 遍历每个 session:
   a. 删除 session 下所有 agenda_items
   b. 删除 session 下所有 participants
3. 删除所有 sessions
4. 删除 meeting 本身
5. 返回 true
```

**注意**: 删除顺序必须为 item/participant → session → meeting，否则外键约束报错。

## 7. 国际化策略

### 7.1 列式 _en 后缀

会议日程模块采用 **列式国际化** 策略（非 portal_* 的行式 country_type 策略）：

- 每个中文字段对应一个 `_en` 后缀英文字段
- 例: `name` ↔ `name_en`, `title` ↔ `title_en`, `speaker_name` ↔ `speaker_name_en`

### 7.2 COALESCE 降级查询

Mapper XML 中使用 COALESCE 实现英文降级到中文：

```sql
-- MeetingAgendaItemMapper.xml 示例
COALESCE(speaker_name_en, speaker_name) AS speakerName
COALESCE(speaker_title_en, speaker_title) AS speakerTitle
COALESCE(speaker_org_en, speaker_org) AS speakerOrg
```

**降级逻辑**: 优先返回英文字段，若为 NULL 则回退到中文字段，保证前端始终有值显示。

## 8. Form表单结构

### 8.1 MeetingSessionForm

```java
public class MeetingSessionForm {
    private Long id;                           // 更新时传入
    private String name;
    private String nameEn;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer sortOrder;
    private List<AgendaItemForm> items;        // 嵌套议程项
    private List<ParticipantForm> participants; // 嵌套参与者
}
```

### 8.2 AgendaItemForm

```java
public class AgendaItemForm {
    private Integer itemType;      // 1-9 枚举
    private String title;
    private String titleEn;
    private LocalTime startTime;
    private LocalTime endTime;
    private String speakerName;
    private String speakerNameEn;
    private String speakerTitle;
    private String speakerTitleEn;
    private String speakerOrg;
    private String speakerOrgEn;
    private Integer sortOrder;
}
```

### 8.3 ParticipantForm

```java
public class ParticipantForm {
    private String name;
    private String nameEn;
    private String title;
    private String titleEn;
    private String org;
    private String orgEn;
    private Integer sortOrder;
}
```

## 9. 数据流图

```
前端 Form 提交
    │
    ▼
MeetingScheduleController
    │
    ▼
MeetingInfoServiceImpl
    ├── createSchedule()
    │     ├── MeetingInfo.save()
    │     ├── MeetingSession.save()
    │     ├── MeetingAgendaItem.save() (含 itemType 自动填充)
    │     └── MeetingSessionParticipant.save()
    │
    ├── updateSession()
    │     ├── MeetingSession.update()
    │     ├── MeetingAgendaItem.remove(sessionId)  ← 先删
    │     ├── MeetingSessionParticipant.remove(sessionId) ← 先删
    │     ├── MeetingAgendaItem.save(batch)  ← 后增
    │     └── MeetingSessionParticipant.save(batch) ← 后增
    │
    └── deleteMeeting()
          ├── MeetingAgendaItem.remove(sessionIds)
          ├── MeetingSessionParticipant.remove(sessionIds)
          ├── MeetingSession.remove(meetingId)
          └── MeetingInfo.remove(meetingId)

查询流程:
MeetingScheduleController.getList()
    │
    ▼
MeetingInfoServiceImpl (组装层级VO)
    ├── MeetingInfo.list()
    ├── 遍历 → MeetingSession.getByMeetingId()
    │     ├── MeetingAgendaItem.getBySessionId() (含COALESCE)
    │     └── MeetingSessionParticipant.getBySessionId()
    └── 返回 List<MeetingScheduleVO>
```

## 10. 开发指南

### 10.1 新增议程类型

1. 在 `MeetingInfoServiceImpl.ITEM_TYPE_MAP` 中添加新枚举映射
2. 确保 itemType 值不与现有 1-9 冲突
3. 前端对应的类型选择下拉框需同步更新

### 10.2 修改更新策略

当前"先删后增"策略适用于低频更新场景。若需改为增量更新：

1. 为 AgendaItemForm/ParticipantForm 添加 `id` 字段
2. 实现差异比较逻辑：新增(id=null)、更新(id存在且字段变化)、删除(原id不在新列表中)
3. 注意并发场景下的数据一致性

### 10.3 注意事项

- **级联删除顺序**: 必须先删子表(item/participant)再删父表(session/meeting)
- **itemType 自动填充**: 仅在 title 为空时触发，手动指定 title 不会被覆盖
- **COALESCE 降级**: 英文优先，NULL 时回退中文，确保前端不显示空值
- **sortOrder**: 所有层级均支持排序，前端应按 sortOrder 升序展示