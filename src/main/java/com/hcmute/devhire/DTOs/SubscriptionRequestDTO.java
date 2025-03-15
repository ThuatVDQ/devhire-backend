package com.hcmute.devhire.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionRequestDTO {
    @JsonProperty("subscription_id")
    private Long subscriptionId;

    @JsonProperty("payment_method")
    private String paymentMethod;
}
