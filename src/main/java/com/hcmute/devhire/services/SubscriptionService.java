package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.SubscriptionDTO;
import com.hcmute.devhire.DTOs.SubscriptionRequestDTO;
import com.hcmute.devhire.components.JwtUtil;
import com.hcmute.devhire.entities.Subscription;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.repositories.SubscriptionRepository;
import com.hcmute.devhire.repositories.UserRepository;
import com.hcmute.devhire.utils.Status;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService implements ISubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final IPaymentService paymentService;
    @Override
    public Subscription addSubscription(SubscriptionDTO subscriptionDTO) {
        Subscription newSubscription = new Subscription();
        newSubscription.setName(subscriptionDTO.getName());
        newSubscription.setBenefit(subscriptionDTO.getBenefit());
        newSubscription.setPrice(subscriptionDTO.getPrice());
        newSubscription.setDescription(subscriptionDTO.getDescription());
        newSubscription.setStatus(Status.ACTIVE);

        return subscriptionRepository.save(newSubscription);
    }

    @Override
    public Page<SubscriptionDTO> getAllSubscriptions(PageRequest pageRequest) {
        Page<Subscription> subscriptions = subscriptionRepository.findAll(pageRequest);
        return subscriptions.map(this::convertDTO);
    }

    @Override
    public Page<SubscriptionDTO> getActiveSubscriptions(PageRequest pageRequest) {
        Page<Subscription> subscriptions = subscriptionRepository.findByStatus(Status.ACTIVE, pageRequest);
        return subscriptions.map(this::convertDTO);
    }

    public SubscriptionDTO convertDTO(Subscription subscription) {
        SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
        subscriptionDTO.setId(subscription.getId());
        subscriptionDTO.setName(subscription.getName());
        subscriptionDTO.setBenefit(subscription.getBenefit());
        subscriptionDTO.setPrice(subscription.getPrice());
        subscriptionDTO.setDescription(subscription.getDescription());
        subscriptionDTO.setStatus(subscription.getStatus());
        return subscriptionDTO;
    }

    @Override
    public void deleteSubscription(Long id) {
        Subscription subscription = subscriptionRepository.findById(id).orElseThrow(() -> new RuntimeException("Subscription not found"));
        subscription.setStatus(Status.INACTIVE);
        subscriptionRepository.save(subscription);
    }

    @Override
    public void activeSubscription(Long id) {
        Subscription subscription = subscriptionRepository.findById(id).orElseThrow(() -> new RuntimeException("Subscription not found"));
        subscription.setStatus(Status.ACTIVE);
        subscriptionRepository.save(subscription);
    }

    @Override
    public boolean isSubscriptionExist(String name) {
        Optional<Subscription> subscription = subscriptionRepository.findByName(name);
        return subscription.isPresent();
    }

    @Transactional
    public String purchaseSubscription(SubscriptionRequestDTO subscriptionRequestDTO, HttpServletRequest request) {
        // Find the subscription
        Subscription subscription = subscriptionRepository.findById(subscriptionRequestDTO.getSubscriptionId())
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        // Find the user
        String username = JwtUtil.getAuthenticatedUsername();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole().getId() != 2 ) {
            throw new RuntimeException("Only recruiter can purchase subscription");
        }

        return paymentService.processPayment(subscription, subscriptionRequestDTO.getPaymentMethod(), request);
    }
}
