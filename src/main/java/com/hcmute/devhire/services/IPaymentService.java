package com.hcmute.devhire.services;

import com.hcmute.devhire.entities.Subscription;
import com.hcmute.devhire.entities.User;
import jakarta.servlet.http.HttpServletRequest;

public interface IPaymentService {
    String processPayment(Subscription subscription, String paymentMethod, HttpServletRequest request);
    void completePayment(String username, Long subscriptionId);
}
