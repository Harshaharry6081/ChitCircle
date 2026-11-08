package com.chitcircle.auction.service;

import com.chitcircle.auction.domain.Auction;
import com.chitcircle.auction.domain.AuctionStatus;
import com.chitcircle.auction.dto.AuctionResponse;
import com.chitcircle.auction.dto.AuctionResultResponse;
import com.chitcircle.auction.dto.CreateAuctionRequest;
import com.chitcircle.auction.repository.AuctionRepository;
import com.chitcircle.bid.domain.Bid;
import com.chitcircle.bid.domain.BidStatus;
import com.chitcircle.bid.dto.BidResponse;
import com.chitcircle.bid.repository.BidRepository;
import com.chitcircle.common.exception.BusinessException;
import com.chitcircle.common.exception.ResourceNotFoundException;
import com.chitcircle.common.util.MoneyUtils;
import com.chitcircle.dividend.service.DividendCalculatorService;
import com.chitcircle.group.domain.ChitGroup;
import com.chitcircle.group.domain.ChitGroupStatus;
import com.chitcircle.group.repository.ChitGroupRepository;
import com.chitcircle.member.domain.ChitMember;
import com.chitcircle.member.repository.ChitMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionApplicationService {

    private final AuctionRepository auctionRepository;
    private final ChitGroupRepository groupRepository;
    private final ChitMemberRepository memberRepository;
    private final BidRepository bidRepository;
    private final DividendCalculatorService dividendCalculatorService;
    private final AuctionSettlementService auctionSettlementService;

    @Value("${chitcircle.rules.defaultOrganizerCommissionPercent:0.0}")
    private double defaultOrganizerCommissionPercent;

    @Transactional
    public AuctionResponse createAuction(UUID groupId, CreateAuctionRequest request) {
        ChitGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + groupId));

        if (group.getStatus() == ChitGroupStatus.COMPLETED || group.getStatus() == ChitGroupStatus.CANCELLED) {
            throw new BusinessException("Cannot create auction for inactive group");
        }

        if (auctionRepository.findByGroupIdAndMonthNumber(groupId, request.getMonthNumber()).isPresent()) {
            throw new BusinessException("Auction for month " + request.getMonthNumber() + " already exists in this group");
        }

        // Monthly pool = monthlyContribution * totalMembers
        BigDecimal monthlyPool = group.getMonthlyContribution().multiply(BigDecimal.valueOf(group.getTotalMembers()));

        Auction auction = Auction.builder()
                .group(group)
                .monthNumber(request.getMonthNumber())
                .auctionDate(request.getAuctionDate())
                .totalPool(MoneyUtils.format(monthlyPool))
                .status(AuctionStatus.CREATED)
                .build();

        Auction saved = auctionRepository.save(auction);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AuctionResponse> listAuctions(UUID groupId) {
        return auctionRepository.findByGroupIdOrderByMonthNumberAsc(groupId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AuctionResponse getAuction(UUID auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction not found: " + auctionId));
        return mapToResponse(auction);
    }

    @Transactional
    public AuctionResponse openAuction(UUID auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction not found: " + auctionId));

        if (auction.getStatus() != AuctionStatus.CREATED) {
            throw new BusinessException("Only CREATED auctions can be opened. Current status: " + auction.getStatus());
        }

        auction.setStatus(AuctionStatus.OPEN);
        return mapToResponse(auctionRepository.save(auction));
    }

    @Transactional
    public AuctionResponse closeAuction(UUID auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction not found: " + auctionId));

        if (auction.getStatus() != AuctionStatus.OPEN) {
            throw new BusinessException("Only OPEN auctions can be closed. Current status: " + auction.getStatus());
        }

        auction.setStatus(AuctionStatus.CLOSED);
        return mapToResponse(auctionRepository.save(auction));
    }

    @Transactional
    public AuctionResultResponse declareWinner(UUID auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction not found: " + auctionId));

        if (auction.getStatus() != AuctionStatus.CLOSED && auction.getStatus() != AuctionStatus.OPEN) {
            throw new BusinessException("Auction must be CLOSED or OPEN to declare winner. Current: " + auction.getStatus());
        }

        List<Bid> bids = bidRepository.findByAuctionIdOrderByBidAmountDescSubmittedAtAsc(auctionId);
        if (bids.isEmpty()) {
            throw new BusinessException("No valid bids submitted for this auction. Cannot declare winner.");
        }

        // Winning bid is the highest discount bid (earliest submitted breaks tie)
        Bid winningBid = bids.get(0);
        ChitMember winner = winningBid.getMember();

        if (Boolean.TRUE.equals(winner.getPrizeReceived())) {
            throw new BusinessException("Winning bid member has already received a prize in this cycle");
        }

        // Calculate commission if any
        BigDecimal commission = MoneyUtils.ZERO;
        if (defaultOrganizerCommissionPercent > 0) {
            commission = winningBid.getBidAmount().multiply(BigDecimal.valueOf(defaultOrganizerCommissionPercent / 100.0));
        }

        // Calculate dividends and winner prize
        var result = dividendCalculatorService.calculate(
                auction.getTotalPool(),
                winningBid.getBidAmount(),
                commission,
                auction.getGroup().getTotalMembers()
        );

        // Update bid statuses
        winningBid.setStatus(BidStatus.WINNING);
        bidRepository.save(winningBid);

        for (int i = 1; i < bids.size(); i++) {
            Bid b = bids.get(i);
            b.setStatus(BidStatus.OUTBID);
            bidRepository.save(b);
        }

        auction.setWinnerMember(winner);
        auction.setWinningDiscount(result.winningDiscount());
        auction.setPrizeAmount(result.winnerPrize());
        auction.setDividendPerMember(result.dividendPerMember());
        auction.setStatus(AuctionStatus.WINNER_DECLARED);

        Auction saved = auctionRepository.save(auction);
        return mapToResultResponse(saved, bids, result.dividendPool());
    }

    @Transactional(readOnly = true)
    public AuctionResultResponse getAuctionResult(UUID auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction not found: " + auctionId));

        List<Bid> bids = bidRepository.findByAuctionIdOrderByBidAmountDescSubmittedAtAsc(auctionId);
        BigDecimal dividendPool = auction.getWinningDiscount() != null ? auction.getWinningDiscount() : MoneyUtils.ZERO;

        return mapToResultResponse(auction, bids, dividendPool);
    }

    @Transactional
    public AuctionResponse settleAuction(UUID auctionId) {
        Auction settled = auctionSettlementService.settleAuction(auctionId);
        return mapToResponse(settled);
    }

    public AuctionResponse mapToResponse(Auction auction) {
        return AuctionResponse.builder()
                .id(auction.getId())
                .groupId(auction.getGroup().getId())
                .groupName(auction.getGroup().getName())
                .monthNumber(auction.getMonthNumber())
                .auctionDate(auction.getAuctionDate())
                .totalPool(auction.getTotalPool())
                .winningDiscount(auction.getWinningDiscount())
                .prizeAmount(auction.getPrizeAmount())
                .dividendPerMember(auction.getDividendPerMember())
                .winnerMemberId(auction.getWinnerMember() != null ? auction.getWinnerMember().getId() : null)
                .winnerMemberName(auction.getWinnerMember() != null ? auction.getWinnerMember().getUser().getFullName() : null)
                .winnerMemberNumber(auction.getWinnerMember() != null ? auction.getWinnerMember().getMemberNumber() : null)
                .status(auction.getStatus().name())
                .build();
    }

    private AuctionResultResponse mapToResultResponse(Auction auction, List<Bid> bids, BigDecimal dividendPool) {
        List<BidResponse> bidResponses = bids.stream()
                .map(b -> BidResponse.builder()
                        .id(b.getId())
                        .auctionId(auction.getId())
                        .memberId(b.getMember().getId())
                        .memberName(b.getMember().getUser().getFullName())
                        .memberNumber(b.getMember().getMemberNumber())
                        .bidAmount(b.getBidAmount())
                        .status(b.getStatus().name())
                        .submittedAt(b.getSubmittedAt())
                        .isHighest(auction.getWinnerMember() != null && b.getMember().getId().equals(auction.getWinnerMember().getId()))
                        .build())
                .collect(Collectors.toList());

        return AuctionResultResponse.builder()
                .auctionId(auction.getId())
                .groupId(auction.getGroup().getId())
                .groupName(auction.getGroup().getName())
                .monthNumber(auction.getMonthNumber())
                .auctionDate(auction.getAuctionDate())
                .totalPool(auction.getTotalPool())
                .winningDiscount(auction.getWinningDiscount())
                .prizeAmount(auction.getPrizeAmount())
                .dividendPool(dividendPool)
                .dividendPerMember(auction.getDividendPerMember())
                .winnerMemberId(auction.getWinnerMember() != null ? auction.getWinnerMember().getId() : null)
                .winnerMemberName(auction.getWinnerMember() != null ? auction.getWinnerMember().getUser().getFullName() : null)
                .winnerMemberNumber(auction.getWinnerMember() != null ? auction.getWinnerMember().getMemberNumber() : null)
                .status(auction.getStatus().name())
                .bids(bidResponses)
                .build();
    }
}

