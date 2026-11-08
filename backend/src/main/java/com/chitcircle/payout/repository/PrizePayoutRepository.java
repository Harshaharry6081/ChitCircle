package com.chitcircle.payout.repository;

import com.chitcircle.payout.domain.PrizePayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrizePayoutRepository extends JpaRepository<PrizePayout, UUID> {
    Optional<PrizePayout> findByAuctionId(UUID auctionId);
}

