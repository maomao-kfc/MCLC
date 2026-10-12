/**
 * wordCounter.js - 单词计数工具
 *
 * 单词计数规则（参考 Microsoft Word）：
 * - don't 算 1 个单词（撇号连接）
 * - e-mail 算 1 个单词（连字符连接）
 * - 中文每个汉字算 1 个词
 * - 纯数字也算单词（如 2024）
 */

/**
 * 按类似 Microsoft Word 的规则统计单词数
 *
 * @param {string} text - 待计数的文本
 * @returns {number} - 单词数
 *
 * 示例：
 *   countWords("Hello world")          -> 2
 *   countWords("don't stop")           -> 2
 *   countWords("e-mail is popular")    -> 3
 *   countWords("这是一个测试")          -> 6
 *   countWords("")                     -> 0
 */
export function countWords(text) {
  if (!text || !text.trim()) return 0

  // 统计中文字符数（每个汉字算 1 个词）
  const chineseChars = (text.match(/[\u4e00-\u9fa5]/g) || []).length

  // 统计英文单词数
  // 正则：[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*(?:'[A-Za-z]+)?
  // - [A-Za-z0-9]+              单词主体（字母或数字）
  // - (?:-[A-Za-z0-9]+)*        连字符+字母数字（e-mail, well-known）
  // - (?:'[A-Za-z]+)?           撇号+字母（don't, it's）
  const wordPattern = /[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*(?:'[A-Za-z]+)?/g
  const englishWords = (text.match(wordPattern) || []).length

  return chineseChars + englishWords
}