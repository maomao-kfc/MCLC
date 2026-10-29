# MCLC 国际化(i18n)策略文档

> 双轨国际化：portal_* 行级策略 vs meeting_* 列级策略 + 前端Admin/Webpage i18n实现

## 1. 双轨国际化架构

```
┌─────────────────────────────────────────────────────────────┐
│                    MCLC 国际化架构                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─── 行级国际化 (Row-based) ───────────────────────────┐   │
│  │  适用: portal_* 系列表                                │   │
│  │  策略: 同一表内通过 country_type 字段区分语言版本     │   │
│  │  值域: ZH(中文) / EN(英文)                            │   │
│  │  查询: WHERE country_type = 'ZH' 或 'EN'             │   │
│  │  表数: 10个 (portal_guest/hotel/question/contact/     │   │
│  │         speech/architecture/arrange/moments/          │   │
│  │         meeting_setting/meeting_date)                 │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─── 列级国际化 (Column-based) ────────────────────────┐   │
│  │  适用: meeting_* 系列表                               │   │
│  │  策略: 同一行内通过 _en 后缀列存储英文翻译            │   │
│  │  模式: name + name_en, title + title_en              │   │
│  │  查询: SELECT name, name_en FROM ...                 │   │
│  │  表数: 4个 (meeting_info/meeting_session/             │   │
│  │         meeting_agenda_item/meeting_speaker)          │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─── 前端UI国际化 ─────────────────────────────────────┐   │
│  │  Admin: vue-i18n (zh-cn.json / en.json)              │   │
│  │  Webpage: vue-i18n (zh.js / en.js)                   │   │
│  │  切换: LangSelect组件 → Pinia store → localStorage   │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## 2. 行级国际化 (portal_* 表)

### 2.1 实现方式

portal_* 表通过 `country_type` 字段区分中英文版本，每种语言一条记录：

```sql
-- portal_guest 表示例
-- 中文版本
INSERT INTO portal_guest (name, position, country_type, ...) 
VALUES ('张三', '主任医师', 'ZH', ...);

-- 英文版本（同一个人，不同语言记录）
INSERT INTO portal_guest (name, position, country_type, ...) 
VALUES ('Dr. Zhang San', 'Chief Physician', 'EN', ...);
```

### 2.2 适用表清单

| 表名 | 实体类 | country_type字段 | 说明 |
|------|--------|------------------|------|
| portal_guest | Guest | ✓ | 特邀嘉宾 |
| portal_hotel | Hotel | ✓ | 酒店信息 |
| portal_question | Question | ✓ | 常见问题 |
| portal_contact | Contact | ✓ | 联系方式 |
| portal_speech | Speech/SpeechV1 | ✓ | 演讲集锦 |
| portal_architecture | Architecture | ✓ | 组织架构 |
| portal_arrange | Arrange | ✓ | 会议安排 |
| portal_moments | Moments | ✓ | 精彩瞬间 |
| portal_meeting_setting | MeetingSetting | ✓ | 会议设置 |
| portal_meeting_date | MeetingDate | ✓ | 会议日期 |

### 2.3 后端查询模式

所有 portal_* 的 ServiceImpl 使用统一的 countryType 过滤模式：

```java
// 标准模式 - 所有 portal ServiceImpl 一致
String countryType = queryParams.getCountryType();
if (StrUtil.isNotBlank(countryType)) {
    queryWrapper.eq(Guest::getCountryType, countryType);
}
```

### 2.4 前端Admin查询模式

Admin 端通过查询参数传递 countryType：

```vue
<!-- 查询表单 -->
<el-form-item prop="countryType" label="语言类型">
  <el-select v-model="queryParams.countryType" placeholder="全部">
    <el-option label="中文" value="ZH" />
    <el-option label="英文" value="EN" />
  </el-select>
</el-form-item>

