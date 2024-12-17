package io.polypen;

import org.apache.commons.numbers.fraction.Fraction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class Polynomial {
    private final List<Fraction> coefficients;

    private Polynomial(List<Fraction> coefficients) {
        this.coefficients = coefficients;
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
            String prettyCoefficient = coefficient.compareTo(Fraction.ZERO) < 0 ? "- " : plus;
            if (i == 0) {
                result.add(prettyCoefficient + coefficient.abs());
            } else {
                String exponent = i == 1 ? "x" : "x^" + i;
                if (coefficient.isOne()) {
                    result.add(plus + exponent);
                } else if (coefficient.equals(Fraction.ONE.negate())) {
                    result.add("- " + exponent);
                } else {
                    result.add(prettyCoefficient + coefficient.abs() + " " + "x^" + i);
                }
            }
        }
        return String.join(" ", result);
    }

    public static Polynomial parse(String s) {
        List<SignedToken> strings = split(s.trim());
        TreeMap<Integer, Fraction> cof = new TreeMap<>();
        for (SignedToken term : strings) {
            String[] tokens = term.token.split("[a-z]", 3);
            String coefficient = tokens[0].replace("*", "").trim();
            if (coefficient.isEmpty()) {
                coefficient = "1";
            }
            if (tokens.length == 1) {
                cof.put(0, Fraction.parse(coefficient).multiply(term.sign.factor));
            } else {
                String rawExponent = tokens[1].replace("^", "").trim();
                if (rawExponent.isEmpty()) {
                    cof.put(1, Fraction.parse(coefficient).multiply(term.sign.factor));
                } else {
                    int exponent = Integer.parseInt(rawExponent);
                    cof.put(exponent, Fraction.parse(coefficient).multiply(term.sign.factor));
                }
            }
        }
        Integer highestExponent = cof.lastKey();
        List<Fraction> result = new ArrayList<>(highestExponent);
        for (int i = 0; i <= highestExponent; i++) {
            result.add(Fraction.ZERO);
        }
        for (Map.Entry<Integer, Fraction> e : cof.entrySet()) {
            Integer exponent = e.getKey();
            Fraction coefficient = e.getValue();
            result.set(exponent, coefficient);
        }
        return new Polynomial(result);
    }

    enum Sign {
        PLUS(1), MINUS(-1);
        final int factor;

        Sign(int factor) {
            this.factor = factor;
        }
    }

    record SignedToken(Sign sign, String token) {
    }

    static List<SignedToken> split(String s) {
        List<SignedToken> result = new ArrayList<>();
        Sign sign = Sign.PLUS;
        int pos = -1;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '-') {
                result.add(new SignedToken(sign, s.substring(pos + 1, i).trim()));
                sign = Sign.MINUS;
                pos = i;
            } else if (s.charAt(i) == '+') {
                result.add(new SignedToken(sign, s.substring(pos + 1, i).trim()));
                sign = Sign.PLUS;
                pos = i;
            }
        }
        if (pos < s.length() - 1) {
            result.add(new SignedToken(sign, s.substring(pos + 1).trim()));
        }
        return result;
    }
}
