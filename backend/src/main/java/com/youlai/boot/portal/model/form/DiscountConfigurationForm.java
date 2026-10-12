package com.youlai.boot.portal.model.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 折扣配置表单
 *
 * @author 2432691112
 * @since 2026/5/13
 */
@Data
public class DiscountConfigurationForm {

    @NotEmpty(message = "规则列表不能为空")
    @Valid
    private List<DiscountRule> list;

    @Data
    public static class DiscountRule {

        @NotNull(message = "最少人数不能为空")
        private Integer minPeople;

        @NotNull(message = "最多人数不能为空")
        private Integer maxPeople;

        @NotNull(message = "折扣不能为空")
        private BigDecimal discount;
    }
}
