package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.PaymentDTO;
import com.hcmute.devhire.entities.MemberVip;
import com.hcmute.devhire.entities.Subscription;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.repositories.MemberVipRepository;
import com.hcmute.devhire.repositories.SubscriptionRepository;
import com.hcmute.devhire.repositories.UserRepository;
import com.hcmute.devhire.utils.Status;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final IVNPayService vnPayService;
    private final MemberVipRepository memberVipRepository;
    @Override
    public String processPayment(Subscription subscription, String paymentMethod, HttpServletRequest request) {
        if (paymentMethod.equals("VNPay")) {
            PaymentDTO paymentDTO = PaymentDTO.builder()
                    .amount(subscription.getPrice())
                    .subcriptionId(subscription.getId())
                    .language("vn")
                    .build();
            return vnPayService.createPaymentUrl(paymentDTO, request);
        }
        return null;
    }

    @Override
    @Transactional
    public void completePayment(String username, Long subscriptionId) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        // Calculate expiration date (e.g., 30 days)
        Date signDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(signDate);
        calendar.add(Calendar.DAY_OF_MONTH, 30);
        Date expireDate = calendar.getTime();

        // Create a new MemberVip record
        MemberVip memberVip = MemberVip.builder()
                .user(user)
                .subscription(subscription)
                .status(Status.ACTIVE)
                .signDay(signDate)
                .expireDay(expireDate)
                .build();

        memberVipRepository.save(memberVip);
    }
}