<!-- 列表展示 -->
<el-tag v-if="scope.row.countryType === 'ZH'">中文</el-tag>
<el-tag v-else-if="scope.row.countryType === 'EN'">英文</el-tag>
```

### 2.5 门户API查询模式

PortalController 公开接口通过 URL 参数传递：

```java
@GetMapping("/meetingDate/list")
public Result<List<MeetingDateVO>> getMeetingDateList(
    @Parameter(description = "国家类型") @RequestParam(required = false) String countryType) {
    return Result.success(meetingDateService.getMeetingDateList(countryType));
}
```

## 3. 列级国际化 (meeting_* 表)

### 3.1 实现方式

meeting_* 表在同一行内通过 `_en` 后缀列存储英文翻译：

```sql
-- meeting_speaker 表示例
CREATE TABLE meeting_speaker (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255),           -- 中文姓名
    name_en VARCHAR(255),        -- 英文姓名
    title VARCHAR(255),          -- 中文职称
    title_en VARCHAR(255),       -- 英文职称
    affiliation VARCHAR(255),    -- 中文单位
    affiliation_en VARCHAR(255), -- 英文单位
    brief_intro TEXT,            -- 中文简介
    brief_intro_en TEXT,         -- 英文简介
    detail_intro TEXT,           -- 中文详细介绍
    detail_intro_en TEXT,        -- 英文详细介绍
    research_fields VARCHAR(500),     -- 中文研究领域
    research_fields_en VARCHAR(500)   -- 英文研究领域
);
```

### 3.2 适用表清单与字段映射

| 表名 | 中文字段 | 英文字段(_en) |
|------|----------|---------------|
| meeting_info | name | name_en |
| meeting_session | title | title_en |
| | chairperson | chairperson_en |
| meeting_agenda_item | type | type_en |
| | topic | topic_en |
| | speaker_name | speaker_name_en |
| | speaker_org | speaker_org_en |
| meeting_speaker | name | name_en |
| | title | title_en |
| | affiliation | affiliation_en |
| | brief_intro | brief_intro_en |
| | detail_intro | detail_intro_en |
| | research_fields | research_fields_en |
| meeting_session_participant | name | name_en |

### 3.3 COALESCE 回退机制

meeting_agenda_item 的 speaker 信息支持从关联表回退：

```sql
-- MeetingAgendaItemMapper.xml
-- 如果议程条目自身有 speaker_name，使用自身值；否则从 meeting_speaker 表取值
SELECT 
    COALESCE(i.speaker_name, s.name) AS speaker_name,
    COALESCE(i.speaker_name_en, s.name_en) AS speaker_name_en,
    COALESCE(i.speaker_org, s.affiliation) AS affiliation,
    COALESCE(i.speaker_org_en, s.affiliation_en) AS affiliation_en
FROM meeting_agenda_item i
LEFT JOIN meeting_speaker s ON i.speaker_id = s.id
WHERE i.session_id = #{sessionId}
```

回退优先级：议程条目自身字段 > 关联 speaker 表字段

### 3.4 前端Admin编辑模式

Admin 端会议日程页面同时编辑中英文字段：

```vue
<!-- 会议名称 -->
<el-input v-model="meetingFormData.name" placeholder="中文名称" />
<el-input v-model="meetingFormData.nameEn" placeholder="English Name" />

<!-- 板块标题 -->
<el-input v-model="formData.title" placeholder="中文标题" />
<el-input v-model="formData.titleEn" placeholder="English Title" />

<!-- 议程主题 -->
<el-input v-model="row.topic" placeholder="中文主题" />
<el-input v-model="row.titleEn" placeholder="Topic" size="small" />
```

### 3.5 前端Webpage展示模式

Webpage 端根据当前语言选择显示字段：

```vue
<!-- GuestCard.vue -->
{{ currentLang === 'zh' ? guest?.nameZh : guest?.nameEn }}
{{ currentLang === 'zh' ? guest?.titleZh : guest?.titleEn }}

