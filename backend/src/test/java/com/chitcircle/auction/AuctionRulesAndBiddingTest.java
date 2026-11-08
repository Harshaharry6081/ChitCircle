package com.chitcircle.auction;

import com.chitcircle.auction.domain.Auction;
import com.chitcircle.auction.domain.AuctionStatus;
import com.chitcircle.auction.dto.AuctionResultResponse;
import com.chitcircle.auction.repository.AuctionRepository;
import com.chitcircle.auction.service.AuctionApplicationService;
import com.chitcircle.bid.domain.Bid;
import com.chitcircle.bid.domain.BidStatus;
import com.chitcircle.bid.dto.PlaceBidRequest;
import com.chitcircle.bid.repository.BidRepository;
import com.chitcircle.bid.service.BidApplicationService;
import com.chitcircle.common.exception.BusinessException;
import com.chitcircle.dividend.service.DividendCalculatorService;
import com.chitcircle.group.domain.ChitGroup;
import com.chitcircle.group.domain.ChitGroupStatus;
import com.chitcircle.group.repository.ChitGroupRepository;
import com.chitcircle.identity.domain.User;
import com.chitcircle.member.domain.ChitMember;
import com.chitcircle.member.domain.MemberStatus;
import com.chitcircle.member.repository.ChitMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Auction Rules & Bidding Engine Tests")
class AuctionRulesAndBiddingTest {

    private Map<UUID, Auction> auctionStore = new HashMap<>();
    private Map<UUID, ChitMember> memberStore = new HashMap<>();
    private List<Bid> bidStore = new ArrayList<>();

    private AuctionRepository auctionRepository;
    private BidRepository bidRepository;
    private ChitMemberRepository memberRepository;
    private ChitGroupRepository groupRepository;

    private BidApplicationService bidService;
    private AuctionApplicationService auctionService;

    private ChitGroup group;
    private ChitMember member1;
    private ChitMember member2;
    private Auction auction;

    @BeforeEach
    void setUp() {
        auctionStore.clear();
        memberStore.clear();
        bidStore.clear();

        auctionRepository = (AuctionRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{AuctionRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        Auction a = (Auction) args[0];
                        if (a.getId() == null) a.setId(UUID.randomUUID());
                        auctionStore.put(a.getId(), a);
                        return a;
                    } else if ("findById".equals(name) || "findByIdForUpdate".equals(name)) {
                        return Optional.ofNullable(auctionStore.get(args[0]));
                    }
                    return null;
                }
        );

