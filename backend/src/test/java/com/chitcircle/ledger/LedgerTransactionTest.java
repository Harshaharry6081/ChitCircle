package com.chitcircle.ledger;

import com.chitcircle.auction.domain.Auction;
import com.chitcircle.group.domain.ChitGroup;
import com.chitcircle.identity.domain.User;
import com.chitcircle.ledger.domain.LedgerTransaction;
import com.chitcircle.ledger.domain.TransactionDirection;
import com.chitcircle.ledger.domain.TransactionType;
import com.chitcircle.ledger.dto.LedgerBalanceResponse;
import com.chitcircle.ledger.repository.LedgerTransactionRepository;
import com.chitcircle.ledger.service.LedgerApplicationService;
import com.chitcircle.member.domain.ChitMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.FluentQuery;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Financial Ledger Invariant & Calculation Tests")
class LedgerTransactionTest {

    private LedgerApplicationService ledgerService;
    private List<LedgerTransaction> inMemoryStore;
    private ChitGroup group;
    private ChitMember member;

    @BeforeEach
    void setUp() {
        inMemoryStore = new ArrayList<>();

        LedgerTransactionRepository repository = (LedgerTransactionRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{LedgerTransactionRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        LedgerTransaction tx = (LedgerTransaction) args[0];
                        if (tx.getId() == null) {
                            tx.setId(UUID.randomUUID());
                        }
                        if (tx.getCreatedAt() == null) {
                            tx.setCreatedAt(Instant.now());
                        }
                        inMemoryStore.add(tx);
                        return tx;
                    } else if ("findById".equals(name)) {
                        UUID id = (UUID) args[0];
                        return inMemoryStore.stream().filter(t -> t.getId().equals(id)).findFirst();
                    } else if ("findByMemberIdOrderByCreatedAtDesc".equals(name)) {
                        UUID memberId = (UUID) args[0];
                        return inMemoryStore.stream()
                                .filter(t -> t.getMember() != null && t.getMember().getId().equals(memberId))
                                .sorted(Comparator.comparing(LedgerTransaction::getCreatedAt).reversed())
                                .toList();
                    } else if ("findByGroupIdOrderByCreatedAtDesc".equals(name)) {
                        UUID groupId = (UUID) args[0];
                        return inMemoryStore.stream()
                                .filter(t -> t.getGroup().getId().equals(groupId))
                                .sorted(Comparator.comparing(LedgerTransaction::getCreatedAt).reversed())
                                .toList();
                    }
                    return null;
                }
        );

        ledgerService = new LedgerApplicationService(repository);

        group = ChitGroup.builder()
                .id(UUID.randomUUID())
                .name("Ledger Test Group")
                .monthlyContribution(new BigDecimal("10000.00"))
                .build();

        User user = User.builder().id(UUID.randomUUID()).fullName("Test User").email("test@example.com").build();

        member = ChitMember.builder()
                .id(UUID.randomUUID())
                .group(group)
                .user(user)
                .memberNumber(1)
                .build();
    }

    @Test
    @DisplayName("Should correctly calculate credits, debits and net balance from ledger stream")
    void testBalanceCalculation() {
        ledgerService.recordTransaction(
                group, member, null,
                TransactionType.CONTRIBUTION, new BigDecimal("10000.00"), TransactionDirection.CREDIT, UUID.randomUUID()
        );

        ledgerService.recordTransaction(
                group, member, null,
                TransactionType.DIVIDEND, new BigDecimal("2000.00"), TransactionDirection.CREDIT, UUID.randomUUID()
        );

        ledgerService.recordTransaction(
                group, member, null,
                TransactionType.PRIZE_PAYOUT, new BigDecimal("5000.00"), TransactionDirection.DEBIT, UUID.randomUUID()
        );

        LedgerBalanceResponse response = ledgerService.getMemberLedger(member.getId());

        // Total Credits = 10,000 + 2,000 = 12,000.00
        assertEquals(new BigDecimal("12000.00"), response.getTotalCredits());
        // Total Debits = 5,000.00
        assertEquals(new BigDecimal("5000.00"), response.getTotalDebits());
        // Net Balance = 12,000 - 5,000 = 7,000.00
        assertEquals(new BigDecimal("7000.00"), response.getNetBalance());
    }

    @Test
    @DisplayName("Should perform ledger reversal as an append-only mirror transaction")
    void testAppendOnlyReversal() {
        LedgerTransaction original = ledgerService.recordTransaction(
                group, member, null,
                TransactionType.CONTRIBUTION, new BigDecimal("10000.00"), TransactionDirection.CREDIT, UUID.randomUUID()
        );

        LedgerTransaction reversal = ledgerService.reverseTransaction(original.getId(), "Correction");

        assertNotNull(reversal);
        assertEquals(TransactionType.REVERSAL, reversal.getTransactionType());
        // Since original was CREDIT, reversal must be DEBIT
        assertEquals(TransactionDirection.DEBIT, reversal.getDirection());
        assertEquals(original.getAmount(), reversal.getAmount());
        assertEquals(original.getId(), reversal.getReferenceId(), "Reversal must explicitly reference the original transaction ID");

        // Verify balance after reversal is 0
        LedgerBalanceResponse balance = ledgerService.getMemberLedger(member.getId());
        assertEquals(new BigDecimal("0.00"), balance.getNetBalance());
    }
}
