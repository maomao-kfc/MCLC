package com.youlai.boot.system.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.core.exception.BusinessException;
import com.youlai.boot.system.converter.EmailTemplateConverter;
import com.youlai.boot.system.mapper.EmailTemplateMapper;
import com.youlai.boot.system.model.entity.EmailTemplate;
import com.youlai.boot.system.model.form.EmailTemplateForm;
import com.youlai.boot.system.model.query.EmailTemplateQuery;
import com.youlai.boot.system.model.vo.EmailTemplatePageVO;
import com.youlai.boot.system.service.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailTemplateServiceImpl extends ServiceImpl<EmailTemplateMapper, EmailTemplate> implements EmailTemplateService {

    private final EmailTemplateConverter emailTemplateConverter;

    @Override
    public Page<EmailTemplatePageVO> getEmailTemplatePage(EmailTemplateQuery queryParams) {
        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();
        return this.baseMapper.getEmailTemplatePage(new Page<>(pageNum, pageSize), queryParams);
    }

    @Override
    public EmailTemplateForm getEmailTemplateForm(String emailType) {
        EmailTemplate entity = this.getOne(new LambdaQueryWrapper<EmailTemplate>()
                .eq(EmailTemplate::getEmailType, emailType)
        );
        if (entity == null) {
            throw new BusinessException("邮箱模板不存在");
        }
        return emailTemplateConverter.toForm(entity);
    }

    @Override
    public boolean saveEmailTemplate(EmailTemplateForm form) {
        String title = form.getEmailTitle();
        long count = this.count(new LambdaQueryWrapper<EmailTemplate>()
                .eq(EmailTemplate::getEmailTitle, title)
        );
        Assert.isTrue(count == 0, "邮箱标题已存在");
        String emailType = form.getEmailType();
        long typeCount = this.count(new LambdaQueryWrapper<EmailTemplate>()
                .eq(EmailTemplate::getEmailType, emailType)
        );
        Assert.isTrue(typeCount == 0, "模板类型已存在");
        EmailTemplate entity = emailTemplateConverter.toEntity(form);
        return this.save(entity);
    }

    @Override
    @Transactional
    public boolean updateEmailTemplate(Long id, EmailTemplateForm form) {
        EmailTemplate entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("邮箱模板不存在");
        }
        String title = form.getEmailTitle();
        if (!entity.getEmailTitle().equals(title)) {
            long count = this.count(new LambdaQueryWrapper<EmailTemplate>()
                    .eq(EmailTemplate::getEmailTitle, title)
            );
            Assert.isTrue(count == 0, "邮箱标题已存在");
        }
        String emailType = form.getEmailType();
        if (!entity.getEmailType().equals(emailType)) {
            long typeCount = this.count(new LambdaQueryWrapper<EmailTemplate>()
                    .eq(EmailTemplate::getEmailType, emailType)
            );
            Assert.isTrue(typeCount == 0, "模板类型已存在");
        }
        EmailTemplate template = emailTemplateConverter.toEntity(form);
        template.setId(id);
        return this.updateById(template);
    }

    @Override
    @Transactional
    public void deleteEmailTemplateByIds(List<String> ids) {
        this.removeByIds(ids);
    }

}
