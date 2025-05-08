package com.example.mutableDecimal;


import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


public class MutableDecimal implements Comparable<MutableDecimal> {

    private static final long HALF_LONG_MAX_VALUE = Long.MAX_VALUE / 2;
    private static final long HALF_LONG_MIN_VALUE = Long.MIN_VALUE / 2;
    private static final int MAX_DIGITS = 18;

    public static final MutableDecimal ZERO = new MutableDecimal("0");
    private long intCompact;
    private int scale;
    private int precision;
    private String stringCache;
    private BigDecimal bigDecimalCache;

    public MutableDecimal(long intCompact, int scale) {
        initializeByIntAndScale(intCompact, scale);
    }

    private void initializeReset() {
        stringCache = null;
        bigDecimalCache = null;
    }

    // reset to other numeral value
    public void reset(String val) {
        validateStringInput(val);
        initializeReset();
        initializeByString(val);
    }

    // reset to other numeral value
    public void reset(long intCompact, int scale) {
        initializeReset();
        initializeByIntAndScale(intCompact, scale);
    }

    private void initializeByIntAndScale(long intCompact, int scale) {
        // adjust to smallest scale
        while (scale > 0) {
            if (intCompact % 10 != 0) break;
            intCompact /= 10;
            scale--;
        }
        this.intCompact = intCompact;
        this.scale = scale;
    }

    private String removeTrailingZero(String inputString) {
        int trailingZeroStartIdx = inputString.length() - 1;
        for (; trailingZeroStartIdx >= 0; trailingZeroStartIdx--) {
            if (inputString.charAt(trailingZeroStartIdx) != '0') break;
        }
        return inputString.substring(0, trailingZeroStartIdx + 1);
    }

    private String removeLeadingZero(String inputString) {
        if ("0".equals(inputString)) return inputString;
        int leadingZeroIdx = 0;
        for (; leadingZeroIdx < inputString.length(); leadingZeroIdx++) {
            if (inputString.charAt(leadingZeroIdx) != '0') break;
        }
        return inputString.substring(leadingZeroIdx);
    }

    public MutableDecimal(String val) {
        validateStringInput(val);
        initializeByString(val);
    }

    private void validateStringInput(String input) {
        if (input == null)
            throw new NumberFormatException("No digits found.");
        input = input.trim();
        if (input.isEmpty())
            throw new NumberFormatException("No digits found.");
        StringBuilder sb = new StringBuilder("Invalid number. ");
        char[] inputCharArray = input.toCharArray();
        boolean hasDot = false;
        boolean isInputValid = true;
        List<Integer> invalidCharPositionList = new LinkedList<>();
        List<Integer> multipleDotPositionList = new LinkedList<>();

        for (int i = 0; i < inputCharArray.length; i++) {
            if (i > 0 && inputCharArray[i] == '-') {
                sb.append("\ninvalid negative sign at index: ").append(i);
                isInputValid = false;
                continue;
            }
            if (!Character.isDigit(inputCharArray[i]) && inputCharArray[i] != '.') {
                invalidCharPositionList.add(i);
            }
            if (inputCharArray[i] == '.') {
                if (!hasDot) hasDot = true;
                else multipleDotPositionList.add(i);
            }
        }

        isInputValid &= invalidCharPositionList.isEmpty();
        isInputValid &= multipleDotPositionList.isEmpty();
        if (!isInputValid) {
            if (!invalidCharPositionList.isEmpty()) {
                sb.append("\ninvalid character at index: ").append(invalidCharPositionList.stream().map(String::valueOf).collect(Collectors.joining(", ")));
            }

            if (!multipleDotPositionList.isEmpty()) {
                sb.append("\nmultiple decimal point at index: ").append(multipleDotPositionList.stream().map(String::valueOf).collect(Collectors.joining(", ")));
            }
            throw new NumberFormatException(sb.toString());
        }

//        String input validity check pass here
//        18 digit string can fit in intCompact (Java long)
        if (input.length() - (input.indexOf('-') > -1 ? 1 : 0) - (hasDot ? 1 : 0) > MAX_DIGITS)
            throw new NumberFormatException(String.format("Max length should not exceed %s. (actual: %s)", MAX_DIGITS, input.length()));
    }

