package com.youlai.boot.portal.payment.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youlai.boot.config.property.AlipayProperties;
import com.youlai.boot.portal.enums.PaymentMethodEnum;
import com.youlai.boot.portal.mapper.PaymentOrderMapper;
import com.youlai.boot.portal.model.entity.PaymentOrder;
import com.youlai.boot.portal.payment.CreatePaymentRequest;
import com.youlai.boot.portal.payment.PaymentProvider;
import com.youlai.boot.portal.payment.PaymentProviderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlipayPaymentProvider implements PaymentProvider {

    private final AlipayClient alipayClient;
    private final AlipayProperties alipayProperties;
    private final PaymentOrderMapper paymentOrderMapper;

    @Override
    public PaymentMethodEnum supportedMethod() {
        return PaymentMethodEnum.ALIPAY;
    }

    @Override
    public PaymentProviderResponse createPayment(CreatePaymentRequest request) {
        String orderNo = request.getRegistrationId() != null
                ? "ALI" + System.currentTimeMillis() + StrUtil.sub(String.valueOf(request.getRegistrationId()), -4, 0)
                : "ALI" + System.currentTimeMillis();

        PaymentOrder order = new PaymentOrder();
        order.setRegistrationId(request.getRegistrationId());
        order.setPaymentMethod(PaymentMethodEnum.ALIPAY.getCode());
        order.setOrderNo(orderNo);
        order.setAmount(request.getAmount().toPlainString());
        order.setCurrency(request.getCurrency());
        order.setStatus("pending");
        order.setCreateTime(LocalDateTime.now());
        order.setExpireTime(LocalDateTime.now().plusMinutes(30));
        paymentOrderMapper.insert(order);

        if ("h5".equals(request.getScene())) {
            return createH5Payment(request, orderNo, order);
        }
        return createScanPayment(request, orderNo, order);
    }

    private PaymentProviderResponse createScanPayment(CreatePaymentRequest request, String orderNo, PaymentOrder order) {
        try {
            AlipayTradePrecreateRequest precreateRequest = new AlipayTradePrecreateRequest();
            precreateRequest.setNotifyUrl(alipayProperties.getNotifyUrl());

            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", orderNo);
            bizContent.put("total_amount", request.getAmount().setScale(2, RoundingMode.HALF_UP).toString());
            bizContent.put("subject", "会议注册费");
            if (request.getRegistrationId() != null) {
                bizContent.put("body", "注册用户ID: " + request.getRegistrationId());
            } else {
                bizContent.put("body", "会议注册费");
            }
            precreateRequest.setBizContent(JSONUtil.toJsonStr(bizContent));

            AlipayTradePrecreateResponse response = alipayClient.execute(precreateRequest);

            if (!response.isSuccess()) {
                throw new RuntimeException("支付宝预下单失败: " + response.getSubMsg());
            }

            String qrCode = response.getQrCode();
            order.setQrCodeUrl(qrCode);
            paymentOrderMapper.updateById(order);

            return PaymentProviderResponse.builder()
                    .orderNo(orderNo)
                    .qrCodeUrl(qrCode)
                    .paymentStatus("pending")
                    .amount(request.getAmount().toPlainString())
                    .build();
        } catch (AlipayApiException e) {
            log.error("支付宝扫码付创建失败", e);
            String errMsg = StrUtil.isNotBlank(e.getErrMsg()) ? e.getErrMsg() : e.getMessage();
            throw new RuntimeException("支付宝创建支付失败: " + errMsg);
        }
    }

    private PaymentProviderResponse createH5Payment(CreatePaymentRequest request, String orderNo, PaymentOrder order) {
        try {
            AlipayTradeWapPayRequest wapRequest = new AlipayTradeWapPayRequest();
            wapRequest.setNotifyUrl(alipayProperties.getNotifyUrl());
            wapRequest.setReturnUrl(alipayProperties.getReturnUrl());

            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", orderNo);
            bizContent.put("total_amount", request.getAmount().setScale(2, RoundingMode.HALF_UP).toString());
            bizContent.put("subject", "会议注册费");
            bizContent.put("product_code", "QUICK_WAP_WAY");
            wapRequest.setBizContent(JSONUtil.toJsonStr(bizContent));

            String payUrl = alipayClient.pageExecute(wapRequest).getBody();

            order.setPayUrl(payUrl);
            paymentOrderMapper.updateById(order);

            return PaymentProviderResponse.builder()
                    .orderNo(orderNo)
                    .payUrl(payUrl)
                    .paymentStatus("pending")
                    .amount(request.getAmount().toPlainString())
                    .build();
        } catch (AlipayApiException e) {
            log.error("支付宝H5支付创建失败", e);
            String errMsg = StrUtil.isNotBlank(e.getErrMsg()) ? e.getErrMsg() : e.getMessage();
            throw new RuntimeException("支付宝创建支付失败: " + errMsg);
        }
    }

    @Override
    public PaymentProviderResponse queryPayment(String orderNo) {
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", orderNo);
            request.setBizContent(JSONUtil.toJsonStr(bizContent));

            AlipayTradeQueryResponse response = alipayClient.execute(request);

            if ("TRADE_SUCCESS".equals(response.getTradeStatus())) {
                PaymentOrder order = paymentOrderMapper.selectOne(
                        new LambdaQueryWrapper<PaymentOrder>().eq(PaymentOrder::getOrderNo, orderNo));
                if (order != null && "pending".equals(order.getStatus())) {
                    order.setStatus("paid");
                    order.setTransactionId(response.getTradeNo());
                    order.setPaidTime(LocalDateTime.now());
                    paymentOrderMapper.updateById(order);
                }
            }

            return PaymentProviderResponse.builder()
                    .orderNo(orderNo)
                    .transactionId(response.getTradeNo())
                    .paymentStatus(convertStatus(response.getTradeStatus()))
                    .build();
        } catch (AlipayApiException e) {
            log.error("支付宝查询支付失败", e);
            String errMsg = StrUtil.isNotBlank(e.getErrMsg()) ? e.getErrMsg() : e.getMessage();
            throw new RuntimeException("支付宝查询失败: " + errMsg);
        }
    }

    @Override
    public String handleCallback(Map<String, String> params) {
        try {
            Map<String, String> verifyParams = new HashMap<>(params);
            String sign = verifyParams.remove("sign");
            String signType = verifyParams.remove("sign_type");
            String[] keys = verifyParams.keySet().toArray(new String[0]);
            java.util.Arrays.sort(keys);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < keys.length; i++) {
                if (i > 0) sb.append("&");
                sb.append(keys[i]).append("=").append(verifyParams.get(keys[i]));
            }
            String content = sb.toString();

            java.security.spec.X509EncodedKeySpec keySpec = new java.security.spec.X509EncodedKeySpec(
                    java.util.Base64.getDecoder().decode(alipayProperties.getAlipayPublicKey()));
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
            java.security.PublicKey pubKey = keyFactory.generatePublic(keySpec);
            java.security.Signature sig = java.security.Signature.getInstance("SHA256withRSA");
            sig.initVerify(pubKey);
            sig.update(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            boolean verified = sig.verify(java.util.Base64.getDecoder().decode(sign));

            if (!verified) {
                log.warn("支付宝回调验签失败");
                return null;
            }

            String orderNo = params.get("out_trade_no");
            String tradeStatus = params.get("trade_status");
            String tradeNo = params.get("trade_no");

            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                PaymentOrder order = paymentOrderMapper.selectOne(
                        new LambdaQueryWrapper<PaymentOrder>().eq(PaymentOrder::getOrderNo, orderNo));
                if (order != null && "pending".equals(order.getStatus())) {
                    order.setStatus("paid");
                    order.setTransactionId(tradeNo);
                    order.setPaidTime(LocalDateTime.now());
                    paymentOrderMapper.updateById(order);
                    log.info("支付宝支付成功: orderNo={}, tradeNo={}", orderNo, tradeNo);
                }
                return orderNo;
            }
            return null;
        } catch (Exception e) {
            log.error("处理支付宝回调失败", e);
            return null;
        }
    }

    @Override
    public void refund(String orderNo) {
        try {
            PaymentOrder order = paymentOrderMapper.selectOne(
                    new LambdaQueryWrapper<PaymentOrder>().eq(PaymentOrder::getOrderNo, orderNo));
            if (order == null || !"paid".equals(order.getStatus())) {
                throw new RuntimeException("订单状态不允许退款");
            }

            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", orderNo);
            bizContent.put("refund_amount", order.getAmount());
            request.setBizContent(JSONUtil.toJsonStr(bizContent));

            AlipayTradeRefundResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                order.setStatus("refunded");
                paymentOrderMapper.updateById(order);
                log.info("支付宝退款成功: orderNo={}", orderNo);
            } else {
                throw new RuntimeException("支付宝退款失败: " + response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            log.error("支付宝退款失败", e);
            String errMsg = StrUtil.isNotBlank(e.getErrMsg()) ? e.getErrMsg() : e.getMessage();
            throw new RuntimeException("支付宝退款失败: " + errMsg);
        }
    }

    private String convertStatus(String tradeStatus) {
        if (tradeStatus == null) return "pending";
        switch (tradeStatus) {
            case "TRADE_SUCCESS":
            case "TRADE_FINISHED":
                return "paid";
            case "TRADE_CLOSED":
                return "closed";
            default:
                return "pending";
        }
    }

}
