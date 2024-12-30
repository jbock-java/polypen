package io.polypen;

import java.util.ArrayList;
import java.util.List;

public record Monomial(int coefficient, int degree) {

    public static final Monomial ZERO = new Monomial(0, 0);

    public static Monomial constant(int n) {
        return new Monomial(n, 0);
    }

    public Polynomial multiply(Polynomial p) {
        List<Integer> result = new ArrayList<>(p.degree() + degree + 1);
        for (int i = 0; i < degree; i++) {
            result.add(0);
        }
        for (int i = 0; i <= p.degree(); i++) {
            result.add(coefficient * (p.coefficient(i)));
        }
        return new Polynomial(result);
    }

    public Polynomial polynomial() {
        List<Integer> coefficients = new ArrayList<>(degree + 1);
        for (int i = 0; i < degree; i++) {
            coefficients.add(0);
        }
        coefficients.add(coefficient);
        return new Polynomial(coefficients);
    }
}
