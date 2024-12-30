package io.polypen;

import io.polypen.parse.Parser;

import java.util.ArrayList;
import java.util.List;

public final class Polynomial {

    public static final Polynomial ZERO = new Polynomial(List.of(0));
    public static final Polynomial ONE = new Polynomial(List.of(1));

    private final List<Integer> coefficients;

    Polynomial(List<Integer> coefficients) {
        this.coefficients = coefficients;
    }

    public static Polynomial parse(String s) {
        return Parser.eval(Parser.parse(s));
    }

    public Polynomial add(Polynomial other) {
        int degree = Math.max(degree(), other.degree());
        List<Integer> r = new ArrayList<>(degree + 1);
        for (int i = 0; i <= degree; i++) {
            r.add(coefficient(i) + other.coefficient(i));
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
        List<Integer> newCoefficients = new ArrayList<>(coefficients.size());
        for (Integer coefficient : coefficients) {
            newCoefficients.add(coefficient * (factor));
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
            Integer coefficient = coefficients.get(i);
            if (coefficient == 0) {
                continue;
            }
            String plus = i == coefficients.size() - 1 && coefficient > 0 ?
                    "" :
                    firstCoefficient ? "" : "+ ";
            firstCoefficient = false;
            String prettySign = coefficient < 0 ? "- " : plus;
            if (i == 0) {
                result.add(prettySign + Math.abs(coefficient));
            } else {
                String factor = i == 1 ? "x" : "x^" + i;
                if (Math.abs(coefficient) == 1) {
                    result.add(prettySign + factor);
                } else {
                    result.add(prettySign + Math.abs(coefficient) + factor);
                }
            }
        }
        return String.join(" ", result);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof Polynomial p)) return false;
        int size = Math.min(coefficients.size(), p.coefficients.size());
        for (int i = 0; i < size; i++) {
            if (!coefficients.get(i).equals(p.coefficients.get(i))) {
                return false;
            }
        }
        for (int i = size; i < coefficients.size(); i++) {
            if (coefficients.get(i) != 0) {
                return false;
            }
        }
        for (int i = size; i < p.coefficients.size(); i++) {
            if (p.coefficients.get(i) != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return coefficients.hashCode();
    }

    public Integer coefficient(int i) {
        if (i >= coefficients.size()) {
            return 0;
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
