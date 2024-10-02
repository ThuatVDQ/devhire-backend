package com.hcmute.devhire.entities;

import com.hcmute.devhire.utils.Status;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Lob
    @Column(name = "introduction", columnDefinition = "TEXT")
    private String introduction;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "role_id", referencedColumnName = "id")
    private Role role;
}