        memberRepository = (ChitMemberRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{ChitMemberRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        ChitMember m = (ChitMember) args[0];
                        if (m.getId() == null) m.setId(UUID.randomUUID());
                        memberStore.put(m.getId(), m);
                        return m;
                    } else if ("findById".equals(name)) {
                        return Optional.ofNullable(memberStore.get(args[0]));
                    }
                    return null;
                }
        );

        bidRepository = (BidRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{BidRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        Bid b = (Bid) args[0];
                        if (b.getId() == null) b.setId(UUID.randomUUID());
                        if (b.getSubmittedAt() == null) b.setSubmittedAt(Instant.now());
                        bidStore.add(b);
                        return b;
                    } else if ("findByAuctionIdOrderByBidAmountDescSubmittedAtAsc".equals(name)) {
                        UUID aId = (UUID) args[0];
                        return bidStore.stream()
                                .filter(b -> b.getAuction().getId().equals(aId))
                                .sorted(Comparator.comparing(Bid::getBidAmount).reversed()
                                        .thenComparing(Bid::getSubmittedAt))
                                .toList();
                    } else if ("findFirstByAuctionIdAndMemberId".equals(name)) {
                        UUID aId = (UUID) args[0];
                        UUID mId = (UUID) args[1];
                        return bidStore.stream()
                                .filter(b -> b.getAuction().getId().equals(aId) && b.getMember().getId().equals(mId))
                                .findFirst();
                    }
                    return null;
                }
        );

        groupRepository = (ChitGroupRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{ChitGroupRepository.class},
                (proxy, method, args) -> null
        );

        bidService = new BidApplicationService(bidRepository, auctionRepository, memberRepository);
        ReflectionTestUtils.setField(bidService, "minDiscountPercent", 5.0);
        ReflectionTestUtils.setField(bidService, "maxDiscountPercent", 40.0);

        DividendCalculatorService dividendService = new DividendCalculatorService();
        auctionService = new AuctionApplicationService(
                auctionRepository, groupRepository, memberRepository, bidRepository, dividendService, null
        );
        ReflectionTestUtils.setField(auctionService, "defaultOrganizerCommissionPercent", 0.0);

        group = ChitGroup.builder()
                .id(UUID.randomUUID())
                .name("Alpha Chit 2026")
                .monthlyContribution(new BigDecimal("10000.00"))
                .totalMembers(20)
                .durationMonths(20)
                .startDate(LocalDate.now())
                .status(ChitGroupStatus.ACTIVE)
                .build();

        User user1 = User.builder().id(UUID.randomUUID()).fullName("Kavita Rao").email("kavita@test.com").build();
        User user2 = User.builder().id(UUID.randomUUID()).fullName("Vijay Kumar").email("vijay@test.com").build();

        member1 = ChitMember.builder()
                .id(UUID.randomUUID())
                .group(group)
                .user(user1)
                .memberNumber(1)
                .status(MemberStatus.ACTIVE)
                .prizeReceived(false)
                .build();
        memberStore.put(member1.getId(), member1);

        member2 = ChitMember.builder()
                .id(UUID.randomUUID())
                .group(group)
                .user(user2)
                .memberNumber(2)
                .status(MemberStatus.ACTIVE)
                .prizeReceived(false)
                .build();
        memberStore.put(member2.getId(), member2);

        auction = Auction.builder()
                .id(UUID.randomUUID())
                .group(group)
                .monthNumber(1)
                .auctionDate(Instant.now())
                .totalPool(new BigDecimal("200000.00"))
                .status(AuctionStatus.OPEN)
                .build();
        auctionStore.put(auction.getId(), auction);
    }

    @Test
    @DisplayName("Should reject bid when auction is not OPEN")
    void testBidRejectedWhenNotOpen() {
        auction.setStatus(AuctionStatus.CREATED);

        PlaceBidRequest request = PlaceBidRequest.builder()
                .memberId(member1.getId())
                .bidAmount(new BigDecimal("25000.00"))
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () ->
                bidService.placeBid(auction.getId(), request, null)
        );

        assertTrue(ex.getMessage().contains("not open for bidding"));
    }

    @Test
    @DisplayName("Should reject bid when member already won a prize in this cycle")
    void testBidRejectedWhenPrizeAlreadyReceived() {
        member1.setPrizeReceived(true);

        PlaceBidRequest request = PlaceBidRequest.builder()
                .memberId(member1.getId())
                .bidAmount(new BigDecimal("25000.00"))
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () ->
                bidService.placeBid(auction.getId(), request, null)
        );

        assertTrue(ex.getMessage().contains("already received the chit prize"));
    }

    @Test
    @DisplayName("Should reject bid outside statutory min and max discount bounds")
    void testBidDiscountBounds() {
        // Min discount is 5% of 200,000 = 10,000. Submitting 5,000 should fail.
        PlaceBidRequest tooLow = PlaceBidRequest.builder()
                .memberId(member1.getId())
                .bidAmount(new BigDecimal("5000.00"))
                .build();

        BusinessException exLow = assertThrows(BusinessException.class, () ->
                bidService.placeBid(auction.getId(), tooLow, null)
        );
        assertTrue(exLow.getMessage().contains("below minimum allowed discount"));

        // Max discount is 40% of 200,000 = 80,000. Submitting 90,000 should fail.
        PlaceBidRequest tooHigh = PlaceBidRequest.builder()
                .memberId(member1.getId())
                .bidAmount(new BigDecimal("90000.00"))
                .build();

        BusinessException exHigh = assertThrows(BusinessException.class, () ->
                bidService.placeBid(auction.getId(), tooHigh, null)
        );
        assertTrue(exHigh.getMessage().contains("exceeds maximum statutory discount"));
    }

    @Test
    @DisplayName("Should declare highest discount bidder as winner")
    void testHighestDiscountBidderWins() {
        auction.setStatus(AuctionStatus.OPEN);

        // Member 1 bids 30,000
        bidService.placeBid(auction.getId(), PlaceBidRequest.builder().memberId(member1.getId()).bidAmount(new BigDecimal("30000.00")).build(), null);
        // Member 2 bids 40,000 (higher discount)
        bidService.placeBid(auction.getId(), PlaceBidRequest.builder().memberId(member2.getId()).bidAmount(new BigDecimal("40000.00")).build(), null);

        auction.setStatus(AuctionStatus.CLOSED);

        AuctionResultResponse result = auctionService.declareWinner(auction.getId());

        assertEquals(member2.getId(), result.getWinnerMemberId());
        assertEquals(new BigDecimal("40000.00"), result.getWinningDiscount());
        assertEquals(new BigDecimal("160000.00"), result.getPrizeAmount());
        assertEquals(new BigDecimal("2000.00"), result.getDividendPerMember());
        assertEquals(AuctionStatus.WINNER_DECLARED.name(), result.getStatus());
    }

    @Test
    @DisplayName("Tie breaker: When two bids have identical highest discount, earliest submission wins")
    void testTieBreakerEarliestBidWins() {
        auction.setStatus(AuctionStatus.OPEN);

        // Member 1 submits 40,000 first
        Bid b1 = Bid.builder()
                .auction(auction)
                .member(member1)
                .bidAmount(new BigDecimal("40000.00"))
                .submittedAt(Instant.now().minusSeconds(100))
                .status(BidStatus.VALID)
                .build();
        bidStore.add(b1);

        // Member 2 submits 40,000 later
        Bid b2 = Bid.builder()
                .auction(auction)
                .member(member2)
                .bidAmount(new BigDecimal("40000.00"))
                .submittedAt(Instant.now().minusSeconds(50))
                .status(BidStatus.VALID)
                .build();
        bidStore.add(b2);

        auction.setStatus(AuctionStatus.CLOSED);

        AuctionResultResponse result = auctionService.declareWinner(auction.getId());

        assertEquals(member1.getId(), result.getWinnerMemberId(), "Earlier bid should win in case of tie");
    }
}
