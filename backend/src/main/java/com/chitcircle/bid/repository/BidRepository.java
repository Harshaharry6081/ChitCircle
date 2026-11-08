package com.chitcircle.bid.repository;

import com.chitcircle.bid.domain.Bid;
import com.chitcircle.bid.domain.BidStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BidRepository extends JpaRepository<Bid, UUID> {
    List<Bid> findByAuctionIdOrderByBidAmountDescSubmittedAtAsc(UUID auctionId);

    List<Bid> findByAuctionIdAndStatusOrderByBidAmountDescSubmittedAtAsc(UUID auctionId, BidStatus status);

    Optional<Bid> findFirstByAuctionIdAndMemberId(UUID auctionId, UUID memberId);

    Optional<Bid> findFirstByAuctionIdAndStatusOrderByBidAmountDescSubmittedAtAsc(UUID auctionId, BidStatus status);
}

