package com.youlai.boot.portal.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.portal.model.entity.Guest;
import com.youlai.boot.portal.model.form.GuestForm;
import com.youlai.boot.portal.model.vo.GuestPageVO;
import org.mapstruct.Mapper;

import java.util.List;


/**
 * 特邀嘉宾对象转换器
 *
 * @author 243269112
 * @since 2026/4/22
 */
@Mapper(componentModel = "spring")
public interface GuestConverter {

    GuestPageVO toPageVo(Guest guest);

    Page<GuestPageVO> toPageVo(Page<Guest> page);

    Guest toEntity(GuestForm guestorm);

    GuestForm toForm(Guest entity);

    List<GuestPageVO> toPageVo(List<Guest> bo);
}
