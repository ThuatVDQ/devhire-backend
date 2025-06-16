package com.hcmute.devhire.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hcmute.devhire.utils.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionDTO {

    private Long id;

    private String name;

    private String benefit;

    private Double price;

    private String description;

    private Status status;

    private Integer amount;
}
