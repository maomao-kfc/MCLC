-- ============================================================
-- MCLC 会议日程测试数据
-- 用途：本地开发环境测试所有条目类型和UI场景
-- 覆盖类型：
--   板块类型：KEYNOTE(主旨报告), INVITED_SPEECH(专题发言), DISCUSSION(Panel Discussion)
--   条目类型：OPENING, KEYNOTE, INVITED_SPEECH, GENERAL_SPEECH, BREAK, LUNCH, DISCUSSION, CLOSING
--   特殊场景：待发布(TBD)、长标题、无讲者
-- 使用：先执行本脚本清空旧数据再插入新数据
-- ============================================================

-- 1. 清空旧数据（按外键依赖顺序）
DELETE FROM meeting_session_participant;
DELETE FROM meeting_agenda_item;
DELETE FROM meeting_session;
DELETE FROM meeting_info;

-- 2. 会议基础信息（2天）
INSERT INTO `meeting_info` VALUES (1, '2027眼科与视光学术大会', '2027 Ophthalmology & Optometry Academic Conference', '2027-01-08', '上海国际会议中心', 'Shanghai International Convention Center', 1, 1, NOW(), NOW());
INSERT INTO `meeting_info` VALUES (2, '2027眼科与视光学术大会', '2027 Ophthalmology & Optometry Academic Conference', '2027-01-09', '上海国际会议中心', 'Shanghai International Convention Center', 1, 2, NOW(), NOW());

-- ============================================================
-- Day 1 (1/8 星期五) - meeting_id=1
-- ============================================================

-- 3. 板块数据 - Day 1
INSERT INTO `meeting_session` VALUES (1, 1, '开幕式', 'Opening Ceremony', '王明', 'Wang Ming', '', '', '08:00:00', 'OPENING', 1, '', '', NOW(), NOW());
INSERT INTO `meeting_session` VALUES (2, 1, '主旨报告', 'Keynote Speech', '张华', 'Zhang Hua', '', '', '08:30:00', 'KEYNOTE', 2, '', '', NOW(), NOW());
INSERT INTO `meeting_session` VALUES (3, 1, '专题发言', 'Invited Speaker', '李芳', 'Li Fang', '', '', '10:20:00', 'INVITED_SPEECH', 3, '', '', NOW(), NOW());
INSERT INTO `meeting_session` VALUES (4, 1, '近视防控专题讨论', 'Panel Discussion: Myopia Control', '陈伟', 'Chen Wei', '陈伟,赵敏,周强,吴丽', 'Chen Wei, Zhao Min, Zhou Qiang, Wu Li', '14:00:00', 'DISCUSSION', 4, '', '', NOW(), NOW());
INSERT INTO `meeting_session` VALUES (5, 1, '闭幕式', 'Closing Ceremony', '王明', 'Wang Ming', '', '', '17:00:00', 'CLOSING', 5, '', '', NOW(), NOW());

-- 4. 条目数据 - Day 1

-- Session 1: 开幕式 (OPENING)
INSERT INTO `meeting_agenda_item` VALUES (1, 1, '', '开幕式', 'Opening Ceremony', '', '', 'OPENING', NULL, '', '', '', '', 1, 0, NOW(), NOW());

-- Session 2: 主旨报告 (KEYNOTE)
INSERT INTO `meeting_agenda_item` VALUES (2, 2, '08:30-09:00', '主旨报告', 'Keynote Speech', '如何判断该治疗对这位患者是否有效？', 'How do I know if this treatment is working in this patient?', 'KEYNOTE', NULL, 'Mark Bullimore', 'Mark Bullimore', '休斯顿大学', 'University of Houston', 1, 0, NOW(), NOW());
INSERT INTO `meeting_agenda_item` VALUES (3, 2, '09:00-09:30', '主旨报告', 'Keynote Speech', '近视控制镜片有效性评估指标', 'Metrics for efficacy with myopia control lenses', 'KEYNOTE', NULL, 'Padmaja Sankaridurg', 'Padmaja Sankaridurg', '新南威尔士大学', 'University of New South Wales', 2, 0, NOW(), NOW());
INSERT INTO `meeting_agenda_item` VALUES (4, 2, '09:30-10:00', '主旨报告', 'Keynote Speech', '角膜塑形镜联合不同时间节点及频次0.01%阿托品滴眼液的近视控制研究', 'Myopia Control with Orthokeratology Lenses Combined with 0.01% Atropine Eye Drops at Different Time Points and Frequencies', 'KEYNOTE', NULL, '姜珺', 'Jun Jiang', '温州医科大学附属眼视光医院', 'Eye Hospital, Wenzhou Medical University', 3, 0, NOW(), NOW());
INSERT INTO `meeting_agenda_item` VALUES (5, 2, '10:00-10:20', '大会发言', 'Free Paper', '', '', 'GENERAL_SPEECH', NULL, '', '', '', '', 4, 0, NOW(), NOW());
INSERT INTO `meeting_agenda_item` VALUES (6, 2, '', '茶歇/壁报', 'Tea/Poster Break', '', '', 'BREAK', NULL, '', '', '', '', 5, 0, NOW(), NOW());

