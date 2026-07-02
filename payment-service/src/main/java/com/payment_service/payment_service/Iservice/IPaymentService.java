package com.payment_service.payment_service.Iservice;

import com.payment_service.payment_service.dto.CreatePaymentRequest;
import com.payment_service.payment_service.dto.PaymentSessionResponse;

public interface IPaymentService {
    PaymentSessionResponse createPaymentLink(CreatePaymentRequest request);
    PaymentSessionResponse getPaymentByOrderCode(Long orderCode);
    void cancelPayment(Long orderCode);
}