<!-- AddScheduleModal.vue -->
:label="currentLang === 'zh' ? session.title : session.titleEn"
```

## 4. 前端UI国际化

### 4.1 Admin端 (frontend/)

**技术栈**: vue-i18n + Pinia + localStorage

**语言包**:
- `frontend/src/lang/package/zh-cn.json` — 中文
- `frontend/src/lang/package/en.json` — 英文

**配置** (`frontend/src/lang/index.ts`):
```typescript
const i18n = createI18n({
  legacy: false,
  locale: appStore.language,     // 从 Pinia 读取
  messages: { 'zh-cn': zhCnLocale, en: enLocale },
  globalInjection: true,
});
```

**语言枚举** (`frontend/src/enums/settings.ts`):
```typescript
export const enum LanguageEnum {
  ZH_CN = "zh-cn",
  EN = "en",
}
```

**切换组件** (`frontend/src/components/LangSelect/index.vue`):
- 下拉选择 中文/English
- 切换时更新 vue-i18n locale + Pinia appStore.language
- 持久化到 localStorage (key: `app:language`)

**使用场景**: 菜单标签、按钮文字、表单标签、提示信息、面包屑、Tab标签

### 4.2 Webpage端 (webpage/)

**技术栈**: vue-i18n + Pinia + localStorage

**语言包**:
- `webpage/src/locales/zh.js` (43.2KB)
- `webpage/src/locales/en.js` (49.0KB)

**配置** (`webpage/src/locales/index.js`):
```javascript
const i18n = createI18n({
  legacy: false,
  locale: 'zh',
  fallbackLocale: 'zh',
  messages: { en, zh }
});
```

**语言状态管理** (`webpage/src/stores/lang.js`):
```javascript
export const useLangStore = defineStore('lang', () => {
  const currentLang = ref('zh');
  const setLang = (lang) => {
    localStorage.setItem('meeting-lang', lang);
    currentLang.value = lang;
  };
  // 初始化默认中文
  const initLang = () => {
    localStorage.setItem('meeting-lang', 'zh');
    currentLang.value = 'zh';
  };
});
```

**持久化**: localStorage key = `meeting-lang`

## 5. 两种策略对比

| 维度 | 行级(portal_*) | 列级(meeting_*) |
|------|---------------|-----------------|
| 存储方式 | 每种语言一条记录 | 同一记录包含所有语言 |
| 区分字段 | country_type (ZH/EN) | _en 后缀列 |
| 查询方式 | WHERE country_type = ? | SELECT name, name_en |
| 数据冗余 | 高(整行重复) | 低(仅翻译列冗余) |
| 新增语言 | 新增记录即可 | 需加列(ALTER TABLE) |
| 适用场景 | 内容结构简单、各语言独立 | 内容结构复杂、中英文强关联 |
| 前端处理 | 按countryType筛选显示 | 按语言选择字段显示 |
| 回退机制 | 无(查不到则无数据) | COALESCE从关联表回退 |

## 6. 新增模块国际化指南

### 6.1 新增 portal_* 类型模块

1. Entity 添加 `private String countryType;` 字段
2. Form 添加 `private String countryType;` 字段
3. Query 添加 `private String countryType;` 字段
4. VO 添加 `private String countryType;` 字段
5. ServiceImpl 添加 countryType 过滤逻辑
6. 前端查询表单添加语言类型下拉
7. 前端新增/编辑表单添加语言类型选择(默认ZH)

### 6.2 新增 meeting_* 类型模块

1. Entity 中每个需翻译的字段添加 `_en` 后缀字段
2. Mapper XML 中添加 _en 列映射
3. VO 中添加对应 _en 字段
4. 前端表单每个字段旁添加英文输入框
5. 前端展示根据 currentLang 选择字段

## 7. 常见问题排查

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| portal_* 查询返回空 | countryType 参数未传或值不对 | 检查前端是否传递 ZH/EN |
| meeting_* 英文字段为空 | 录入时未填写_en字段 | 检查数据录入，添加默认值或COALESCE |
| Admin语言切换不生效 | localStorage缓存 | 清除localStorage或检查appStore.language |
| Webpage语言切换后刷新恢复 | initLang()强制重置为zh | 检查initLang调用时机 |
| COALESCE回退不生效 | agenda_item自身字段有值但为空字符串 | COALESCE不处理空字符串，需用NULLIF |