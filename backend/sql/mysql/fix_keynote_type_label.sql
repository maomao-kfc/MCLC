-- ============================================================
-- 修复日程类型中英文标签
-- 执行时间：2026-08-12
-- 说明：
--   1. KEYNOTE: 中文"专题发言"→"主旨报告", 英文"Keynote Speech"→"Keynote Speech"
--   2. INVITED_SPEECH: 中文"邀请报告"→"专题发言", 英文"Invited Speech"→"Invited Speaker"
--   3. GENERAL_SPEECH: 英文"General Speech"→"Free Paper"
--   4. BREAK: 英文"Tea Break / Poster"→"Tea/Poster Break"
--   5. LUNCH: 英文"Lunch - Symposium"→"Lunchtime Symposium"
--   6. meeting_session中KEYNOTE板块的title_en同步修正
-- ============================================================

-- 1. KEYNOTE条目：中文"专题发言"或"主旨演讲"→"主旨报告", 英文统一为"Keynote Speech"
UPDATE meeting_agenda_item SET type = '主旨报告', type_en = 'Keynote Speech' WHERE item_type = 'KEYNOTE';

-- 2. INVITED_SPEECH条目：中文"邀请报告"→"专题发言", 英文"Invited Speech"→"Invited Speaker"
UPDATE meeting_agenda_item SET type = '专题发言', type_en = 'Invited Speaker' WHERE item_type = 'INVITED_SPEECH';

-- 3. GENERAL_SPEECH条目：中文"大会发言"保持不变, 英文"General Speech"→"Free Paper"
UPDATE meeting_agenda_item SET type_en = 'Free Paper' WHERE item_type = 'GENERAL_SPEECH';

-- 4. BREAK条目：英文"Tea Break / Poster"→"Tea/Poster Break"
UPDATE meeting_agenda_item SET type_en = 'Tea/Poster Break' WHERE item_type = 'BREAK';

-- 5. LUNCH条目：英文"Lunch - Symposium"→"Lunchtime Symposium"
UPDATE meeting_agenda_item SET type_en = 'Lunchtime Symposium' WHERE item_type = 'LUNCH';

-- 6. meeting_session中KEYNOTE板块：title_en修正为"Keynote Speech"
UPDATE meeting_session SET title_en = 'Keynote Speech' WHERE item_type = 'KEYNOTE' AND title_en != 'Keynote Speech';