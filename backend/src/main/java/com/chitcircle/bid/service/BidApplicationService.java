package com.chitcircle.bid.service;

import com.chitcircle.auction.domain.Auction;
import com.chitcircle.auction.domain.AuctionStatus;
import com.chitcircle.auction.repository.AuctionRepository;
import com.chitcircle.bid.domain.Bid;
import com.chitcircle.bid.domain.BidStatus;
import com.chitcircle.bid.dto.BidResponse;
import com.chitcircle.bid.dto.PlaceBidRequest;
import com.chitcircle.bid.repository.BidRepository;
import com.chitcircle.common.exception.BusinessException;
import com.chitcircle.common.exception.ResourceNotFoundException;
import com.chitcircle.common.util.MoneyUtils;
import com.chitcircle.member.domain.ChitMember;
import com.chitcircle.member.domain.MemberStatus;
import com.chitcircle.member.repository.ChitMemberRepository;
import com.chitcircle.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BidApplicationService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final ChitMemberRepository memberRepository;

    @Value("${chitcircle.rules.minDiscountPercent:5.0}")
    private double minDiscountPercent;

    @Value("${chitcircle.rules.maxDiscountPercent:40.0}")
    private double maxDiscountPercent;

    @Transactional
    public BidResponse placeBid(UUID auctionId, PlaceBidRequest request, Authentication authentication) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction not found: " + auctionId));

        // Rule 5: Bidding must happen only when auction is OPEN
        if (auction.getStatus() != AuctionStatus.OPEN) {
            throw new BusinessException("Auction is not open for bidding. Current status: " + auction.getStatus());
        }

        // Determine member
        ChitMember member = resolveMember(auction, request.getMemberId(), authentication);

        // Rule 1: Only active group members can bid
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException("Only active members can place bids. Member status: " + member.getStatus());
        }

        // Rule 2: Members who already received the prize cannot win again in the cycle
        if (Boolean.TRUE.equals(member.getPrizeReceived())) {
            throw new BusinessException("Member " + member.getUser().getFullName() + " has already received the chit prize and is ineligible to bid.");
        }

        // Rule 3: A member cannot submit duplicate active bids for the same auction
        Optional<Bid> existingBid = bidRepository.findFirstByAuctionIdAndMemberId(auctionId, member.getId());
        if (existingBid.isPresent()) {
            throw new BusinessException("Member already submitted a bid for this auction. Duplicate active bids are not allowed.");
        }

        BigDecimal bidDiscount = MoneyUtils.format(request.getBidAmount());
        BigDecimal totalPool = auction.getTotalPool();

        // Rule 4: Bid must be within configured minimum and maximum discount
        BigDecimal minDiscount = totalPool.multiply(BigDecimal.valueOf(minDiscountPercent / 100.0));
        BigDecimal maxDiscount = totalPool.multiply(BigDecimal.valueOf(maxDiscountPercent / 100.0));

        if (MoneyUtils.isLessThan(bidDiscount, minDiscount)) {
            throw new BusinessException("Bid discount ₹" + bidDiscount + " is below minimum allowed discount ₹" + MoneyUtils.format(minDiscount));
        }

        if (MoneyUtils.isGreaterThan(bidDiscount, maxDiscount)) {
            throw new BusinessException("Bid discount ₹" + bidDiscount + " exceeds maximum statutory discount ₹" + MoneyUtils.format(maxDiscount));
        }

        Bid bid = Bid.builder()
                .auction(auction)
                .member(member)
                .bidAmount(bidDiscount)
                .status(BidStatus.VALID)
                .build();

        Bid saved = bidRepository.save(bid);
        return mapToResponse(saved, true);
    }

    @Transactional(readOnly = true)
    public List<BidResponse> listBids(UUID auctionId) {
        List<Bid> bids = bidRepository.findByAuctionIdOrderByBidAmountDescSubmittedAtAsc(auctionId);
        UUID highestBidId = bids.isEmpty() ? null : bids.get(0).getId();

        return bids.stream()
                .map(b -> mapToResponse(b, b.getId().equals(highestBidId)))
                .collect(Collectors.toList());
    }

    private ChitMember resolveMember(Auction auction, UUID requestedMemberId, Authentication authentication) {
        if (requestedMemberId != null) {
            ChitMember member = memberRepository.findById(requestedMemberId)
                    .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + requestedMemberId));
            if (!member.getGroup().getId().equals(auction.getGroup().getId())) {
                throw new BusinessException("Member does not belong to this auction's chit group");
            }
            return member;
        }

        // Resolve by authenticated user
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return memberRepository.findByGroupIdAndUserId(auction.getGroup().getId(), principal.getId())
                    .orElseThrow(() -> new BusinessException("Authenticated user is not an enrolled member of this chit group"));
        }

        throw new BusinessException("Member identification required to place bid");
    }

    public BidResponse mapToResponse(Bid bid, boolean isHighest) {
        return BidResponse.builder()
                .id(bid.getId())
                .auctionId(bid.getAuction().getId())
                .memberId(bid.getMember().getId())
                .memberName(bid.getMember().getUser().getFullName())
                .memberNumber(bid.getMember().getMemberNumber())
                .bidAmount(bid.getBidAmount())
                .status(bid.getStatus().name())
                .submittedAt(bid.getSubmittedAt())
                .isHighest(isHighest)
                .build();
    }
}

