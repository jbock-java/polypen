package io.polypen;

import org.apache.commons.numbers.fraction.Fraction;

import java.util.ArrayList;
import java.util.List;

import static io.polypen.Util.isAbsoluteOne;

public final class Polynomial {
    private final List<Fraction> coefficients;

    private Polynomial(List<Fraction> coefficients) {
        this.coefficients = coefficients;
    }

    public static Polynomial parse(String s) {
        return new Polynomial(Parser.parse(s));
    }

    @Override
    public String toString() {
        List<String> result = new ArrayList<>(coefficients.size());
        for (int i = coefficients.size() - 1; i >= 0; i--) {
            Fraction coefficient = coefficients.get(i);
            if (coefficient.isZero()) {
                continue;
            }
            String plus = (i == coefficients.size() - 1 && coefficient.compareTo(Fraction.ZERO) > 0) ? "" : "+ ";
            String prettySign = coefficient.compareTo(Fraction.ZERO) < 0 ? "- " : plus;
            if (i == 0) {
                result.add(prettySign + coefficient.abs());
            } else {
                String exponent = i == 1 ? "x" : "x^" + i;
                if (isAbsoluteOne(coefficient)) {
                    result.add(prettySign + exponent);
                } else {
                    result.add(prettySign + coefficient.abs() + " " + "x^" + i);
                }
            }
        }
        return String.join(" ", result);
    }
}
