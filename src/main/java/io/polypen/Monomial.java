package io.polypen;

import java.util.ArrayList;
import java.util.List;

public record Monomial(int coefficient, int exponent) {

    public static final Monomial ZERO = new Monomial(0, 0);

    public Polynomial multiply(Polynomial p) {
        List<Integer> result = new ArrayList<>(p.degree() + exponent + 1);
        for (int i = 0; i < exponent; i++) {
            result.add(0);
        }
        for (int i = 0; i <= p.degree(); i++) {
            result.add(coefficient * (p.coefficient(i)));
        }
        return new Polynomial(result);
    }
}
