package com.youlai.boot.portal.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.portal.model.entity.Contact;
import com.youlai.boot.portal.model.form.ContactForm;
import com.youlai.boot.portal.model.query.ContactQuery;
import com.youlai.boot.portal.model.vo.ContactPageVO;

import java.util.List;


/**
 * 联系人业务接口层
 *
 * @author 243269112
 * @since 2024/4/23
 */
public interface ContactService extends IService<Contact> {

    /**
     * 联系人分页列表
     *
     * @param queryParams
     * @return
     */
    Page<ContactPageVO> getContactPage(ContactQuery queryParams);

    /**
     *
     * @param ContactForm
     * @return
     */
    boolean saveContact(ContactForm ContactForm);

    /**
     * 修改联系人状态
     *
     * @param ContactId 联系人ID
     * @param status 角色状态(1:启用；0:禁用)
     * @return {@link Boolean}
     */
    boolean updateContactStatus(Long ContactId, Integer status);

    /**
     * 批量删除联系人
     *
     * @param ids 联系人ID，多个使用英文逗号(,)分割
     */
    void deleteContacts(String ids);

    /**
     * 联系人列表
     *
     * @param queryParams
     * @return
     */
    List<ContactPageVO> getContactList(ContactQuery queryParams);
}
