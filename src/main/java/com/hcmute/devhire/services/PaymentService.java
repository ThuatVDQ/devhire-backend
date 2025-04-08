package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.PaymentDTO;
import com.hcmute.devhire.entities.*;
import com.hcmute.devhire.repositories.*;
import com.hcmute.devhire.utils.JobStatus;
import com.hcmute.devhire.utils.Status;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final IVNPayService vnPayService;
    private final MemberVipRepository memberVipRepository;
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;

    @Override
    public String processPayment(Subscription subscription, String paymentMethod, HttpServletRequest request) {
        if (paymentMethod.equals("VNPay")) {
            PaymentDTO paymentDTO = PaymentDTO.builder()
                    .amount(subscription.getPrice())
                    .subscriptionId(subscription.getId())
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

        Company company = companyRepository.findByUser(user.getUsername());
        List<Job> jobs = jobRepository.findByCompanyId(company.getId());

        for (Job job : jobs) {
            if (job.getStatus() == JobStatus.OPEN) {
                job.setStatus(JobStatus.HOT);
                job.setHighlightEndTime(LocalDateTime.now().plusDays(subscription.getHighlightDuration()));
            }
        }

        jobRepository.saveAll(jobs);
    }
}
