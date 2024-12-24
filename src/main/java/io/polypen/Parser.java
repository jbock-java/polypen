package io.polypen;

import org.apache.commons.numbers.fraction.Fraction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

final class Parser {

    static List<Fraction> parsePolynomial(String s) {
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
        return result;
    }

    static List<String> parseProduct(String s) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int nestingLevel = -1;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '(' -> nestingLevel = Math.max(0, nestingLevel) + 1;
                case ')' -> {
                    nestingLevel--;
                    if (!sb.isEmpty() && nestingLevel == 0) {
                        result.add(sb.toString());
                        sb.setLength(0);
                    }
                    if (nestingLevel < 0) {
                        throw new IllegalStateException("Illegal nesting");
                    }
                }
                default -> {
                    if (nestingLevel != 0) {
                        sb.append(c);
                    }
                }
            }
        }
        if (Math.max(nestingLevel, 0) != 0) {
            throw new IllegalStateException("Illegal nesting");
        }
        if (!sb.isEmpty()) {
            result.add(sb.toString());
        }
        return result;
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

    private static List<SignedToken> split(String s) {
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