-- Session 3: 专题发言 (INVITED_SPEECH)
INSERT INTO `meeting_agenda_item` VALUES (7, 3, '10:40-11:10', '专题发言', 'Invited Speaker', '低能量红光在近视预防和治疗中有效性的系列研究', 'A series of studies on the effectiveness of low-energy redlight in the prevention and treatment of myopia', 'INVITED_SPEECH', NULL, '邹海东', 'Haidong Zou', '上海市眼病防治中心', 'Shanghai Eye Diseases Prevention and Treatment Center', 1, 0, NOW(), NOW());
INSERT INTO `meeting_agenda_item` VALUES (8, 3, '11:10-11:40', '专题发言', 'Invited Speaker', '近视最新临床研究', 'Latest Clinical Research on Myopia', 'INVITED_SPEECH', NULL, '杨晓', 'Xiao Yang', '中山大学中山眼科中心', 'Zhongshan Ophthalmic Center, Sun Yat-Sen University', 2, 0, NOW(), NOW());
INSERT INTO `meeting_agenda_item` VALUES (9, 3, '11:40-12:10', '专题发言', 'Invited Speaker', '待发布', 'TBD', 'INVITED_SPEECH', NULL, '张伟', 'Wei Zhang', '北京协和医院', 'Peking Union Medical College Hospital', 3, 0, NOW(), NOW());
INSERT INTO `meeting_agenda_item` VALUES (10, 3, '', '中餐-专题会', 'Lunchtime Symposium', '', '', 'LUNCH', NULL, '', '', '', '', 4, 0, NOW(), NOW());

-- Session 4: Panel Discussion - 无条目，用participants
-- Session 5: 闭幕式 (CLOSING)
INSERT INTO `meeting_agenda_item` VALUES (11, 5, '', '闭幕式', 'Closing Ceremony', '', '', 'CLOSING', NULL, '', '', '', '', 1, 0, NOW(), NOW());

-- 5. Panel Discussion 参与人 - Day 1
INSERT INTO `meeting_session_participant` VALUES (1, 4, NULL, '陈伟', 'Chen Wei', 'MODERATOR', 1, NOW(), NOW());
INSERT INTO `meeting_session_participant` VALUES (2, 4, NULL, '赵敏', 'Zhao Min', 'PANELIST', 2, NOW(), NOW());
INSERT INTO `meeting_session_participant` VALUES (3, 4, NULL, '周强', 'Zhou Qiang', 'PANELIST', 3, NOW(), NOW());
INSERT INTO `meeting_session_participant` VALUES (4, 4, NULL, '吴丽', 'Wu Li', 'PANELIST', 4, NOW(), NOW());

-- ============================================================
-- Day 2 (1/9 星期六) - meeting_id=2
-- ============================================================

-- 6. 板块数据 - Day 2
INSERT INTO `meeting_session` VALUES (6, 2, '主旨报告', 'Keynote Speech', '刘悦', 'Maria Liu', '', '', '08:30:00', 'KEYNOTE', 1, '', '', NOW(), NOW());
INSERT INTO `meeting_session` VALUES (7, 2, '专题发言', 'Invited Speaker', 'James Wolffsohn', 'James Wolffsohn', '', '', '10:20:00', 'INVITED_SPEECH', 2, '', '', NOW(), NOW());
INSERT INTO `meeting_session` VALUES (8, 2, '真实世界近视病例专题讨论', 'Panel Discussion: Real World Myopia Cases', '陈志', 'Zhi Chen', '陈志,Langis Michaud,Lachlan Hoy,姜珺,刘悦', 'Zhi Chen, Langis Michaud, Lachlan Hoy, Jun Jiang, Maria Liu', '14:00:00', 'DISCUSSION', 3, '', '', NOW(), NOW());

-- 7. 条目数据 - Day 2

