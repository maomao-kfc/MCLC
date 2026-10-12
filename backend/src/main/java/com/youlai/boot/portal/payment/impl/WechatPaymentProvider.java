package com.youlai.boot.portal.payment.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.h5.H5Service;
import com.wechat.pay.java.service.payments.h5.model.H5Info;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.youlai.boot.config.property.WechatPayProperties;
import com.youlai.boot.portal.enums.PaymentMethodEnum;
import com.youlai.boot.portal.mapper.PaymentOrderMapper;
import com.youlai.boot.portal.model.entity.PaymentOrder;
import com.youlai.boot.portal.payment.CreatePaymentRequest;
import com.youlai.boot.portal.payment.PaymentProvider;
import com.youlai.boot.portal.payment.PaymentProviderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WechatPaymentProvider implements PaymentProvider {

    private final WechatPayProperties wechatPayProperties;
    private final NativePayService nativePayService;
    private final H5Service h5Service;
    private final RefundService refundService;
    private final NotificationParser notificationParser;
    private final PaymentOrderMapper paymentOrderMapper;

    @Override
    public PaymentMethodEnum supportedMethod() {
        return PaymentMethodEnum.WECHAT;
    }

    @Override
    public PaymentProviderResponse createPayment(CreatePaymentRequest request) {
        String orderNo = request.getRegistrationId() != null
                ? "WXP" + System.currentTimeMillis() + StrUtil.sub(request.getRegistrationId().toString(), -4, 0)
                : "WXP" + System.currentTimeMillis();

        PaymentOrder order = new PaymentOrder();
        order.setRegistrationId(request.getRegistrationId());
        order.setPaymentMethod(PaymentMethodEnum.WECHAT.getCode());
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
        return createNativePayment(request, orderNo, order);
    }

    private PaymentProviderResponse createNativePayment(CreatePaymentRequest request, String orderNo, PaymentOrder order) {
        PrepayRequest payRequest = new PrepayRequest();
        payRequest.setAppid(wechatPayProperties.getAppId());
        payRequest.setMchid(wechatPayProperties.getMchId());
        payRequest.setDescription("会议注册费");
        payRequest.setOutTradeNo(orderNo);
        payRequest.setNotifyUrl(wechatPayProperties.getNotifyUrl());
        Amount amount = new Amount();
        amount.setTotal(Math.round(request.getAmount().floatValue() * 100));
        amount.setCurrency(request.getCurrency().toUpperCase());
        payRequest.setAmount(amount);

        PrepayResponse response = nativePayService.prepay(payRequest);
        String codeUrl = response.getCodeUrl();

        order.setQrCodeUrl(codeUrl);
        paymentOrderMapper.updateById(order);

        return PaymentProviderResponse.builder()
                .orderNo(orderNo)
                .qrCodeUrl(codeUrl)
                .paymentStatus("pending")
                .amount(request.getAmount().toPlainString())
                .build();
    }

    private PaymentProviderResponse createH5Payment(CreatePaymentRequest request, String orderNo, PaymentOrder order) {
        com.wechat.pay.java.service.payments.h5.model.PrepayRequest payRequest =
                new com.wechat.pay.java.service.payments.h5.model.PrepayRequest();
        payRequest.setAppid(wechatPayProperties.getAppId());
        payRequest.setMchid(wechatPayProperties.getMchId());
        payRequest.setDescription("会议注册费");
        payRequest.setOutTradeNo(orderNo);
        payRequest.setNotifyUrl(wechatPayProperties.getNotifyUrl());
        com.wechat.pay.java.service.payments.h5.model.Amount amount =
                new com.wechat.pay.java.service.payments.h5.model.Amount();
        amount.setTotal(Math.round(request.getAmount().floatValue() * 100));
        amount.setCurrency(request.getCurrency().toUpperCase());
        payRequest.setAmount(amount);

        com.wechat.pay.java.service.payments.h5.model.SceneInfo sceneInfo =
                new com.wechat.pay.java.service.payments.h5.model.SceneInfo();
        H5Info h5Info = new H5Info();
        h5Info.setType("Wap");
        sceneInfo.setH5Info(h5Info);
        payRequest.setSceneInfo(sceneInfo);

        String h5Url = h5Service.prepay(payRequest).getH5Url();

        order.setPayUrl(h5Url);
        paymentOrderMapper.updateById(order);

        return PaymentProviderResponse.builder()
                .orderNo(orderNo)
                .payUrl(h5Url)
                .paymentStatus("pending")
                .amount(request.getAmount().toPlainString())
                .build();
    }

    @Override
    public PaymentProviderResponse queryPayment(String orderNo) {
        QueryOrderByOutTradeNoRequest queryRequest = new QueryOrderByOutTradeNoRequest();
        queryRequest.setMchid(wechatPayProperties.getMchId());
        queryRequest.setOutTradeNo(orderNo);

        Transaction transaction = nativePayService.queryOrderByOutTradeNo(queryRequest);

        if (Transaction.TradeStateEnum.SUCCESS.equals(transaction.getTradeState())) {
            PaymentOrder order = paymentOrderMapper.selectOne(
                    new LambdaQueryWrapper<PaymentOrder>().eq(PaymentOrder::getOrderNo, orderNo));
            if (order != null && "pending".equals(order.getStatus())) {
                order.setStatus("paid");
                order.setTransactionId(transaction.getTransactionId());
                order.setPaidTime(LocalDateTime.now());
                paymentOrderMapper.updateById(order);
            }
        }

        return PaymentProviderResponse.builder()
                .orderNo(orderNo)
                .transactionId(transaction.getTransactionId())
                .paymentStatus(convertTradeState(transaction.getTradeState()))
                .build();
    }

    @Override
    public String handleCallback(Map<String, String> params) {
        String signature = params.get("wechatpaySignature");
        String nonce = params.get("wechatpayNonce");
        String timestamp = params.get("wechatpayTimestamp");
        String serial = params.get("wechatpaySerial");
        String body = params.get("body");

        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(serial != null ? serial : "")
                .nonce(nonce != null ? nonce : "")
                .signature(signature != null ? signature : "")
                .timestamp(timestamp != null ? timestamp : "")
                .body(body)
                .build();

        Transaction transaction = notificationParser.parse(requestParam, Transaction.class);

        if (Transaction.TradeStateEnum.SUCCESS.equals(transaction.getTradeState())) {
            String orderNo = transaction.getOutTradeNo();
            PaymentOrder order = paymentOrderMapper.selectOne(
                    new LambdaQueryWrapper<PaymentOrder>().eq(PaymentOrder::getOrderNo, orderNo));
            if (order != null && "pending".equals(order.getStatus())) {
                order.setStatus("paid");
                order.setTransactionId(transaction.getTransactionId());
                order.setPaidTime(LocalDateTime.now());
                paymentOrderMapper.updateById(order);
                log.info("微信支付成功: orderNo={}, transactionId={}", orderNo, transaction.getTransactionId());
                return orderNo;
            }
        }

        return null;
    }

    @Override
    public void refund(String orderNo) {
        PaymentOrder order = paymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PaymentOrder>().eq(PaymentOrder::getOrderNo, orderNo));
        if (order == null || !"paid".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不允许退款");
        }

        CreateRequest refundRequest = new CreateRequest();
        refundRequest.setOutTradeNo(orderNo);
        refundRequest.setOutRefundNo("RF" + orderNo);
        refundRequest.setNotifyUrl(wechatPayProperties.getRefundNotifyUrl());
        AmountReq amount = new AmountReq();
        amount.setRefund(Long.parseLong(order.getAmount()));
        amount.setTotal(Long.parseLong(order.getAmount()));
        amount.setCurrency(order.getCurrency() != null ? order.getCurrency() : "CNY");
        refundRequest.setAmount(amount);

        refundService.create(refundRequest);

        order.setStatus("refunded");
        paymentOrderMapper.updateById(order);
        log.info("微信支付退款成功: orderNo={}", orderNo);
    }

    private String convertTradeState(Transaction.TradeStateEnum tradeState) {
        switch (tradeState) {
            case SUCCESS:
                return "paid";
            case CLOSED:
            case REVOKED:
                return "closed";
            default:
                return "pending";
        }
    }
}
