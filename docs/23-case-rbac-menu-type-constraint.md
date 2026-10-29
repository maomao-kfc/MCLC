# 案例04：权限菜单挂 perm 无效 — youlai-boot 框架 type=B 约束

> **故障等级**: P2（新增菜单的权限注解不生效，接口返回未授权）  
> **发生时间**: 2026-10（方案B上线阶段）  
> **状态**: 【曾经发生】【已修复】【已验证】

---

## A. 故障现象

新增"支付凭证管理"菜单后，配置了权限标识（perm），但访问对应 API 仍返回 403 未授权：
```
Access Denied - 不允许访问
```

## B. 排查过程

1. **检查菜单配置**：sys_menu 表中已配置 perm 字段
2. **检查角色授权**：sys_role_menu 中已关联该菜单
3. **检查 SecurityConfig**：接口路径不在白名单中（符合预期，需要权限）
4. **对比正常菜单**：发现已生效的菜单 `type=B`（按钮类型），未生效的菜单 `type=M`（目录/菜单类型）
5. **追溯框架源码**：youlai-boot 权限查询逻辑只加载 `type=B` 的 perm

## C. 根因分析

**youlai-boot 框架设计约束：权限查询只认 type=B（按钮）的菜单**。

框架源码逻辑：
```
MenuServiceImpl → getRolePerms() 
  → 查询 sys_menu WHERE type = 'B' (Button)
  → 只收集按钮类型菜单的 perm 字段
  → 构建 Spring Security GrantedAuthority
```

- `type=M`（Menu/目录）：只用于路由展示，perm 不参与鉴权
- `type=B`（Button/按钮）：perm 参与鉴权
- 将 perm 配在 M 类型菜单上 → 框架不加载 → 接口无权限数据 → 403

## D. 修复方案

**正确做法**：在 M 类型菜单下创建 B 类型子菜单（按钮），perm 配在 B 类型上。

```
sys_menu 结构（正确示例）：
├── 支付凭证管理 (type=M, perm=null, path=/portal/paymentProof)
    ├── 查询 (type=B, perm=portal:paymentProof:list)
    ├── 审核 (type=B, perm=portal:paymentProof:review)
    └── 列表 (type=B, perm=portal:paymentProof:page)
```

**SQL 修复**：
```sql
-- 将原M菜单的perm移到新建的B子菜单
INSERT INTO sys_menu (name, type, perm, parent_id, ...) 
VALUES ('支付凭证审核', 'B', 'portal:paymentProof:review', <parent_menu_id>, ...);
```

## E. 验证方式

1. 角色绑定含 B 类型子菜单 → 访问 API 返回 200
2. 角色未绑定 → 访问 API 返回 403
3. 前端按钮级权限：v-permission 指令正常隐藏/显示按钮

## F. 影响范围

- 所有新增菜单如果 perm 配在 M 类型上，鉴权均不生效
- 已有正常菜单不受影响（已按规范配置）

## G. 经验教训

1. **框架设计约束必须了解**：youlai-boot 的 type=B 约束是框架级设计，不是 Bug
2. **新增菜单的标准操作**：M 类型做路由 + B 类型做权限，perm 只配在 B 上
3. **排查权限问题先查菜单类型**：403 时先 `SELECT type, perm FROM sys_menu WHERE perm IS NOT NULL` 确认类型
4. **前后端权限联动**：后端 perm 鉴权 + 前端 v-permission 指令，都依赖 B 类型菜单

## H. 相关代码/文件

| 文件 | 说明 |
|------|------|
| backend/.../MenuServiceImpl.java | 权限查询逻辑（只加载 type=B） |
| backend/.../SecurityConfig.java | Spring Security 鉴权配置 |
| frontend/.../permission/index.ts | v-permission 指令 |
| 数据库 sys_menu 表 | type 字段（M=菜单/C=目录/B=按钮） |