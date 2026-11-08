package com.chitcircle.payment.repository;

import com.chitcircle.payment.domain.Payment;
import com.chitcircle.payment.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByGroupIdOrderByPaymentDateDesc(UUID groupId);

    List<Payment> findByMemberIdOrderByPaymentDateDesc(UUID memberId);

    List<Payment> findByGroupIdAndAuctionMonth(UUID groupId, Integer auctionMonth);

    List<Payment> findByMemberIdAndAuctionMonthAndStatus(UUID memberId, Integer auctionMonth, PaymentStatus status);

    boolean existsByReferenceNumber(String referenceNumber);

    Optional<Payment> findByReferenceNumber(String referenceNumber);
}

