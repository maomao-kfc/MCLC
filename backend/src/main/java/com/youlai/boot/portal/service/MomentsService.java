package com.youlai.boot.portal.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.portal.model.entity.Moments;
import com.youlai.boot.portal.model.form.MomentsForm;
import com.youlai.boot.portal.model.query.MomentsQuery;
import com.youlai.boot.portal.model.vo.MomentsPageVO;

import java.util.List;


/**
 * 精彩瞬间业务接口层
 *
 * @author 243269112
 * @since 2024/4/23
 */
public interface MomentsService extends IService<Moments> {

    /**
     * 精彩瞬间分页列表
     *
     * @param queryParams
     * @return
     */
    Page<MomentsPageVO> getMomentsPage(MomentsQuery queryParams);

    /**
     *
     * @param momentsForm
     * @return
     */
    boolean saveMoments(MomentsForm momentsForm);

    /**
     * 修改精彩瞬间状态
     *
     * @param momentsId 精彩瞬间ID
     * @param status 角色状态(1:启用；0:禁用)
     * @return {@link Boolean}
     */
    boolean updateMomentsStatus(Long momentsId, Integer status);

    /**
     * 批量删除精彩瞬间
     *
     * @param ids 精彩瞬间ID，多个使用英文逗号(,)分割
     */
    void deleteMomentss(String ids);

    /**
     * 精彩瞬间列表
     *
     * @param queryParams
     * @return
     */
    List<MomentsPageVO> getMomentsList(MomentsQuery queryParams);
}
