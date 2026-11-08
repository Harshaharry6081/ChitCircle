package com.chitcircle.member.domain;

import com.chitcircle.group.domain.ChitGroup;
import com.chitcircle.identity.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "chit_members",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_group_user", columnNames = {"group_id", "user_id"}),
                @UniqueConstraint(name = "uq_group_member_number", columnNames = {"group_id", "member_number"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChitMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private ChitGroup group;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "member_number", nullable = false)
    private Integer memberNumber;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column(name = "prize_received", nullable = false)
    @Builder.Default
    private Boolean prizeReceived = false;
}

