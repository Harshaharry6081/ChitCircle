package com.chitcircle.dividend.domain;

import com.chitcircle.auction.domain.Auction;
import com.chitcircle.member.domain.ChitMember;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "dividend_distributions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_auction_member", columnNames = {"auction_id", "member_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DividendDistribution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private ChitMember member;

    @Column(name = "dividend_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal dividendAmount;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "DISTRIBUTED";
}

