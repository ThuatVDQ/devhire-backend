package com.hcmute.devhire.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;


@Entity
@Table(name = "notification")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message")
    private String message;

    @Column(name = "send_at", nullable = false)
    @JsonProperty("send_at")
    private Instant sendAt;

    @Column(name = "is_read", nullable = false)
    @JsonProperty("is_read")
    private Boolean isRead = false;

    @Column(name = "target_url")
    private String targetUrl;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonBackReference
    private User user;
}
