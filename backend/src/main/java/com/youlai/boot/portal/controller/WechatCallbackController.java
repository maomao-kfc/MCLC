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

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Tag(name = "微信支付回调")
@RestController
@RequestMapping("/api/v1/payment/wechat")
@RequiredArgsConstructor
public class WechatCallbackController {

    private final PaymentRouter paymentRouter;
    private final PaymentService paymentService;

    @Operation(summary = "微信支付异步通知")
    @PostMapping("/notify")
    public String handleNotify(HttpServletRequest request) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("wechatpaySignature", request.getHeader("Wechatpay-Signature"));
            params.put("wechatpayNonce", request.getHeader("Wechatpay-Nonce"));
            params.put("wechatpayTimestamp", request.getHeader("Wechatpay-Timestamp"));
            params.put("wechatpaySerial", request.getHeader("Wechatpay-Serial"));

            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String body = sb.toString();
            params.put("body", body);

            String orderNo = paymentRouter.routeByCallback(PaymentMethodEnum.WECHAT.getCode())
                    .handleCallback(params);

            if (orderNo != null) {
                paymentService.syncRegisterUserByOrderNo(orderNo);
            }

            return orderNo != null ? "SUCCESS" : "FAIL";
        } catch (Exception e) {
            log.error("微信支付回调处理异常", e);
            return "FAIL";
        }
    }
}
