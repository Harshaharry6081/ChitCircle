package com.chitcircle.report.service;

import com.chitcircle.auction.domain.Auction;
import com.chitcircle.auction.domain.AuctionStatus;
import com.chitcircle.auction.repository.AuctionRepository;
import com.chitcircle.common.exception.ResourceNotFoundException;
import com.chitcircle.common.util.MoneyUtils;
import com.chitcircle.dividend.domain.DividendDistribution;
import com.chitcircle.dividend.repository.DividendDistributionRepository;
import com.chitcircle.group.domain.ChitGroup;
import com.chitcircle.group.repository.ChitGroupRepository;
import com.chitcircle.ledger.domain.LedgerTransaction;
import com.chitcircle.ledger.domain.TransactionDirection;
import com.chitcircle.ledger.domain.TransactionType;
import com.chitcircle.ledger.repository.LedgerTransactionRepository;
import com.chitcircle.ledger.service.LedgerApplicationService;
import com.chitcircle.member.domain.ChitMember;
import com.chitcircle.member.repository.ChitMemberRepository;
import com.chitcircle.payment.domain.Payment;
import com.chitcircle.payment.domain.PaymentStatus;
import com.chitcircle.payment.repository.PaymentRepository;
import com.chitcircle.payout.domain.PrizePayout;
import com.chitcircle.payout.repository.PrizePayoutRepository;
import com.chitcircle.report.dto.GroupSummaryReport;
import com.chitcircle.report.dto.MemberStatementReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportApplicationService {

    private final ChitGroupRepository groupRepository;
    private final ChitMemberRepository memberRepository;
    private final AuctionRepository auctionRepository;
    private final PaymentRepository paymentRepository;
    private final PrizePayoutRepository prizePayoutRepository;
    private final DividendDistributionRepository dividendDistributionRepository;
    private final LedgerTransactionRepository ledgerRepository;
    private final LedgerApplicationService ledgerService;

    @Transactional(readOnly = true)
    public GroupSummaryReport getGroupSummary(UUID groupId) {
        ChitGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + groupId));

        int memberCount = (int) memberRepository.countByGroupId(groupId);
        List<Auction> auctions = auctionRepository.findByGroupIdOrderByMonthNumberAsc(groupId);

        long conducted = auctions.stream().filter(a -> a.getStatus() == AuctionStatus.SETTLED).count();
        long remaining = Math.max(0, group.getDurationMonths() - conducted);

        List<Payment> payments = paymentRepository.findByGroupIdOrderByPaymentDateDesc(groupId);
        BigDecimal totalCollected = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.CONFIRMED)
                .map(Payment::getAmount)
                .reduce(MoneyUtils.ZERO, BigDecimal::add);

        BigDecimal totalDisbursed = auctions.stream()
                .filter(a -> a.getStatus() == AuctionStatus.SETTLED && a.getPrizeAmount() != null)
                .map(Auction::getPrizeAmount)
                .reduce(MoneyUtils.ZERO, BigDecimal::add);

        BigDecimal totalDividends = auctions.stream()
                .filter(a -> a.getStatus() == AuctionStatus.SETTLED && a.getWinningDiscount() != null)
                .map(Auction::getWinningDiscount)
                .reduce(MoneyUtils.ZERO, BigDecimal::add);

        return GroupSummaryReport.builder()
                .groupId(group.getId())
                .groupName(group.getName())
                .status(group.getStatus().name())
                .chitValue(group.getChitValue())
                .monthlyContribution(group.getMonthlyContribution())
                .totalMembers(group.getTotalMembers())
                .enrolledMembers(memberCount)
                .totalAuctionsConducted((int) conducted)
                .totalAuctionsRemaining((int) remaining)
                .totalPoolCollected(MoneyUtils.format(totalCollected))
                .totalPrizeDisbursed(MoneyUtils.format(totalDisbursed))
                .totalDividendsDistributed(MoneyUtils.format(totalDividends))
                .build();
    }

    @Transactional(readOnly = true)
    public MemberStatementReport getMemberStatement(UUID memberId) {
        ChitMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberId));

        List<Payment> payments = paymentRepository.findByMemberIdOrderByPaymentDateDesc(memberId);
        BigDecimal totalContributions = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.CONFIRMED)
                .map(Payment::getAmount)
                .reduce(MoneyUtils.ZERO, BigDecimal::add);

        List<DividendDistribution> dividends = dividendDistributionRepository.findByMemberId(memberId);
        BigDecimal totalDividendsEarned = dividends.stream()
                .map(DividendDistribution::getDividendAmount)
                .reduce(MoneyUtils.ZERO, BigDecimal::add);

        BigDecimal prizeAmountWon = MoneyUtils.ZERO;
        if (Boolean.TRUE.equals(member.getPrizeReceived())) {
            List<Auction> wonAuctions = auctionRepository.findByGroupIdOrderByMonthNumberAsc(member.getGroup().getId())
                    .stream()
                    .filter(a -> a.getWinnerMember() != null && a.getWinnerMember().getId().equals(memberId))
                    .collect(Collectors.toList());

            if (!wonAuctions.isEmpty() && wonAuctions.get(0).getPrizeAmount() != null) {
                prizeAmountWon = wonAuctions.get(0).getPrizeAmount();
            }
        }

        // Net position = Prize Won + Dividends - Total Contributions
        BigDecimal netPosition = prizeAmountWon.add(totalDividendsEarned).subtract(totalContributions);

        List<LedgerTransaction> transactions = ledgerRepository.findByMemberIdOrderByCreatedAtDesc(memberId);

        return MemberStatementReport.builder()
                .memberId(member.getId())
                .memberName(member.getUser().getFullName())
                .memberNumber(member.getMemberNumber())
                .groupId(member.getGroup().getId())
                .groupName(member.getGroup().getName())
                .prizeReceived(member.getPrizeReceived())
                .totalContributions(MoneyUtils.format(totalContributions))
                .totalDividendsEarned(MoneyUtils.format(totalDividendsEarned))
                .prizeAmountWon(MoneyUtils.format(prizeAmountWon))
                .netFinancialPosition(MoneyUtils.format(netPosition))
                .transactions(transactions.stream().map(ledgerService::mapToResponse).collect(Collectors.toList()))
                .build();
    }
}

