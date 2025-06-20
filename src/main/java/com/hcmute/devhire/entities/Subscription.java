package com.hcmute.devhire.entities;

import com.hcmute.devhire.utils.Status;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "subscription")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Subscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "benefit")
    private String benefit;

    @Column(name = "price")
    private Double price;

    @Column(name="amount")
    private Integer amount;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private Status status;

    @Column(name = "highlight_duration")
    private Integer highlightDuration;

    @OneToMany(mappedBy = "subscription",cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MemberVip> memberVips;
}
