package com.chitcircle.dividend.repository;

import com.chitcircle.dividend.domain.DividendDistribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DividendDistributionRepository extends JpaRepository<DividendDistribution, UUID> {
    List<DividendDistribution> findByAuctionId(UUID auctionId);
    List<DividendDistribution> findByMemberId(UUID memberId);
    Optional<DividendDistribution> findByAuctionIdAndMemberId(UUID auctionId, UUID memberId);
}

