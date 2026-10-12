package com.youlai.boot.portal.controller;

import com.youlai.boot.portal.enums.PaymentMethodEnum;
import com.youlai.boot.portal.payment.PaymentRouter;
import com.youlai.boot.portal.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Tag(name = "支付宝支付回调")
@RestController
@RequestMapping("/api/v1/payment/alipay")
@RequiredArgsConstructor
public class AlipayCallbackController {

    private final PaymentRouter paymentRouter;
    private final PaymentService paymentService;

    @Operation(summary = "支付宝异步通知")
    @PostMapping("/notify")
    public String handleNotify(HttpServletRequest request) {
        try {
            Map<String, String> params = new HashMap<>();
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String name = paramNames.nextElement();
                params.put(name, request.getParameter(name));
            }

            String orderNo = paymentRouter.routeByCallback(PaymentMethodEnum.ALIPAY.getCode())
                    .handleCallback(params);

            if (orderNo != null) {
                paymentService.syncRegisterUserByOrderNo(orderNo);
            }

            return orderNo != null ? "success" : "fail";
        } catch (Exception e) {
            log.error("支付宝回调处理异常", e);
            return "fail";
        }
    }
}
