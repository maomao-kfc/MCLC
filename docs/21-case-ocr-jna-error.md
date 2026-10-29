# 案例02：OCR 识别抛 java.lang.Error 导致凭证提交失败

> **故障等级**: P1（凭证提交功能完全不可用）  
> **发生时间**: 2026-10（OCR 集成阶段）  
> **状态**: 【曾经发生】【已修复】【已验证】

---

## A. 故障现象

用户上传支付凭证时，后端报错导致整个凭证提交失败：
```
java.lang.Error: Invalid memory access
    at com.sun.jna.Native.invokeInt(...)
    at net.sourceforge.tess4j.Tesseract.doOCR(...)
```

## B. 排查过程

1. **初步定位**：错误堆栈指向 Tess4j 的 `doOCR()` 方法，JNA 层内存访问异常
2. **分析错误类型**：`java.lang.Error` 而非 `java.lang.Exception`——现有的 `catch (Exception e)` **捕获不到**
3. **追溯触发条件**：tess4j 加载语言包（tessdata）失败时，JNA 原生层抛 `Error: Invalid memory access`
4. **环境确认**：服务器/Docker 容器内可能未安装 tesseract-ocr 或语言包路径不正确

## C. 根因分析

**JNA 原生层异常类型不遵守 Java 异常体系**。

- Java 异常体系：`Error` 和 `Exception` 都继承自 `Throwable`，但 `catch(Exception)` 只能捕获 Exception 及其子类
- Tess4j 通过 JNA 调用 C++ 的 Tesseract 库，原生层内存访问失败时抛 `java.lang.Error`
- 代码中只有 `catch (TesseractException)` 和 `catch (Exception)`，Error 直接向上抛出
- 导致：OCR 识别失败 → Error 未捕获 → 凭证提交事务回滚 → 用户无法提交凭证

## D. 修复方案

```java
// 修复前
try {
    return tesseract.doOCR(image);
} catch (TesseractException e) {
    return null;
}
// Exception 捕获不到 Error，凭证提交失败

// 修复后
try {
    return tesseract.doOCR(image);
} catch (TesseractException e) {
    log.error("Tesseract OCR 识别异常, fileName={}: {}", fileName, e.getMessage());
    return null;
} catch (Throwable e) {
    // 捕获Throwable含Error：tess4j加载语言包失败时会抛java.lang.Error: Invalid memory access
    // catch(Exception)捕获不到，会导致整个凭证提交失败
    log.error("OCR处理异常(含Error), fileName={}: {}", fileName, e.toString());
    return null;
}
```

**同时增加前置防护**——`@PostConstruct` 初始化时探测 tessdata 路径，探测不到则 `available=false`，后续直接跳过 OCR：
```java
@PostConstruct
public void init() {
    // 探测不到tessdata目录 → available=false → recognizeText直接return null
    if (tessdataPath == null) {
        available = false;
        log.warn("未找到tessdata目录，OCR初筛功能不可用");
        return;
    }
}
```

## E. 验证方式

1. 服务器未安装 tesseract-ocr 时：启动日志输出 WARN，凭证提交正常（OCR 跳过）
2. tessdata 路径错误时：`recognizeText` 返回 null，凭证提交不报错
3. 正常安装时：OCR 识别金额/交易号，自动填充

## F. 影响范围

- 修复前：服务器无 tessdata 时，所有凭证提交都会失败（P1）
- 修复后：OCR 降级为可选功能，不影响主流程

## G. 经验教训

1. **JNA/JNI 调用必须 catch(Throwable)**：原生层不遵守 Java 异常体系，Error 会被 catch(Exception) 漏掉
2. **外部依赖必须优雅降级**：OCR 是辅助功能，不可用不应阻断核心业务
3. **前置检查优于后置容错**：`@PostConstruct` 探测环境可用性，运行时直接跳过，避免每次调用都触发异常
4. **初始化日志要明确**：available=false 时输出 WARN 级别日志，运维可快速定位

## H. 相关代码/文件

| 文件 | 说明 |
|------|------|
| backend/.../TesseractOcrServiceImpl.java | OCR 服务（修复后含 catch(Throwable) + available 标志 + 路径探测） |
| backend/.../PaymentProofServiceImpl.java | 凭证提交（调用 OCR，OCR 返回 null 时不影响流程） |