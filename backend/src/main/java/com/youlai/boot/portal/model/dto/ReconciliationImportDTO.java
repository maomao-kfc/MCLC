package com.youlai.boot.portal.model.dto;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 协会对账 Excel 导入 DTO
 * <p>列名支持常见中文表头，导入时按 @ExcelProperty 匹配</p>
 * <p>如协会实际表头不同，可在导入时让用户选择列映射（当前版本按固定表头解析）</p>
 */
@Data
public class ReconciliationImportDTO {

    @ExcelProperty("付款人姓名")
    private String name;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("金额")
    private BigDecimal amount;

    @ExcelProperty("支付时间")
    private String payTime;

    @ExcelProperty("交易号")
    private String tradeNo;

    @ExcelProperty("备注")
    private String remark;
}