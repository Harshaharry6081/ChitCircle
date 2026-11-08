package com.chitcircle.auction.domain;

import com.chitcircle.group.domain.ChitGroup;
import com.chitcircle.member.domain.ChitMember;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "auctions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_group_month", columnNames = {"group_id", "month_number"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private ChitGroup group;

    @Column(name = "month_number", nullable = false)
    private Integer monthNumber;

    @Column(name = "auction_date", nullable = false)
    private Instant auctionDate;

    @Column(name = "total_pool", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPool;

    @Column(name = "winning_discount", precision = 15, scale = 2)
    private BigDecimal winningDiscount;

    @Column(name = "prize_amount", precision = 15, scale = 2)
    private BigDecimal prizeAmount;

    @Column(name = "dividend_per_member", precision = 15, scale = 2)
    private BigDecimal dividendPerMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_member_id")
    private ChitMember winnerMember;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AuctionStatus status = AuctionStatus.CREATED;
}

