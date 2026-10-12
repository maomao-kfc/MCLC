export async function toEnglish(text) {
    if (!text) return text
    try {
        const res = await fetch(
            `https://api.mymemory.translated.net/get?q=${encodeURIComponent(text)}&langpair=zh|en`
        )
        const data = await res.json()
        return data.responseData?.translatedText || text
    } catch (err) {
        return text // 翻译失败返回原文
    }
}
