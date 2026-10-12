package com.youlai.boot.portal.service;

import com.youlai.boot.portal.model.entity.DiscountConfiguration;
import com.youlai.boot.portal.model.form.DiscountConfigurationForm;

import java.util.List;

/**
 * 折扣配置业务接口层
 *
 * @author 243269112
 * @since 2026/5/13
 */
public interface DiscountConfigurationService {

    List<DiscountConfiguration> getAll();

    boolean saveAll(DiscountConfigurationForm form);
}
