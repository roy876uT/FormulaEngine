package com.example.mutableDecimal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Stream;

public class MutableDecimalTest {

    @Test
    public void equalIgnoreScaleTest() {
        MutableDecimal mutableDecimal0 = new MutableDecimal("0.5000");
        MutableDecimal mutableDecimal1 = new MutableDecimal("0.5");
        Assertions.assertEquals(mutableDecimal1, mutableDecimal0);
        Assertions.assertEquals(0, mutableDecimal0.compareTo(mutableDecimal1));
    }

    @Test
    public void compareValueTest() {
        MutableDecimal mutableDecimal0_6 = new MutableDecimal("0.6");
        MutableDecimal mutableDecimal0_5 = new MutableDecimal("0.5");
        MutableDecimal mutableDecimal0_5a = new MutableDecimal("0.5");
        Assertions.assertEquals(1, mutableDecimal0_6.compareTo(mutableDecimal0_5));
        Assertions.assertEquals(-1, mutableDecimal0_5.compareTo(mutableDecimal0_6));
        Assertions.assertEquals(0, mutableDecimal0_5.compareTo(mutableDecimal0_5a));
    }

    //    Test on MutableDecimal's consistency on equal & compareTo
    @Test
    public void consistentEqualAndCompareToTest() {
        {
            Set<MutableDecimal> mutableDecimalSet = new HashSet<>();
            Stream.of("0.5000", "0.500000", "0.5").map(MutableDecimal::new).forEach(mutableDecimalSet::add);
            Assertions.assertIterableEquals(mutableDecimalSet, Set.of(new MutableDecimal("0.5")));
        }

        {
            Set<MutableDecimal> mutableDecimalSet = new TreeSet<>();
            Stream.of("0.5000", "0.500000", "0.5").map(MutableDecimal::new).forEach(mutableDecimalSet::add);
            Assertions.assertIterableEquals(mutableDecimalSet, Set.of(new MutableDecimal("0.5")));
        }

        {
            Set<BigDecimal> mutableDecimalSet = new HashSet<>();
            final List<String> DATA_LIST = List.of("0.5", "0.5000", "0.500000");
            DATA_LIST.stream().map(BigDecimal::new).forEach(mutableDecimalSet::add);
            Assertions.assertIterableEquals(
                    mutableDecimalSet.stream().map(BigDecimal::toString).sorted(Comparator.comparingInt(String::length)).toList()
                    , DATA_LIST);
        }

        {
            Set<BigDecimal> mutableDecimalSet = new TreeSet<>();
            final List<String> DATA_LIST = List.of("0.5", "0.5000", "0.500000");
            DATA_LIST.stream().map(BigDecimal::new).forEach(mutableDecimalSet::add);
            Assertions.assertIterableEquals(
                    mutableDecimalSet.stream().map(BigDecimal::toString).sorted(Comparator.comparingInt(String::length)).toList()
                    , List.of("0.5"));
        }
    }
    @Test
    public void toStringTest() {
        {
            String s0 = "1.23";
            MutableDecimal mutableDecimal0 = new MutableDecimal(s0);
            Assertions.assertEquals(s0, mutableDecimal0.toString());
            Assertions.assertEquals(mutableDecimal0, new BigDecimal(s0));
        }
        {
            MutableDecimal mutableDecimal0 = new MutableDecimal("0.23");
            Assertions.assertEquals("0.23", mutableDecimal0.toString());
        }
        {
            MutableDecimal mutableDecimal0 = new MutableDecimal("123");
            Assertions.assertEquals("123", mutableDecimal0.toString());
        }
        {
            MutableDecimal mutableDecimal0 = new MutableDecimal("123.987");
            Assertions.assertEquals("123.987", mutableDecimal0.toString());
        }
        {
            MutableDecimal mutableDecimal0 = new MutableDecimal("100.00");
            Assertions.assertEquals("100", mutableDecimal0.toString());
        }
        {
            MutableDecimal mutableDecimal0 = new MutableDecimal("100.30");
            Assertions.assertEquals("100.3", mutableDecimal0.toString());
        }
        {
            MutableDecimal mutableDecimal0 = new MutableDecimal(10000, 2);
            Assertions.assertEquals("100", mutableDecimal0.toString());
        }
        {
            MutableDecimal mutableDecimal0 = new MutableDecimal(10010, 2);
            Assertions.assertEquals("100.1", mutableDecimal0.toString());
        }
    }

    @Test
    public void mutabilityTest() {
        MutableDecimal mutableDecimal0 = new MutableDecimal(10010, 2);
        Assertions.assertEquals("100.1", mutableDecimal0.toString());

        mutableDecimal0.reset("32.987");
        Assertions.assertEquals("32.987", mutableDecimal0.toString());
    }

    @Test
    public void additionTest() {
        parallelAddWithBigDecimal2dp("1.11", "2.2");
        parallelAddWithBigDecimal2dp("100.1111", "2.2999");
        parallelAddWithBigDecimal2dp("100.1", "2.2999");
    }

