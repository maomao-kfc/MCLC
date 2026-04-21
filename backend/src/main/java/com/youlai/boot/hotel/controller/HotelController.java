package com.youlai.boot.hotel.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.youlai.boot.core.web.Result;
import com.youlai.boot.hotel.model.dto.HotelPageQuery;
import com.youlai.boot.hotel.model.entity.Hotel;
import com.youlai.boot.hotel.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 酒店控制层
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "酒店控制层")
@RequestMapping("/api/v1/hotel")
public class HotelController {
    private final HotelService hotelService;

    @Operation(summary = "分页查询酒店列表")
    @GetMapping("/page")
    public Result<IPage<Hotel>> getHotelPage(@Valid HotelPageQuery query) {
        IPage<Hotel> page = hotelService.lambdaQuery()
            .like(query.getName() != null, Hotel::getName, query.getName())
            .eq(query.getStatus() != null, Hotel::getStatus, query.getStatus())
            .page(query.toPage());  // 使用 BaseQuery 的页码和每页大小
        return Result.success(page);
    }
}
