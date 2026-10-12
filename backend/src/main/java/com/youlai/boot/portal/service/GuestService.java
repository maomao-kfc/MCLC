package com.youlai.boot.portal.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.portal.model.entity.Guest;
import com.youlai.boot.portal.model.form.GuestForm;
import com.youlai.boot.portal.model.query.GuestQuery;
import com.youlai.boot.portal.model.vo.GuestPageVO;

import java.util.List;


/**
 * 特邀嘉宾业务接口层
 *
 * @author 243269112
 * @since 2024/4/22
 */
public interface GuestService extends IService<Guest> {

    /**
     * 特邀嘉宾分页列表
     *
     * @param queryParams
     * @return
     */
    Page<GuestPageVO> getGuestPage(GuestQuery queryParams);

    /**
     *
     * @param guestForm
     * @return
     */
    boolean saveGuest(GuestForm guestForm);

    /**
     * 修改特邀嘉宾状态
     *
     * @param gusetId 角色ID
     * @param status 角色状态(1:启用；0:禁用)
     * @return {@link Boolean}
     */
    boolean updateGuestStatus(Long gusetId, Integer status);

    /**
     * 批量删除特邀嘉宾
     *
     * @param ids 特邀嘉宾ID，多个使用英文逗号(,)分割
     */
    void deleteGuests(String ids);

    /**
     * 特邀嘉宾列表
     *
     * @param queryParams
     * @return
     */
    List<GuestPageVO> getGuestList(GuestQuery queryParams);
}
