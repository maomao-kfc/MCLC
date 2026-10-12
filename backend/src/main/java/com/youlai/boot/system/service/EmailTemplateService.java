package com.youlai.boot.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.system.model.entity.EmailTemplate;
import com.youlai.boot.system.model.form.EmailTemplateForm;
import com.youlai.boot.system.model.query.EmailTemplateQuery;
import com.youlai.boot.system.model.vo.EmailTemplatePageVO;

import java.util.List;

public interface EmailTemplateService extends IService<EmailTemplate> {

    Page<EmailTemplatePageVO> getEmailTemplatePage(EmailTemplateQuery queryParams);

    EmailTemplateForm getEmailTemplateForm(String emailType);

    boolean saveEmailTemplate(EmailTemplateForm form);

    boolean updateEmailTemplate(Long id, EmailTemplateForm form);

    void deleteEmailTemplateByIds(List<String> ids);

}
