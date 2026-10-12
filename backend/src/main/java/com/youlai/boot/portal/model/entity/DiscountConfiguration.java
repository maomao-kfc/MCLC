package com.youlai.boot.portal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 折扣配置实体
 *
 * @author 2432691112
 * @since 2026/5/13
 */
@TableName("discount_configuration")
@Getter
@Setter
public class DiscountConfiguration implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 最少人数
     */
    private Integer minPeople;

    /**
     * 最多人数
     */
    private Integer maxPeople;

    /**
     * 折扣
     */
    private BigDecimal discount;
}
