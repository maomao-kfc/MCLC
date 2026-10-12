package com.youlai.boot.portal.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.common.model.Option;
import com.youlai.boot.portal.model.entity.Contact;
import com.youlai.boot.portal.model.form.ContactForm;
import com.youlai.boot.portal.model.vo.ContactPageVO;
import com.youlai.boot.system.model.bo.UserBO;
import com.youlai.boot.system.model.entity.DictItem;
import com.youlai.boot.system.model.vo.UserPageVO;
import org.mapstruct.Mapper;

import java.util.List;


/**
 * 酒店信息对象转换器
 *
 * @author 243269112
 * @since 2026/4/23
 */
@Mapper(componentModel = "spring")
public interface ContactConverter {

    ContactPageVO toPageVo(Contact contact);

    Page<ContactPageVO> toPageVo(Page<Contact> page);

    Contact toEntity(ContactForm ContactForm);

    ContactForm toForm(Contact entity);

    List<ContactPageVO> toPageVo(List<Contact> bo);
}
