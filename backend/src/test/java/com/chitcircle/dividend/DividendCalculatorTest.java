package com.chitcircle.dividend;

import com.chitcircle.common.util.MoneyUtils;
import com.chitcircle.dividend.service.DividendCalculatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dividend Calculator Financial Tests")
class DividendCalculatorTest {

    private DividendCalculatorService calculatorService;

    @BeforeEach
    void setUp() {
        calculatorService = new DividendCalculatorService();
    }

    @Test
    @DisplayName("Should correctly calculate MVP financial example: P=2,00,000, D=40,000, N=20")
    void testMvpCalculationExample() {
        // Business example from Section 1.2
        BigDecimal monthlyPool = new BigDecimal("200000.00");
        BigDecimal winningDiscount = new BigDecimal("40000.00");
        BigDecimal commission = BigDecimal.ZERO;
        int memberCount = 20;

        var result = calculatorService.calculate(monthlyPool, winningDiscount, commission, memberCount);

        assertEquals(new BigDecimal("40000.00"), result.dividendPool());
        assertEquals(new BigDecimal("2000.00"), result.dividendPerMember());
        assertEquals(new BigDecimal("160000.00"), result.winnerPrize());
        assertEquals(new BigDecimal("40000.00"), result.totalDistributedDividend());
        assertEquals(MoneyUtils.ZERO, result.roundingRemainder());
    }

    @Test
    @DisplayName("Should correctly handle organizer commission deduction")
    void testCalculationWithCommission() {
        BigDecimal monthlyPool = new BigDecimal("200000.00");
        BigDecimal winningDiscount = new BigDecimal("50000.00");
        BigDecimal commission = new BigDecimal("5000.00"); // 5% or fixed
        int memberCount = 20;

        var result = calculatorService.calculate(monthlyPool, winningDiscount, commission, memberCount);

        // Dividend pool = 50,000 - 5,000 = 45,000
        assertEquals(new BigDecimal("45000.00"), result.dividendPool());
        // Dividend per member = 45,000 / 20 = 2,250
        assertEquals(new BigDecimal("2250.00"), result.dividendPerMember());
        // Winner prize = 200,000 - 50,000 = 150,000
        assertEquals(new BigDecimal("150000.00"), result.winnerPrize());
    }

    @Test
    @DisplayName("Should handle uneven division and capture fractional remainder")
    void testUnevenDivisionRounding() {
        BigDecimal monthlyPool = new BigDecimal("100000.00");
        BigDecimal winningDiscount = new BigDecimal("10000.00");
        BigDecimal commission = BigDecimal.ZERO;
        int memberCount = 3; // 10000 / 3 = 3333.33 each, 3333.33 * 3 = 9999.99, remainder = 0.01

        var result = calculatorService.calculate(monthlyPool, winningDiscount, commission, memberCount);

        assertEquals(new BigDecimal("3333.33"), result.dividendPerMember());
        assertEquals(new BigDecimal("9999.99"), result.totalDistributedDividend());
        assertEquals(new BigDecimal("0.01"), result.roundingRemainder());
        assertEquals(new BigDecimal("90000.00"), result.winnerPrize());
    }

    @Test
    @DisplayName("Should reject invalid member counts")
    void testInvalidMemberCount() {
        assertThrows(IllegalArgumentException.class, () ->
                calculatorService.calculate(new BigDecimal("100000.00"), new BigDecimal("10000.00"), BigDecimal.ZERO, 0)
        );
    }
}