    private long parseLong(String val) {
        long result = 0;
        var charArray = val.toCharArray();
        boolean isNegative = false;
        for (int i = 0; i < charArray.length; i++) {
            if (charArray[i] == '-') {
                isNegative = true;
                continue;
            } else if (charArray[i] == '+') {
                continue;
            }
            int digit = Character.digit(charArray[i], 10);
            result = result * 10 + digit;
        }
        return isNegative ? -result : result;
    }

    private void initializeByString(String val) {
        String[] numSplitByDot = val.split("\\.");
        switch (numSplitByDot.length) {
            case 1: {
                var integerPart = this.removeLeadingZero(numSplitByDot[0]);
                this.scale = 0;
                this.intCompact = parseLong(integerPart);
                break;
            }
            case 2: {
                var integerPart = this.removeLeadingZero(numSplitByDot[0]);
                var fractionalPart = this.removeTrailingZero(numSplitByDot[1]);
                this.intCompact = parseLong(integerPart + fractionalPart);
                this.scale = fractionalPart.length();
                break;
            }
            default: {
                throw new IllegalArgumentException(String.format("invalid input (multiple dot): %s", val));
            }
        }
    }

    public MutableDecimal divide(MutableDecimal divisor) {
        return this.divide(divisor, 2);
    }

    public MutableDecimal divide(MutableDecimal divisor, int scale) {
        return divide(this.intCompact, this.scale, divisor.intCompact, divisor.scale, scale);
    }

    private MutableDecimal divide(long dividend, int dividendScale, long divisor, int divisorScale, int scale) {
        if (scale + divisorScale > dividendScale) {
            int newScale = scale + divisorScale;
            int raise = newScale - dividendScale;
            long xs = Math.multiplyExact(dividend, (long)Math.pow(10, raise));
            return divideAndRound(xs, divisor, scale, scale);
        } else {
            int newScale = dividendScale - scale;
            int raise = newScale - divisorScale;
            long ys = Math.multiplyExact(divisor, (long)Math.pow(10, raise));
            return divideAndRound(dividend, ys, scale, scale);
        }
    }

    public static MutableDecimal valueOf(long unscaledVal, int scale) {
        return new MutableDecimal(unscaledVal, scale);
    }

    private MutableDecimal divideAndRound(long ldividend, long ldivisor, int scale,
                                          int preferredScale) {
        int qsign;
        long q = ldividend / ldivisor;
        long r = ldividend % ldivisor;
        qsign = ((ldividend < 0) == (ldivisor < 0)) ? 1 : -1;
        if (r != 0) {
            boolean increment = needIncrement(ldivisor, qsign, q, r);
            return valueOf((increment ? q + qsign : q), scale);
        } else {
            if (preferredScale != scale)
                return createAndStripZerosToMatchScale(q, scale, preferredScale);
            else
                return valueOf(q, scale);
        }
    }

    private static MutableDecimal createAndStripZerosToMatchScale(long compactVal, int scale, long preferredScale) {
        while (Math.abs(compactVal) >= 10L && scale > preferredScale) {
            if ((compactVal & 1L) != 0L)
                break; // odd number cannot end in 0
            long r = compactVal % 10L;
            if (r != 0L)
                break;
            compactVal /= 10;
            scale = checkScale(compactVal, (long) scale - 1); // could Overflow
        }
        return valueOf(compactVal, scale);
    }

    private static int checkScale(long intCompact, long val) {
        int asInt = (int) val;
        if (asInt != val) {
            asInt = val > Integer.MAX_VALUE ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            if (intCompact != 0)
                throw new ArithmeticException(asInt > 0 ? "Underflow" : "Overflow");
        }
        return asInt;
    }

    private static int longCompareMagnitude(long x, long y) {
        if (x < 0)
            x = -x;
        if (y < 0)
            y = -y;
        return Long.compare(x, y);
    }

