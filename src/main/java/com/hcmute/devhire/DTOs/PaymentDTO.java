package com.hcmute.devhire.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PaymentDTO {
    @JsonProperty("amount")
    private Double amount; // Số tiền cần thanh toán

    @JsonProperty("bank_code")
    private String bankCode; // Mã ngân hàng

    @JsonProperty("subcription_id")
    private Long subcriptionId; // Mã gói subcription

    @JsonProperty("language")
    private String language; // Ngôn ngữ giao diện thanh toán (vd: "vn", "en")
}
