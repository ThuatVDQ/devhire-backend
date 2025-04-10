package com.hcmute.devhire.DTOs;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressDTO {
    private String country;
    private String city;
    private String district;
    private String street;
}
