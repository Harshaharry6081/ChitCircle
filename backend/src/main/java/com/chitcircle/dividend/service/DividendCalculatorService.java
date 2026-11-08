package com.chitcircle.dividend.service;

import com.chitcircle.common.util.MoneyUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class DividendCalculatorService {

    public record CalculationResult(
            BigDecimal monthlyPool,
            BigDecimal winningDiscount,
            BigDecimal commissionAmount,
            BigDecimal dividendPool,
            BigDecimal dividendPerMember,
            BigDecimal winnerPrize,
            BigDecimal totalDistributedDividend,
            BigDecimal roundingRemainder
    ) {}

    /**
     * Calculates the dividend and prize distribution for a given auction.
     *
     * @param monthlyPool Pool amount (P)
     * @param winningDiscount Winning bid discount (D)
     * @param commissionAmount Commission or deductions (C)
     * @param memberCount Total group members (N)
     * @return CalculationResult
     */
    public CalculationResult calculate(
            BigDecimal monthlyPool,
            BigDecimal winningDiscount,
            BigDecimal commissionAmount,
            int memberCount
    ) {
        if (memberCount <= 0) {
            throw new IllegalArgumentException("Member count must be positive");
        }

        BigDecimal p = MoneyUtils.format(monthlyPool);
        BigDecimal d = MoneyUtils.format(winningDiscount);
        BigDecimal c = commissionAmount != null ? MoneyUtils.format(commissionAmount) : MoneyUtils.ZERO;

        // Dividend Pool = D - C
        BigDecimal dividendPool = d.subtract(c);
        if (dividendPool.compareTo(BigDecimal.ZERO) < 0) {
            dividendPool = MoneyUtils.ZERO;
        }

        // Dividend Per Member = Dividend Pool / N (using HALF_UP rounding to 2 decimals)
        BigDecimal dividendPerMember = dividendPool.divide(
                BigDecimal.valueOf(memberCount),
                MoneyUtils.DEFAULT_SCALE,
                RoundingMode.HALF_UP
        );

        // Winner Prize = P - D
        BigDecimal winnerPrize = p.subtract(d);
        if (winnerPrize.compareTo(BigDecimal.ZERO) < 0) {
            winnerPrize = MoneyUtils.ZERO;
        }

        BigDecimal totalDistributed = dividendPerMember.multiply(BigDecimal.valueOf(memberCount));
        BigDecimal roundingRemainder = dividendPool.subtract(totalDistributed);

        return new CalculationResult(
                p,
                d,
                c,
                dividendPool,
                dividendPerMember,
                winnerPrize,
                totalDistributed,
                roundingRemainder
        );
    }
}

