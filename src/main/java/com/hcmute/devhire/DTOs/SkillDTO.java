package com.hcmute.devhire.DTOs;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SkillDTO {
    private String name;
    private int frequency;
}
