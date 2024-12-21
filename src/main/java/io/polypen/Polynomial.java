package io.polypen;

import org.apache.commons.numbers.fraction.Fraction;

import java.util.ArrayList;
import java.util.List;

import static io.polypen.Util.isAbsoluteOne;

public final class Polynomial {

    public static final Polynomial ZERO = new Polynomial(List.of(Fraction.ZERO));

    private final List<Fraction> coefficients;

    Polynomial(List<Fraction> coefficients) {
        this.coefficients = coefficients;
    }

    public static Polynomial parse(String s) {
        return new Polynomial(Parser.parse(s));
    }

    public Polynomial add(Polynomial other) {
        int degree = Math.max(degree(), other.degree());
        List<Fraction> r = new ArrayList<>(degree + 1);
        for (int i = 0; i <= degree; i++) {
            r.add(coefficient(i).add(other.coefficient(i)));
        }
        return new Polynomial(r);
    }

    public Polynomial add(String s) {
        return add(parse(s));
    }

    public Polynomial multiply(Polynomial other) {
        Polynomial result = ZERO;
        for (int i = 0; i <= degree(); i++) {
            Monomial m = monomial(i);
            Polynomial p = m.multiply(other);
            result = result.add(p);
        }
        return result;
    }

    public Polynomial multiply(String s) {
        return multiply(parse(s));
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
                String factor = i == 1 ? "x" : "x^" + i;
                if (isAbsoluteOne(coefficient)) {
                    result.add(prettySign + factor);
                } else {
                    result.add(prettySign + coefficient.abs() + " " + factor);
                }
            }
        }
        return String.join(" ", result);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof Polynomial)) return false;
        return coefficients.equals(((Polynomial) o).coefficients);
    }

    @Override
    public int hashCode() {
        return coefficients.hashCode();
    }

    public Fraction coefficient(int i) {
        if (i >= coefficients.size()) {
            return Fraction.ZERO;
        }
        return coefficients.get(i);
    }

    public Monomial monomial(int i) {
        if (i >= coefficients.size()) {
            return Monomial.ZERO;
        }
        return new Monomial(coefficients.get(i), i);
    }

    public int degree() {
        return coefficients.size() - 1;
    }
}
