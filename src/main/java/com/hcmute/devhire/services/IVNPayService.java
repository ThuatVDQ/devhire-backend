package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.PaymentDTO;
import com.hcmute.devhire.DTOs.PaymentQueryDTO;
import com.hcmute.devhire.DTOs.PaymentRefundDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public interface IVNPayService {
    String createPaymentUrl(PaymentDTO paymentRequest, HttpServletRequest request);
    String queryTransaction(PaymentQueryDTO paymentQueryDTO, HttpServletRequest request) throws IOException;
    String refundTransaction(PaymentRefundDTO refundDTO) throws IOException;
}
