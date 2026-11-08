package com.chitcircle.payment.service;

import com.chitcircle.auction.domain.Auction;
import com.chitcircle.auction.repository.AuctionRepository;
import com.chitcircle.common.exception.BusinessException;
import com.chitcircle.common.exception.ResourceNotFoundException;
import com.chitcircle.common.util.MoneyUtils;
import com.chitcircle.group.domain.ChitGroup;
import com.chitcircle.group.repository.ChitGroupRepository;
import com.chitcircle.ledger.domain.TransactionDirection;
import com.chitcircle.ledger.domain.TransactionType;
import com.chitcircle.ledger.service.LedgerApplicationService;
import com.chitcircle.member.domain.ChitMember;
import com.chitcircle.member.domain.MemberStatus;
import com.chitcircle.member.repository.ChitMemberRepository;
import com.chitcircle.payment.domain.Payment;
import com.chitcircle.payment.domain.PaymentStatus;
import com.chitcircle.payment.dto.DueSummaryResponse;
import com.chitcircle.payment.dto.PaymentResponse;
import com.chitcircle.payment.dto.RecordPaymentRequest;
import com.chitcircle.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentApplicationService {

    private final PaymentRepository paymentRepository;
    private final ChitGroupRepository groupRepository;
    private final ChitMemberRepository memberRepository;
    private final AuctionRepository auctionRepository;
    private final LedgerApplicationService ledgerService;

    @Transactional
    public PaymentResponse recordPayment(RecordPaymentRequest request) {
        ChitGroup group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + request.getGroupId()));

        ChitMember member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + request.getMemberId()));

        if (!member.getGroup().getId().equals(group.getId())) {
            throw new BusinessException("Member does not belong to specified chit group");
        }

        String ref = request.getReferenceNumber();
        if (ref != null && !ref.isBlank() && paymentRepository.existsByReferenceNumber(ref)) {
            throw new BusinessException("Duplicate payment reference number: " + ref);
        }

        Payment payment = Payment.builder()
                .group(group)
                .member(member)
                .auctionMonth(request.getAuctionMonth())
                .amount(MoneyUtils.format(request.getAmount()))
                .paymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : Instant.now())
                .paymentMethod(request.getPaymentMethod())
                .referenceNumber(ref != null && !ref.isBlank() ? ref : "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .status(PaymentStatus.CONFIRMED)
                .build();

        Payment saved = paymentRepository.save(payment);

        // Record immutable ledger entry for contribution
        ledgerService.recordTransaction(
                group,
                member,
                null,
                TransactionType.CONTRIBUTION,
                saved.getAmount(),
                TransactionDirection.CREDIT,
                saved.getId()
        );

        return mapToResponse(saved);
    }

    @Transactional
    public PaymentResponse confirmPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));

        if (payment.getStatus() == PaymentStatus.CONFIRMED) {
            throw new BusinessException("Payment is already confirmed");
        }

        payment.setStatus(PaymentStatus.CONFIRMED);
        Payment saved = paymentRepository.save(payment);

        ledgerService.recordTransaction(
                payment.getGroup(),
                payment.getMember(),
                null,
                TransactionType.CONTRIBUTION,
                payment.getAmount(),
                TransactionDirection.CREDIT,
                payment.getId()
        );

        return mapToResponse(saved);
    }

    @Transactional
    public PaymentResponse reversePayment(UUID paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));

        if (payment.getStatus() == PaymentStatus.REVERSED) {
            throw new BusinessException("Payment is already reversed");
        }

        payment.setStatus(PaymentStatus.REVERSED);
        Payment saved = paymentRepository.save(payment);

        // Record reversal in ledger
        ledgerService.recordTransaction(
                payment.getGroup(),
                payment.getMember(),
                null,
                TransactionType.REVERSAL,
                payment.getAmount(),
                TransactionDirection.DEBIT,
                payment.getId()
        );

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> listPayments(UUID groupId, Integer month) {
        List<Payment> list;
        if (month != null) {
            list = paymentRepository.findByGroupIdAndAuctionMonth(groupId, month);
        } else if (groupId != null) {
            list = paymentRepository.findByGroupIdOrderByPaymentDateDesc(groupId);
        } else {
            list = paymentRepository.findAll();
        }
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DueSummaryResponse> calculatePendingDues(UUID groupId, int month) {
        ChitGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + groupId));

        List<ChitMember> activeMembers = memberRepository.findByGroupIdAndStatus(groupId, MemberStatus.ACTIVE);

        // Fetch auction for month to see if dividend is already determined
        Optional<Auction> auctionOpt = auctionRepository.findByGroupIdAndMonthNumber(groupId, month);
        BigDecimal dividend = MoneyUtils.ZERO;
        if (auctionOpt.isPresent() && auctionOpt.get().getDividendPerMember() != null) {
            dividend = auctionOpt.get().getDividendPerMember();
        }

        BigDecimal monthlyContribution = group.getMonthlyContribution();
        BigDecimal netPayable = monthlyContribution.subtract(dividend);
        if (netPayable.compareTo(BigDecimal.ZERO) < 0) {
            netPayable = MoneyUtils.ZERO;
        }

        List<DueSummaryResponse> dues = new ArrayList<>();
        for (ChitMember member : activeMembers) {
            List<Payment> payments = paymentRepository.findByMemberIdAndAuctionMonthAndStatus(
                    member.getId(), month, PaymentStatus.CONFIRMED
            );

            BigDecimal paid = payments.stream()
                    .map(Payment::getAmount)
                    .reduce(MoneyUtils.ZERO, BigDecimal::add);

            BigDecimal balance = netPayable.subtract(paid);
            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                balance = MoneyUtils.ZERO;
            }

            dues.add(DueSummaryResponse.builder()
                    .memberId(member.getId())
                    .memberName(member.getUser().getFullName())
                    .memberNumber(member.getMemberNumber())
                    .auctionMonth(month)
                    .monthlyContribution(monthlyContribution)
                    .dividendCredit(dividend)
                    .netPayable(netPayable)
                    .amountPaid(paid)
                    .balanceDue(balance)
                    .isPaid(balance.compareTo(BigDecimal.ZERO) == 0)
                    .build());
        }

        return dues;
    }

    public PaymentResponse mapToResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .groupId(p.getGroup().getId())
                .groupName(p.getGroup().getName())
                .memberId(p.getMember().getId())
                .memberName(p.getMember().getUser().getFullName())
                .memberNumber(p.getMember().getMemberNumber())
                .auctionMonth(p.getAuctionMonth())
                .amount(p.getAmount())
                .paymentDate(p.getPaymentDate())
                .paymentMethod(p.getPaymentMethod())
                .referenceNumber(p.getReferenceNumber())
                .status(p.getStatus().name())
                .build();
    }
}

