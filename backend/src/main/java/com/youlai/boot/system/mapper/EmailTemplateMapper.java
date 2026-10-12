package com.youlai.boot.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.system.model.entity.EmailTemplate;
import com.youlai.boot.system.model.query.EmailTemplateQuery;
import com.youlai.boot.system.model.vo.EmailTemplatePageVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmailTemplateMapper extends BaseMapper<EmailTemplate> {

    Page<EmailTemplatePageVO> getEmailTemplatePage(Page<EmailTemplatePageVO> page, EmailTemplateQuery queryParams);

}
