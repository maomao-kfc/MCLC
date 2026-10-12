package com.youlai.boot.portal.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.core.exception.BusinessException;
import com.youlai.boot.portal.converter.ArchitectureConverter;
import com.youlai.boot.portal.mapper.ArchitectureMapper;
import com.youlai.boot.portal.model.entity.Architecture;
import com.youlai.boot.portal.model.form.ArchitectureForm;
import com.youlai.boot.portal.model.query.ArchitectureQuery;
import com.youlai.boot.portal.model.vo.ArchitecturePageVO;
import com.youlai.boot.portal.service.ArchitectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 组织架构业务实现类
 *
 * @author 243269112
 * @since 2026/5/25
 */
@Service
@RequiredArgsConstructor
public class ArchitectureServiceImpl extends ServiceImpl<ArchitectureMapper, Architecture> implements ArchitectureService {

    private final ArchitectureConverter architectureConverter;

    /**
     * 组织架构分页列表
     *
     * @param queryParams 组织架构查询参数
     * @return {@link Page< ArchitecturePageVO >} – 组织架构分页列表
     */
    @Override
    public Page<ArchitecturePageVO> getArchitecturePage(ArchitectureQuery queryParams) {
        // 查询参数
        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();
        String keywords = queryParams.getName();
        String job = queryParams.getJob();
        String countryType = queryParams.getCountryType();

        // 查询数据
        LambdaQueryWrapper<Architecture> queryWrapper = new LambdaQueryWrapper<Architecture>()
                .and(StrUtil.isNotBlank(keywords),
                    wrapper ->
                        wrapper.like(Architecture::getName, keywords)
                )
                .orderByAsc(Architecture::getSort).orderByDesc(Architecture::getCreateTime).orderByDesc(Architecture::getUpdateTime);
        if (StrUtil.isNotBlank(job)) {
            queryWrapper.like(Architecture::getJob, job);
        }
        if (StrUtil.isNotBlank(countryType)) {
            queryWrapper.eq(Architecture::getCountryType, countryType);
        }
        Page<Architecture> architecturePage = this.page(new Page<>(pageNum, pageSize), queryWrapper);

        // 实体转换
        return architectureConverter.toPageVo(architecturePage);
    }


    /**
     * 保存组织架构
     *
     * @param architectureForm 组织架构表单数据
     * @return {@link Boolean}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveArchitecture(ArchitectureForm architectureForm) {

        Long architectureId = architectureForm.getId();

        // 编辑角色时，判断角色是否存在
        Architecture oldArchitecture = null;
        if (architectureId != null) {
            oldArchitecture = this.getById(architectureId);
            Assert.isTrue(oldArchitecture != null, "组织架构不存在");
        }

        // 实体转换
        Architecture architecture = architectureConverter.toEntity(architectureForm);

        return this.saveOrUpdate(architecture);
    }


    /**
     * 修改组织架构状态
     *
     * @param architectureId 组织架构ID
     * @param status 角色状态(1:启用；0:禁用)
     * @return {@link Boolean}
     */
    @Override
    public boolean updateArchitectureStatus(Long architectureId, Integer status) {

        Architecture architecture = this.getById(architectureId);
        if (architecture == null) {
            throw new BusinessException("组织架构不存在");
        }

        architecture.setStatus(status);

        return this.updateById(architecture);
    }

    /**
     * 批量删除组织架构
     *
     * @param ids 组织架构ID，多个使用英文逗号(,)分割
     */
    @Override
    public void deleteArchitectures(String ids) {
        Assert.isTrue(StrUtil.isNotBlank(ids), "删除的组织架构ID不能为空");
        List<Long> architectureIds = Arrays.stream(ids.split(","))
            .map(Long::parseLong)
            .toList();

        for (Long architectureId : architectureIds) {
            Architecture architecture = this.getById(architectureId);
            Assert.isTrue(architecture != null, "组织架构不存在");

            boolean deleteResult = this.removeById(architectureId);
        }
    }


    /**
     * 组织架构列表
     *
     * @param queryParams 组织架构查询参数
     * @return {@link Page< ArchitecturePageVO >} – 组织架构列表
     */
    @Override
    public List<ArchitecturePageVO> getArchitectureList(ArchitectureQuery queryParams) {

        // 查询数据
        LambdaQueryWrapper<Architecture> queryWrapper = new LambdaQueryWrapper<Architecture>()
                .eq(Architecture::getStatus, 1)
                .orderByAsc(Architecture::getSort).orderByDesc(Architecture::getCreateTime).orderByDesc(Architecture::getUpdateTime);
        List<Architecture> architectureList = this.list(queryWrapper);

        // 实体转换
        return architectureConverter.toPageVo(architectureList);
    }


}
