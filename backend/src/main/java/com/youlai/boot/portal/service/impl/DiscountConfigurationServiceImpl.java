package com.youlai.boot.portal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.portal.mapper.DiscountConfigurationMapper;
import com.youlai.boot.portal.model.entity.DiscountConfiguration;
import com.youlai.boot.portal.model.form.DiscountConfigurationForm;
import com.youlai.boot.portal.service.DiscountConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 折扣配置业务实现类
 *
 * @author 243269112
 * @since 2026/5/13
 */
@Service
@RequiredArgsConstructor
public class DiscountConfigurationServiceImpl extends ServiceImpl<DiscountConfigurationMapper, DiscountConfiguration> implements DiscountConfigurationService {

    @Override
    public List<DiscountConfiguration> getAll() {
        return this.list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveAll(DiscountConfigurationForm form) {
        this.remove(null);

        List<DiscountConfiguration> entities = new ArrayList<>();
        for (DiscountConfigurationForm.DiscountRule rule : form.getList()) {
            DiscountConfiguration entity = new DiscountConfiguration();
            entity.setMinPeople(rule.getMinPeople());
            entity.setMaxPeople(rule.getMaxPeople());
            entity.setDiscount(rule.getDiscount());
            entities.add(entity);
        }
        return this.saveBatch(entities);
    }
}
