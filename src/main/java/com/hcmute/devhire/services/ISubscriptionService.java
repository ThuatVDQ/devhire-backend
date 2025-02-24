package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.SubscriptionDTO;
import com.hcmute.devhire.entities.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ISubscriptionService {
    Subscription addSubscription(SubscriptionDTO subscriptionDTO);
    Page<SubscriptionDTO> getAllSubscriptions(PageRequest pageRequest);
    void deleteSubscription(Long id);
    void activeSubscription(Long id);
    boolean isSubscriptionExist(String name);
}
