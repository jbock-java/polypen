package io.polypen;

import org.apache.commons.numbers.fraction.Fraction;

import java.util.ArrayList;
import java.util.List;

public record Monomial(Fraction coefficient, int exponent) {

    public static final Monomial ZERO = new Monomial(Fraction.ZERO, 0);

    public Polynomial multiply(Polynomial p) {
        List<Fraction> result = new ArrayList<>(p.degree() + exponent + 1);
        for (int i = 0; i < exponent; i++) {
            result.add(Fraction.ZERO);
        }
        for (int i = 0; i <= p.degree(); i++) {
            result.add(coefficient.multiply(p.coefficient(i)));
        }
        return new Polynomial(result);
    }
}
