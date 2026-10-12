package com.youlai.boot.system.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.system.model.entity.EmailTemplate;
import com.youlai.boot.system.model.form.EmailTemplateForm;
import com.youlai.boot.system.model.vo.EmailTemplatePageVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmailTemplateConverter {

    Page<EmailTemplatePageVO> toPageVo(Page<EmailTemplate> page);

    EmailTemplateForm toForm(EmailTemplate entity);

    EmailTemplate toEntity(EmailTemplateForm form);

}
