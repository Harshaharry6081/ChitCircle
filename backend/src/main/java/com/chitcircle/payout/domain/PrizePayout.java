package com.chitcircle.payout.domain;

import com.chitcircle.auction.domain.Auction;
import com.chitcircle.member.domain.ChitMember;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "prize_payouts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrizePayout {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auction_id", nullable = false, unique = true)
    private Auction auction;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "winner_member_id", nullable = false)
    private ChitMember winnerMember;

    @Column(name = "payout_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal payoutAmount;

    @Column(name = "payout_status", nullable = false)
    @Builder.Default
    private String payoutStatus = "APPROVED";

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "payment_reference")
    private String paymentReference;
}

