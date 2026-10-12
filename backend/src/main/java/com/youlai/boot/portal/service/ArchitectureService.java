package com.youlai.boot.portal.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.portal.model.entity.Architecture;
import com.youlai.boot.portal.model.form.ArchitectureForm;
import com.youlai.boot.portal.model.query.ArchitectureQuery;
import com.youlai.boot.portal.model.vo.ArchitecturePageVO;

import java.util.List;


/**
 * 组织架构业务接口层
 *
 * @author 243269112
 * @since 2026/5/25
 */
public interface ArchitectureService extends IService<Architecture> {

    /**
     * 组织架构分页列表
     *
     * @param queryParams
     * @return
     */
    Page<ArchitecturePageVO> getArchitecturePage(ArchitectureQuery queryParams);

    /**
     *
     * @param architectureForm
     * @return
     */
    boolean saveArchitecture(ArchitectureForm architectureForm);

    /**
     * 修改组织架构状态
     *
     * @param architectureId 组织架构ID
     * @param status 角色状态(1:启用；0:禁用)
     * @return {@link Boolean}
     */
    boolean updateArchitectureStatus(Long architectureId, Integer status);

    /**
     * 批量删除组织架构
     *
     * @param ids 组织架构ID，多个使用英文逗号(,)分割
     */
    void deleteArchitectures(String ids);

    /**
     * 组织架构列表
     *
     * @param queryParams
     * @return
     */
    List<ArchitecturePageVO> getArchitectureList(ArchitectureQuery queryParams);
}
