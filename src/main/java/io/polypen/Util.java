package io.polypen;

import org.apache.commons.numbers.fraction.Fraction;

final class Util {
    private static final Fraction NEGATIVE_ONE = Fraction.ONE.negate();

    static boolean isAbsoluteOne(Fraction fraction) {
        return fraction.isOne() || NEGATIVE_ONE.equals(fraction);
    }

    private Util() {
    }
}
