package com.youlai.boot.portal.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.portal.model.entity.Arrange;
import com.youlai.boot.portal.model.form.ArrangeForm;
import com.youlai.boot.portal.model.query.ArrangeQuery;
import com.youlai.boot.portal.model.vo.ArrangePageVO;

import java.util.List;


/**
 * 会前安排业务接口层
 *
 * @author 243269112
 * @since 2024/4/23
 */
public interface ArrangeService extends IService<Arrange> {

    /**
     * 会前安排分页列表
     *
     * @param queryParams
     * @return
     */
    Page<ArrangePageVO> getArrangePage(ArrangeQuery queryParams);

    /**
     *
     * @param ArrangeForm
     * @return
     */
    boolean saveArrange(ArrangeForm ArrangeForm);

    /**
     * 修改会前安排状态
     *
     * @param ArrangeId 会前安排ID
     * @param status 角色状态(1:启用；0:禁用)
     * @return {@link Boolean}
     */
    boolean updateArrangeStatus(Long ArrangeId, Integer status);

    /**
     * 批量删除会前安排
     *
     * @param ids 会前安排ID，多个使用英文逗号(,)分割
     */
    void deleteArranges(String ids);

    /**
     * 会前安排列表
     *
     * @param queryParams
     * @return
     */
    List<ArrangePageVO> getArrangeList(ArrangeQuery queryParams);
}
