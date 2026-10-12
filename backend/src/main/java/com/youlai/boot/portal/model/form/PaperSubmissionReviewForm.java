package com.youlai.boot.portal.model.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 论文投稿审核表单
 *
 * @author 2432691112
 * @since 2026/5/13
 */
@Data
public class PaperSubmissionReviewForm {

    @NotEmpty(message = "专家列表不能为空")
    @Valid
    private List<ExpertEmailItem> emailsList;

    private String url;

    @NotBlank(message = "发送人姓名不能为空")
    private String senderName;

    @Data
    public static class ExpertEmailItem {

        @NotBlank(message = "专家姓名不能为空")
        private String name;

        @NotBlank(message = "专家邮箱不能为空")
        private String email;
    }
}
