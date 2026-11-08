package com.chitcircle.ledger.service;

import com.chitcircle.auction.domain.Auction;
import com.chitcircle.common.exception.ResourceNotFoundException;
import com.chitcircle.common.util.MoneyUtils;
import com.chitcircle.group.domain.ChitGroup;
import com.chitcircle.ledger.domain.LedgerTransaction;
import com.chitcircle.ledger.domain.TransactionDirection;
import com.chitcircle.ledger.domain.TransactionType;
import com.chitcircle.ledger.dto.LedgerBalanceResponse;
import com.chitcircle.ledger.dto.LedgerTransactionResponse;
import com.chitcircle.ledger.repository.LedgerTransactionRepository;
import com.chitcircle.member.domain.ChitMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LedgerApplicationService {

    private final LedgerTransactionRepository ledgerRepository;

    @Transactional
    public LedgerTransaction recordTransaction(
            ChitGroup group,
            ChitMember member,
            Auction auction,
            TransactionType type,
            BigDecimal amount,
            TransactionDirection direction,
            UUID referenceId
    ) {
        LedgerTransaction tx = LedgerTransaction.builder()
                .group(group)
                .member(member)
                .auction(auction)
                .transactionType(type)
                .amount(MoneyUtils.format(amount))
                .direction(direction)
                .referenceId(referenceId)
                .build();

        return ledgerRepository.save(tx);
    }

    @Transactional(readOnly = true)
    public LedgerBalanceResponse getGroupLedger(UUID groupId) {
        List<LedgerTransaction> transactions = ledgerRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
        return calculateBalance(groupId, null, transactions);
    }

    @Transactional(readOnly = true)
    public LedgerBalanceResponse getMemberLedger(UUID memberId) {
        List<LedgerTransaction> transactions = ledgerRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
        UUID groupId = transactions.isEmpty() ? null : transactions.get(0).getGroup().getId();
        return calculateBalance(groupId, memberId, transactions);
    }

    @Transactional
    public LedgerTransaction reverseTransaction(UUID transactionId, String reason) {
        LedgerTransaction original = ledgerRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Ledger transaction not found: " + transactionId));

        TransactionDirection oppositeDirection = original.getDirection() == TransactionDirection.CREDIT
                ? TransactionDirection.DEBIT : TransactionDirection.CREDIT;

        return recordTransaction(
                original.getGroup(),
                original.getMember(),
                original.getAuction(),
                TransactionType.REVERSAL,
                original.getAmount(),
                oppositeDirection,
                original.getId()
        );
    }

    private LedgerBalanceResponse calculateBalance(
            UUID groupId,
            UUID memberId,
            List<LedgerTransaction> transactions
    ) {
        BigDecimal totalCredits = MoneyUtils.ZERO;
        BigDecimal totalDebits = MoneyUtils.ZERO;

        for (LedgerTransaction tx : transactions) {
            if (tx.getDirection() == TransactionDirection.CREDIT) {
                totalCredits = totalCredits.add(tx.getAmount());
            } else {
                totalDebits = totalDebits.add(tx.getAmount());
            }
        }

        BigDecimal netBalance = totalCredits.subtract(totalDebits);

        List<LedgerTransactionResponse> dtos = transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return LedgerBalanceResponse.builder()
                .groupId(groupId)
                .memberId(memberId)
                .totalCredits(MoneyUtils.format(totalCredits))
                .totalDebits(MoneyUtils.format(totalDebits))
                .netBalance(MoneyUtils.format(netBalance))
                .transactions(dtos)
                .build();
    }

    public LedgerTransactionResponse mapToResponse(LedgerTransaction tx) {
        return LedgerTransactionResponse.builder()
                .id(tx.getId())
                .groupId(tx.getGroup().getId())
                .groupName(tx.getGroup().getName())
                .memberId(tx.getMember() != null ? tx.getMember().getId() : null)
                .memberName(tx.getMember() != null && tx.getMember().getUser() != null ? tx.getMember().getUser().getFullName() : null)
                .memberNumber(tx.getMember() != null ? tx.getMember().getMemberNumber() : null)
                .auctionId(tx.getAuction() != null ? tx.getAuction().getId() : null)
                .transactionType(tx.getTransactionType().name())
                .amount(tx.getAmount())
                .direction(tx.getDirection().name())
                .referenceId(tx.getReferenceId())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}
