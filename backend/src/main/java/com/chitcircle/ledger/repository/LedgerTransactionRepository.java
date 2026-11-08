package com.chitcircle.ledger.repository;

import com.chitcircle.ledger.domain.LedgerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LedgerTransactionRepository extends JpaRepository<LedgerTransaction, UUID> {
    List<LedgerTransaction> findByGroupIdOrderByCreatedAtDesc(UUID groupId);

    List<LedgerTransaction> findByMemberIdOrderByCreatedAtDesc(UUID memberId);

    List<LedgerTransaction> findByAuctionIdOrderByCreatedAtDesc(UUID auctionId);

    @Query("SELECT l FROM LedgerTransaction l WHERE l.group.id = :groupId ORDER BY l.createdAt ASC")
    List<LedgerTransaction> findByGroupIdChronological(@Param("groupId") UUID groupId);
}