    @Test
    public void SubtractionTest() {
        parallelSubtractWithBigDecimal2dp("1.11", "2.2");
        parallelSubtractWithBigDecimal2dp("100.1111", "2.2999");
    }

    @Test
    public void multiplyTest() {
        parallelMultiplyWithBigDecimal2dp("1.11", "2.2");
        parallelMultiplyWithBigDecimal2dp("100.1111", "2.2999");
    }

    private void parallelAddWithBigDecimal2dp(final String s0, final String s1) {
        final RoundingMode HALF_UP_ROUNDING = RoundingMode.HALF_UP;
        System.out.println("=====");
        System.out.printf("calculation: %s+%s%n", s0, s1);
        double d0 = Double.parseDouble(s0);
        double d1 = Double.parseDouble(s1);
        BigDecimal b0 = new BigDecimal(s0);
        BigDecimal b1 = new BigDecimal(s1);
        BigDecimal bigDecimalResult = b0.add(b1);
        System.out.printf("BigDecimal Result: %s%n", bigDecimalResult);

        BigDecimal bigDecimalResult1 = b0.add(b1).setScale(5, RoundingMode.HALF_UP);
        System.out.printf("BigDecimal (setScale) Result: %s%n", bigDecimalResult1.toPlainString());

        MutableDecimal mutableDecimal0 = new MutableDecimal(s0);
        MutableDecimal mutableDecimal1 = new MutableDecimal(s1);
        MutableDecimal mutableDecimalResult = mutableDecimal0.add(mutableDecimal1);
        System.out.printf("mutableDecimal Result: %s%n", mutableDecimalResult);

        double doubleResult = d0 + d1;
        final DecimalFormat df = new DecimalFormat("0" + (mutableDecimalResult.getScale() > 0 ? "." : "") + "#".repeat(mutableDecimalResult.getScale()));
        df.setRoundingMode(HALF_UP_ROUNDING);
        String doubleCalStringResult = df.format(doubleResult);
        Assertions.assertEquals(mutableDecimalResult, bigDecimalResult);
        Assertions.assertEquals(doubleCalStringResult, mutableDecimalResult.toString());
        System.out.printf("doubleCalString Result: %s%n", doubleCalStringResult);
        System.out.println();
    }

    private void parallelSubtractWithBigDecimal2dp(final String s0, final String s1) {
        final RoundingMode HALF_UP_ROUNDING = RoundingMode.HALF_UP;
        System.out.println("=====");
        System.out.printf("calculation: %s-%s%n", s0, s1);
        double d0 = Double.parseDouble(s0);
        double d1 = Double.parseDouble(s1);
        BigDecimal b0 = new BigDecimal(s0);
        BigDecimal b1 = new BigDecimal(s1);
        BigDecimal bigDecimalResult = b0.subtract(b1);
        System.out.printf("BigDecimal Result: %s%n", bigDecimalResult);
        MutableDecimal mutableDecimal0 = new MutableDecimal(s0);
        MutableDecimal mutableDecimal1 = new MutableDecimal(s1);
        MutableDecimal mutableDecimalResult = mutableDecimal0.subtract(mutableDecimal1);
        System.out.printf("mutableDecimal Result: %s%n", mutableDecimalResult);

        double doubleResult = d0 - d1;
        final DecimalFormat df = new DecimalFormat("0" + (mutableDecimalResult.getScale() > 0 ? "." : "") + "#".repeat(mutableDecimalResult.getScale()));
        df.setRoundingMode(HALF_UP_ROUNDING);
        String doubleCalStringResult = df.format(doubleResult);
        Assertions.assertEquals(mutableDecimalResult, bigDecimalResult);
        Assertions.assertEquals(doubleCalStringResult, mutableDecimalResult.toString());
        System.out.printf("doubleCalString Result: %s%n", doubleCalStringResult);
        System.out.println();
    }

    private void parallelMultiplyWithBigDecimal2dp(final String s0, final String s1) {
        final RoundingMode HALF_UP_ROUNDING = RoundingMode.HALF_UP;
        System.out.println("=====");
        System.out.printf("calculation: %s*%s%n", s0, s1);
        double d0 = Double.parseDouble(s0);
        double d1 = Double.parseDouble(s1);
        BigDecimal b0 = new BigDecimal(s0);
        BigDecimal b1 = new BigDecimal(s1);
        BigDecimal bigDecimalResult = b0.multiply(b1);
        System.out.printf("BigDecimal Result: %s%n", bigDecimalResult);
        MutableDecimal mutableDecimal0 = new MutableDecimal(s0);
        MutableDecimal mutableDecimal1 = new MutableDecimal(s1);
        MutableDecimal mutableDecimalResult = mutableDecimal0.multiply(mutableDecimal1);
        System.out.printf("mutableDecimal Result: %s%n", mutableDecimalResult);

        double doubleResult = d0 * d1;
        final DecimalFormat df = new DecimalFormat("0" + (mutableDecimalResult.getScale() > 0 ? "." : "") + "#".repeat(mutableDecimalResult.getScale()));
        df.setRoundingMode(HALF_UP_ROUNDING);
        String doubleCalStringResult = df.format(doubleResult);
        Assertions.assertEquals(mutableDecimalResult, bigDecimalResult);
        Assertions.assertEquals(doubleCalStringResult, mutableDecimalResult.toString());
        System.out.printf("doubleCalString Result: %s%n", doubleCalStringResult);
        System.out.println();
    }

