package com.youlai.boot.portal.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.portal.model.entity.QuestionnaireSurvey;
import com.youlai.boot.portal.model.entity.RegisterUserInformation;
import com.youlai.boot.portal.model.form.RegisterUserForm;
import com.youlai.boot.portal.model.vo.RegisterUserPageVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RegisterUserConverter {

    RegisterUserPageVO toPageVo(RegisterUserPageVO vo);

    Page<RegisterUserPageVO> toPageVo(Page<RegisterUserPageVO> page);

    RegisterUserInformation toRegisterUserInformation(RegisterUserForm form);

    QuestionnaireSurvey toQuestionnaireSurvey(RegisterUserForm form);

    List<RegisterUserPageVO> toPageVo(List<RegisterUserPageVO> list);
}