    private static boolean commonNeedIncrement(int qsign,
                                               int cmpFracHalf, boolean oddQuot) {
        if (cmpFracHalf < 0) // We're closer to higher digit
            return false;
        else if (cmpFracHalf > 0) // We're closer to lower digit
            return true;
        else { // half-way
            return false;
        }
    }

    private static boolean needIncrement(long ldivisor,
                                         int qsign, long q, long r) {
        assert r != 0L;

        int cmpFracHalf;
        if (r <= HALF_LONG_MIN_VALUE || r > HALF_LONG_MAX_VALUE) {
            cmpFracHalf = 1; // 2 * r can't fit into long
        } else {
            cmpFracHalf = longCompareMagnitude(2 * r, ldivisor);
        }

        return commonNeedIncrement(qsign, cmpFracHalf, (q & 1L) != 0L);
    }

    private int checkScale(long val) {
        int asInt = (int) val;
        if (asInt != val) {
            asInt = val > Integer.MAX_VALUE ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            throw new ArithmeticException(asInt > 0 ? "Underflow" : "Overflow");
        }
        return asInt;
    }

    public MutableDecimal multiply(MutableDecimal multiplicand) {
        int productScale = checkScale((long) scale + multiplicand.scale);
        return multiply(this.intCompact, multiplicand.intCompact, productScale);
    }

    private static MutableDecimal multiply(long x, long y, int scale) {
        long product = multiply(x, y);
        return valueOf(product, scale);
    }

    private static long multiply(long x, long y) {
        return Math.multiplyExact(x, y);
    }

    public MutableDecimal add(MutableDecimal augend) {
        return add(this.intCompact, this.scale, augend.intCompact, augend.scale);
    }

    public MutableDecimal subtract(MutableDecimal subtrahend) {
        return add(this.intCompact, this.scale, -subtrahend.intCompact, subtrahend.scale);
    }

    private static MutableDecimal add(final long xs, int scale1, final long ys, int scale2) {
        long sdiff = (long) scale1 - scale2;
        if (sdiff == 0) {
            return add(xs, ys, scale1);
        } else if (sdiff < 0) {
            int raise = checkScale(xs, -sdiff);
            long scaledX = (long) (xs * Math.pow(10, raise));
            return add(scaledX, ys, scale2);
        } else {
            int raise = checkScale(ys, sdiff);
            long scaledY = (long) (ys * Math.pow(10, raise));
            return add(xs, scaledY, scale1);
        }
    }

    private static MutableDecimal add(long xs, long ys, int scale) {
        long sum = add(xs, ys);
        return valueOf(sum, scale);
    }

    private static long add(long xs, long ys) {
        return Math.addExact(xs, ys);
    }

    @Override
    public String toString() {
        if (stringCache == null) {
            if (this.scale == 0) return Long.toString(this.intCompact);
            StringBuilder s = new StringBuilder(Long.toString(this.intCompact));
            s.insert(s.length() - this.scale, ".");
            if (s.charAt(0) == '.') s.insert(0, "0");
            stringCache = String.format("%s", s);
        }
        return stringCache;
    }

    public BigDecimal toBigDecimal() {
        if (bigDecimalCache == null) {
            bigDecimalCache = BigDecimal.valueOf(this.intCompact, this.scale).stripTrailingZeros();
        }
        return bigDecimalCache;
    }

    @Override
    public boolean equals(Object x) {
        if (x instanceof BigDecimal xBigDecimal) {
            var self = this.toBigDecimal();
            return self.compareTo(xBigDecimal) == 0;
        }
        if (x == this)
            return true;
        if (x instanceof MutableDecimal xMutableDecimal) {
            return this.toBigDecimal().compareTo(xMutableDecimal.toBigDecimal()) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(intCompact);
        result = 31 * result + scale;
        return result;
    }

    @Override
    public int compareTo(MutableDecimal o) {
        return this.toBigDecimal().compareTo(o.toBigDecimal());
    }

    public int getScale() {
        return scale;
    }
}
