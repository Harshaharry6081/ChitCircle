package com.chitcircle.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtils {

    public static final int DEFAULT_SCALE = 2;
    public static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);

    private MoneyUtils() {
        // utility class
    }

    public static BigDecimal format(BigDecimal amount) {
        if (amount == null) {
            return ZERO;
        }
        return amount.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
    }

    public static BigDecimal format(double amount) {
        return BigDecimal.valueOf(amount).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
    }

    public static BigDecimal divide(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return ZERO;
        }
        return numerator.divide(denominator, DEFAULT_SCALE, DEFAULT_ROUNDING);
    }

    public static BigDecimal divide(BigDecimal numerator, int denominator) {
        return divide(numerator, BigDecimal.valueOf(denominator));
    }

    public static boolean isGreaterThan(BigDecimal a, BigDecimal b) {
        if (a == null) return false;
        if (b == null) return true;
        return a.compareTo(b) > 0;
    }

    public static boolean isGreaterThanOrEqual(BigDecimal a, BigDecimal b) {
        if (a == null) return false;
        if (b == null) return true;
        return a.compareTo(b) >= 0;
    }

    public static boolean isLessThan(BigDecimal a, BigDecimal b) {
        if (a == null) return true;
        if (b == null) return false;
        return a.compareTo(b) < 0;
    }

    public static boolean isLessThanOrEqual(BigDecimal a, BigDecimal b) {
        if (a == null) return true;
        if (b == null) return false;
        return a.compareTo(b) <= 0;
    }
}

