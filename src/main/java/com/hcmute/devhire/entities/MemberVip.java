package com.hcmute.devhire.entities;

import com.hcmute.devhire.Utils.Status;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "member_vip")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberVip extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private Status status;

    @Column(name = "sign_day")
    private Date signDay;

    @Column(name = "expire_day")
    private Date expireDay;

    @ManyToOne
    @JoinColumn(name = "subscription_id", referencedColumnName = "id")
    private Subscription subscription;
}