-- Session 6: 主旨报告 (KEYNOTE)
INSERT INTO `meeting_agenda_item` VALUES (12, 6, '08:30-09:00', '主旨报告', 'Keynote Speech', '高度近视与病理性近视研究', 'Research on High Myopia and Pathologic Myopia', 'KEYNOTE', NULL, '吕帆', 'Fan Lu', '温州医科大学附属眼视光医院', 'Eye Hospital, Wenzhou Medical University', 1, 0, NOW(), NOW());
INSERT INTO `meeting_agenda_item` VALUES (13, 6, '09:00-09:30', '主旨报告', 'Keynote Speech', '散光性模糊对眼轴生长的调控作用：从小鸡模型到人类近视管理', 'The role of astigmatic blur in directing eye growth: evidence from chick models and potential implications for human myopia management', 'KEYNOTE', NULL, '纪家树', 'Chea-su Kee', '香港理工大学', 'Hong Kong Polytechnic University', 2, 0, NOW(), NOW());
INSERT INTO `meeting_agenda_item` VALUES (14, 6, '09:30-10:00', '主旨报告', 'Keynote Speech', '待发布', 'TBD', 'KEYNOTE', NULL, '李明', 'Ming Li', '复旦大学附属眼耳鼻喉科医院', 'Fudan University Eye & ENT Hospital', 3, 0, NOW(), NOW());
INSERT INTO `meeting_agenda_item` VALUES (15, 6, '10:00-10:20', '大会发言', 'Free Paper', '', '', 'GENERAL_SPEECH', NULL, '', '', '', '', 4, 0, NOW(), NOW());
INSERT INTO `meeting_agenda_item` VALUES (16, 6, '', '茶歇/壁报', 'Tea/Poster Break', '', '', 'BREAK', NULL, '', '', '', '', 5, 0, NOW(), NOW());

-- Session 7: 专题发言 (INVITED_SPEECH)
INSERT INTO `meeting_agenda_item` VALUES (17, 7, '10:40-11:10', '专题发言', 'Invited Speaker', 'TFOS DEWS III 在临床干眼管理中的应用', 'TFOS DEWS III in clinical dry eye management', 'INVITED_SPEECH', NULL, 'James Wolffsohn', 'James Wolffsohn', '英国伯明翰阿斯顿大学', 'Aston University, Birmingham, UK', 1, 0, NOW(), NOW());
INSERT INTO `meeting_agenda_item` VALUES (18, 7, '11:10-11:40', '专题发言', 'Invited Speaker', '不同近视干预手段对眼表的影响：阿托品、接触镜及其联合治疗', 'Ocular surface changes due to variety of myopia interventions: atropine, contact lenses, combinations', 'INVITED_SPEECH', NULL, 'Debarun Dutta', 'Debarun Dutta', '英国阿斯顿大学', 'Aston University, UK', 2, 0, NOW(), NOW());
INSERT INTO `meeting_agenda_item` VALUES (19, 7, '11:40-12:10', '专题发言', 'Invited Speaker', '近视与圆锥角膜：诊断重叠的辨析', 'Myopia vs keratoconus: navigating the diagnostic overlap', 'INVITED_SPEECH', NULL, 'Elizabeth Chetty', 'Elizabeth Chetty', '南非约翰内斯堡大学', 'University of Johannesburg, South Africa', 3, 0, NOW(), NOW());
INSERT INTO `meeting_agenda_item` VALUES (20, 7, '', '中餐-专题会', 'Lunchtime Symposium', '', '', 'LUNCH', NULL, '', '', '', '', 4, 0, NOW(), NOW());

-- Session 8: Panel Discussion - 无条目，用participants

-- 8. Panel Discussion 参与人 - Day 2
INSERT INTO `meeting_session_participant` VALUES (5, 8, NULL, '陈志', 'Zhi Chen', 'MODERATOR', 1, NOW(), NOW());
INSERT INTO `meeting_session_participant` VALUES (6, 8, NULL, 'Langis Michaud', 'Langis Michaud', 'PANELIST', 2, NOW(), NOW());
INSERT INTO `meeting_session_participant` VALUES (7, 8, NULL, 'Lachlan Hoy', 'Lachlan Hoy', 'PANELIST', 3, NOW(), NOW());
INSERT INTO `meeting_session_participant` VALUES (8, 8, NULL, '姜珺', 'Jun Jiang', 'PANELIST', 4, NOW(), NOW());
INSERT INTO `meeting_session_participant` VALUES (9, 8, NULL, '刘悦', 'Maria Liu', 'PANELIST', 5, NOW(), NOW());