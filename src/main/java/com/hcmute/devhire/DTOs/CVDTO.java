package com.hcmute.devhire.DTOs;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CVDTO {
    private Long id;

    private String cvUrl;

    private Long userId;
}
