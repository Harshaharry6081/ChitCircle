package com.chitcircle.auction.service;

import com.chitcircle.auction.domain.Auction;
import com.chitcircle.auction.domain.AuctionStatus;
import com.chitcircle.auction.repository.AuctionRepository;
import com.chitcircle.common.exception.BusinessException;
import com.chitcircle.common.exception.ResourceNotFoundException;
import com.chitcircle.dividend.domain.DividendDistribution;
import com.chitcircle.dividend.repository.DividendDistributionRepository;
import com.chitcircle.ledger.domain.TransactionDirection;
import com.chitcircle.ledger.domain.TransactionType;
import com.chitcircle.ledger.service.LedgerApplicationService;
import com.chitcircle.member.domain.ChitMember;
import com.chitcircle.member.domain.MemberStatus;
import com.chitcircle.member.repository.ChitMemberRepository;
import com.chitcircle.payout.domain.PrizePayout;
import com.chitcircle.payout.repository.PrizePayoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionSettlementService {

    private final AuctionRepository auctionRepository;
    private final ChitMemberRepository memberRepository;
    private final PrizePayoutRepository prizePayoutRepository;
    private final DividendDistributionRepository dividendDistributionRepository;
    private final LedgerApplicationService ledgerService;

    /**
     * Executes atomic settlement for a declared auction.
     * Guarded by pessimistic write lock and database transaction.
     */
    @Transactional
    public Auction settleAuction(UUID auctionId) {
        // 1. Acquire pessimistic lock on auction
        Auction auction = auctionRepository.findByIdForUpdate(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction not found: " + auctionId));

        // 2. Validate state machine
        if (auction.getStatus() == AuctionStatus.SETTLED) {
            throw new BusinessException("Auction is already settled");
        }
        if (auction.getStatus() != AuctionStatus.WINNER_DECLARED) {
            throw new BusinessException("Auction is not ready for settlement. Current status: " + auction.getStatus());
        }

        ChitMember winner = auction.getWinnerMember();
        if (winner == null) {
            throw new BusinessException("Cannot settle auction: Winner is not declared");
        }

        // 3. Create Prize Payout record (prevents duplicate via unique constraint)
        PrizePayout prizePayout = prizePayoutRepository.findByAuctionId(auctionId)
                .orElseGet(() -> prizePayoutRepository.save(
                        PrizePayout.builder()
                                .auction(auction)
                                .winnerMember(winner)
                                .payoutAmount(auction.getPrizeAmount())
                                .payoutStatus("APPROVED")
                                .paidAt(Instant.now())
                                .paymentReference("PAYOUT-M" + auction.getMonthNumber() + "-" + winner.getMemberNumber())
                                .build()
                ));

        // 4. Create Dividend Distributions for all active members
        List<ChitMember> activeMembers = memberRepository.findByGroupIdAndStatus(
                auction.getGroup().getId(), MemberStatus.ACTIVE
        );

        for (ChitMember member : activeMembers) {
            if (dividendDistributionRepository.findByAuctionIdAndMemberId(auctionId, member.getId()).isEmpty()) {
                dividendDistributionRepository.save(
                        DividendDistribution.builder()
                                .auction(auction)
                                .member(member)
                                .dividendAmount(auction.getDividendPerMember())
                                .status("DISTRIBUTED")
                                .build()
                );

                // Record member dividend credit in immutable ledger
                ledgerService.recordTransaction(
                        auction.getGroup(),
                        member,
                        auction,
                        TransactionType.DIVIDEND,
                        auction.getDividendPerMember(),
                        TransactionDirection.CREDIT,
                        auction.getId()
                );
            }
        }

        // 5. Record winner prize payout debit in ledger
        ledgerService.recordTransaction(
                auction.getGroup(),
                winner,
                auction,
                TransactionType.PRIZE_PAYOUT,
                auction.getPrizeAmount(),
                TransactionDirection.DEBIT,
                prizePayout.getId()
        );

        // 6. Update winner member prize_received flag
        winner.setPrizeReceived(true);
        memberRepository.save(winner);

        // 7. Update auction status to SETTLED
        auction.setStatus(AuctionStatus.SETTLED);
        Auction settled = auctionRepository.save(auction);

        log.info("Auction {} for group {} settled successfully. Winner: Member #{}",
                auction.getId(), auction.getGroup().getName(), winner.getMemberNumber());

        return settled;
    }
}

