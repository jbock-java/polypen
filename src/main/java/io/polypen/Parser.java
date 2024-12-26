package io.polypen;

import io.polypen.Expressions.Expression;
import io.polypen.Expressions.Literal;
import io.polypen.Expressions.Product;
import io.polypen.Expressions.Sum;
import org.apache.commons.numbers.fraction.Fraction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static io.polypen.Parser.Sign.MINUS;
import static io.polypen.Parser.Sign.PLUS;
import static io.polypen.Parser.Type.PRODUCT;
import static io.polypen.Parser.Type.SUM;

final class Parser {

    static List<Fraction> parsePolynomial(String s) {
        List<SignedString> strings = split(s.trim());
        TreeMap<Integer, Fraction> cof = new TreeMap<>();
        for (SignedString term : strings) {
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

    static Expression parse(String s) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int nestingLevel = -1;
        Type outerop = PRODUCT;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '(' -> {
                    nestingLevel = Math.max(0, nestingLevel) + 1;
                    sb.append(c);
                }
                case ')' -> {
                    nestingLevel--;
                    sb.append(c);
                    if (nestingLevel == 0) {
                        result.add(sb.toString());
                        sb.setLength(0);
                    }
                    if (nestingLevel < 0) {
                        throw new IllegalStateException("Illegal nesting");
                    }
                }
                case '-', '+' -> {
                    if (nestingLevel == 0) {
                        outerop = SUM;
                    }
                    sb.append(c);
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
        if (result.size() == 1) {
            return new Literal(s);
        }
        return switch (outerop) {
            case PRODUCT -> new Product(result);
            case SUM -> new Sum(result);
        };
    }

    enum Type {
        SUM, PRODUCT
    }

    enum Sign {
        PLUS(1), MINUS(-1);
        final int factor;

        Sign(int factor) {
            this.factor = factor;
        }
    }

    public record SignedString(Sign sign, String token) {
    }

    private static List<SignedString> split(String s) {
        List<SignedString> result = new ArrayList<>();
        Sign sign = PLUS;
        int pos = -1;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '-') {
                result.add(new SignedString(sign, s.substring(pos + 1, i).trim()));
                sign = MINUS;
                pos = i;
            } else if (s.charAt(i) == '+') {
                result.add(new SignedString(sign, s.substring(pos + 1, i).trim()));
                sign = PLUS;
                pos = i;
            }
        }
        if (pos < s.length() - 1) {
            result.add(new SignedString(sign, s.substring(pos + 1).trim()));
        }
        return result;
    }
}