    @Test
    public void division2DpTest() {
        this.parallelDivideWithBigDecimal2dp("1", "2");
        this.parallelDivideWithBigDecimal2dp("1", "2.2222");
        this.parallelDivideWithBigDecimal2dp("10000", "2");
        this.parallelDivideWithBigDecimal2dp("10000", "3");
        this.parallelDivideWithBigDecimal2dp("10000", "3.33");
        this.parallelDivideWithBigDecimal2dp("100.456789", "3");
        this.parallelDivideWithBigDecimal2dp("1.23", "4.3");
        this.parallelDivideWithBigDecimal2dp("500.98", "2.33");
        this.parallelDivideWithBigDecimal2dp("0.987", "0.111");
        this.parallelDivideWithBigDecimal2dp("1001", "2.365");
    }

    @Test
    public void division4dp() {
        this.parallelDivideWithBigDecimal("1", "2", 4);
        this.parallelDivideWithBigDecimal("100.456789", "3", 6);
        this.parallelDivideWithBigDecimal("100.456789", "3", 5);
    }

    private void parallelDivideWithBigDecimal2dp(final String s0, final String s1) {
        final RoundingMode HALF_UP_ROUNDING = RoundingMode.HALF_UP;
        System.out.println("=====");
        System.out.printf("calculation: %s/%s%n", s0, s1);
        double d0 = Double.parseDouble(s0);
        double d1 = Double.parseDouble(s1);
        BigDecimal b0 = new BigDecimal(s0);
        BigDecimal b1 = new BigDecimal(s1);
        BigDecimal bigDecimalResult = b0.divide(b1, 2, HALF_UP_ROUNDING);
        System.out.printf("BigDecimal Result: %s%n", bigDecimalResult);
        MutableDecimal mutableDecimal0 = new MutableDecimal(s0);
        MutableDecimal mutableDecimal1 = new MutableDecimal(s1);
        MutableDecimal mutableDecimalResult = mutableDecimal0.divide(mutableDecimal1);
        System.out.printf("mutableDecimal Result: %s%n", mutableDecimalResult);

        double doubleResult = d0 / d1;
        final DecimalFormat df = new DecimalFormat("0.##");
        df.setRoundingMode(HALF_UP_ROUNDING);
        String doubleCalStringResult = df.format(doubleResult);
        Assertions.assertEquals(mutableDecimalResult, bigDecimalResult);
        Assertions.assertEquals(doubleCalStringResult, mutableDecimalResult.toString());
        System.out.printf("doubleCalString Result: %s%n", doubleCalStringResult);
        System.out.println();
    }

    private void parallelDivideWithBigDecimal(final String s0, final String s1, int scale) {
        final RoundingMode HALF_UP_ROUNDING = RoundingMode.HALF_UP;
        System.out.println("=====");
        System.out.printf("calculation: %s/%s%n", s0, s1);
        double d0 = Double.parseDouble(s0);
        double d1 = Double.parseDouble(s1);
        BigDecimal b0 = new BigDecimal(s0);
        BigDecimal b1 = new BigDecimal(s1);
        BigDecimal bigDecimalResult = b0.divide(b1, scale, HALF_UP_ROUNDING);
        System.out.printf("BigDecimal Result: %s%n", bigDecimalResult);
        MutableDecimal mutableDecimal0 = new MutableDecimal(s0);
        MutableDecimal mutableDecimal1 = new MutableDecimal(s1);
        MutableDecimal mutableDecimalResult = mutableDecimal0.divide(mutableDecimal1, scale);
        System.out.printf("mutableDecimal Result: %s%n", mutableDecimalResult);

        double doubleResult = d0 / d1;
        StringBuffer sb = new StringBuffer("0");
        if (scale > 0) sb.append(".");
        sb.append("#".repeat(Math.max(0, scale)));
        final DecimalFormat df = new DecimalFormat(sb.toString());
        df.setRoundingMode(HALF_UP_ROUNDING);
        String doubleCalStringResult = df.format(doubleResult);
        Assertions.assertEquals(mutableDecimalResult, bigDecimalResult );
        Assertions.assertEquals(doubleCalStringResult, mutableDecimalResult.toString());
        System.out.printf("doubleCalString Result: %s%n", doubleCalStringResult);
        System.out.println();
    }

    @Test
    public void f2() {

    }

    @Test
    public void f3() {

    }


}
