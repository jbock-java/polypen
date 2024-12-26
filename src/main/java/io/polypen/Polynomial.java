package io.polypen;

import org.apache.commons.numbers.fraction.Fraction;

import java.util.ArrayList;
import java.util.List;

import static io.polypen.Util.isAbsoluteOne;

public final class Polynomial {

    public static final Polynomial ZERO = new Polynomial(List.of(Fraction.ZERO));
    public static final Polynomial ONE = new Polynomial(List.of(Fraction.ONE));

    private final List<Fraction> coefficients;

    Polynomial(List<Fraction> coefficients) {
        this.coefficients = coefficients;
    }

    public static Polynomial parse(String s) {
        NestingInfo nestingInfo = unnest(s.trim());
        return new Polynomial(Parser.parsePolynomial(nestingInfo.term)).multiply(nestingInfo.sign);
    }

    private static NestingInfo unnest(String s) {
        int nestingLevel = -1;
        int sign = 1;
        int start = 0;
        int end = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '(' -> {
                    nestingLevel = Math.max(nestingLevel, 0) + 1;
                }
                case ')' -> {
                    if (end == 0) {
                        end = i;
                    }
                    nestingLevel--;
                    if (nestingLevel < 0) {
                        throw new IllegalStateException("Illegal nesting");
                    }
                }
                case '-' -> {
                    if (start == 0) {
                        sign *= -1;
                    }
                }
                case '+', ' ' -> {
                    //ignore
                }
                default -> {
                    if (start == 0) {
                        start = i;
                    }
                }
            }
        }
        if (nestingLevel > 0) {
            throw new IllegalStateException("Illegal nesting");
        }
        if (nestingLevel == -1) {
            return new NestingInfo(1, s);
        }
        return new NestingInfo(sign, s.substring(start, end));
    }

    private record NestingInfo(int sign, String term) {
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

    public Polynomial multiply(int factor) {
        ArrayList<Fraction> newCoefficients = new ArrayList<>();
        for (Fraction coefficient : coefficients) {
            newCoefficients.add(coefficient.multiply(factor));
        }
        return new Polynomial(newCoefficients);
    }

    public Polynomial multiply(String s) {
        return multiply(parse(s));
    }

    @Override
    public String toString() {
        List<String> result = new ArrayList<>(coefficients.size());
        boolean firstCoefficient = true;
        for (int i = coefficients.size() - 1; i >= 0; i--) {
            Fraction coefficient = coefficients.get(i);
            if (coefficient.isZero()) {
                continue;
            }
            String plus = (i == coefficients.size() - 1 && coefficient.compareTo(Fraction.ZERO) > 0) ? "" : firstCoefficient ? "" : "+ ";
            firstCoefficient = false;
            String prettySign = coefficient.compareTo(Fraction.ZERO) < 0 ? "- " : plus;
            if (i == 0) {
                result.add(prettySign + coefficient.abs());
            } else {
                String factor = i == 1 ? "x" : "x^" + i;
                if (isAbsoluteOne(coefficient)) {
                    result.add(prettySign + factor);
                } else {
                    result.add(prettySign + coefficient.abs() + factor);
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
        Polynomial p = (Polynomial) o;
        int size = Math.min(coefficients.size(), p.coefficients.size());
        for (int i = 0; i < size; i++) {
            if (!coefficients.get(i).equals(p.coefficients.get(i))) {
                return false;
            }
        }
        for (int i = size; i < coefficients.size(); i++) {
            if (!coefficients.get(i).equals(Fraction.ZERO)) {
                return false;
            }
        }
        for (int i = size; i < p.coefficients.size(); i++) {
            if (!p.coefficients.get(i).equals(Fraction.ZERO)) {
                return false;
            }
        }
        return true;
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
