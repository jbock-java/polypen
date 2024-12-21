package io.polypen;

import org.apache.commons.numbers.fraction.Fraction;

import java.util.ArrayList;
import java.util.List;

import static io.polypen.Util.isAbsoluteOne;

public final class Polynomial {
    private final List<Fraction> coefficients;

    Polynomial(List<Fraction> coefficients) {
        this.coefficients = coefficients;
    }

    public static Polynomial parse(String s) {
        return new Polynomial(Parser.parse(s));
    }

    public Polynomial add(String s) {
        List<Fraction> other = Parser.parse(s);
        int rank = Math.max(coefficients.size(), other.size());
        List<Fraction> r = new ArrayList<>(rank);
        for (int i = 0; i < rank; i++) {
            r.add(coefficient(i).add(i < other.size() ? other.get(i) : Fraction.ZERO));
        }
        return new Polynomial(r);
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

    public int degree() {
        return coefficients.size() - 1;
    }
}
