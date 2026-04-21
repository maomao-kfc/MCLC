package com.youlai.boot.hotel.model.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.youlai.boot.common.base.BaseQuery;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "酒店分页查询参数")
public class HotelPageQuery extends BaseQuery {

    @Schema(description = "酒店名称")
    private String name;

    @Schema(description = "状态：1-正常，0-已取消合作")
    private Integer status;

    /**
     * 转换为 MyBatis-Plus 分页对象
     */
    public <T> Page<T> toPage() {
        return new Page<>(getPageNum(), getPageSize());
    }
}
