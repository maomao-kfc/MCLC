package com.youlai.boot.portal.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.core.exception.BusinessException;
import com.youlai.boot.portal.converter.ContactConverter;
import com.youlai.boot.portal.mapper.ContactMapper;
import com.youlai.boot.portal.model.entity.Contact;
import com.youlai.boot.portal.model.form.ContactForm;
import com.youlai.boot.portal.model.query.ContactQuery;
import com.youlai.boot.portal.model.vo.ContactPageVO;
import com.youlai.boot.portal.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 酒店信息业务实现类
 *
 * @author 243269112
 * @since 2024/4/23
 */
@Service
@RequiredArgsConstructor
public class ContactServiceImpl extends ServiceImpl<ContactMapper, Contact> implements ContactService {

    private final ContactConverter ContactConverter;

    /**
     * 酒店信息分页列表
     *
     * @param queryParams 酒店信息查询参数
     * @return {@link Page< ContactPageVO >} – 酒店信息分页列表
     */
    @Override
    public Page<ContactPageVO> getContactPage(ContactQuery queryParams) {
        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();
        String keywords = queryParams.getContactName();
        String title = queryParams.getTitle();
        String countryType = queryParams.getCountryType();

        LambdaQueryWrapper<Contact> queryWrapper = new LambdaQueryWrapper<Contact>()
                .and(StrUtil.isNotBlank(keywords),
                    wrapper ->
                        wrapper.like(Contact::getContactName, keywords)
                )
                .and(StrUtil.isNotBlank(title),
                    wrapper ->
                        wrapper.like(Contact::getTitle, title)
                )
                .orderByAsc(Contact::getSort).orderByDesc(Contact::getCreateTime).orderByDesc(Contact::getUpdateTime);
        if (StrUtil.isNotBlank(countryType)) {
            queryWrapper.eq(Contact::getCountryType, countryType);
        }
        Page<Contact> ContactPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);

        // 实体转换
        return ContactConverter.toPageVo(ContactPage);
    }


    /**
     * 保存酒店信息
     *
     * @param ContactForm 酒店信息表单数据
     * @return {@link Boolean}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveContact(ContactForm ContactForm) {

        Long ContactId = ContactForm.getId();

        // 编辑角色时，判断角色是否存在
        Contact oldContact = null;
        if (ContactId != null) {
            oldContact = this.getById(ContactId);
            Assert.isTrue(oldContact != null, "酒店信息不存在");
        }

        // 实体转换
        Contact Contact = ContactConverter.toEntity(ContactForm);

        return this.saveOrUpdate(Contact);
    }


    /**
     * 修改酒店信息状态
     *
     * @param ContactId 酒店信息ID
     * @param status 角色状态(1:启用；0:禁用)
     * @return {@link Boolean}
     */
    @Override
    public boolean updateContactStatus(Long ContactId, Integer status) {

        Contact Contact = this.getById(ContactId);
        if (Contact == null) {
            throw new BusinessException("酒店信息不存在");
        }

        Contact.setStatus(status);

        return this.updateById(Contact);
    }

    /**
     * 批量删除酒店信息
     *
     * @param ids 酒店信息ID，多个使用英文逗号(,)分割
     */
    @Override
    public void deleteContacts(String ids) {
        Assert.isTrue(StrUtil.isNotBlank(ids), "删除的酒店信息ID不能为空");
        List<Long> ContactIds = Arrays.stream(ids.split(","))
            .map(Long::parseLong)
            .toList();

        for (Long ContactId : ContactIds) {
            Contact Contact = this.getById(ContactId);
            Assert.isTrue(Contact != null, "酒店信息不存在");

            boolean deleteResult = this.removeById(ContactId);
        }
    }


    /**
     * 酒店信息列表
     *
     * @param queryParams 酒店信息查询参数
     * @return {@link Page< ContactPageVO >} – 酒店信息列表
     */
    @Override
    public List<ContactPageVO> getContactList(ContactQuery queryParams) {

        // 查询数据
        LambdaQueryWrapper<Contact> queryWrapper = new LambdaQueryWrapper<Contact>()
                .eq(Contact::getStatus, 1)
                .orderByAsc(Contact::getSort).orderByDesc(Contact::getCreateTime).orderByDesc(Contact::getUpdateTime);
        if (queryParams.getCountryType() != null && !queryParams.getCountryType().isEmpty()) {
            queryWrapper.eq(Contact::getCountryType, queryParams.getCountryType());
        }
        List<Contact> contactList = this.list(queryWrapper);
        // 实体转换
        return ContactConverter.toPageVo(contactList);
    }


}
