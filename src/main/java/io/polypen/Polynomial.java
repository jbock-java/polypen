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
        for (int i = 0; i < coefficients.size(); i++) {
            Fraction coefficient = coefficients.get(i);
            if (coefficient.isZero()) {
                continue;
            }
            if (i == 0) {
                result.add(coefficient.toString());
            } else {
                String exponent = i == 1 ? "x" : "x^" + i;
                if (coefficient.isOne()) {
                    result.add(exponent);
                } else {
                    result.add(coefficient + " " + "x^" + i);
                }
            }
        }
        return String.join(" + ", result);
    }

    public static Polynomial parse(String s) {
        String[] strings = s.split("\\w*[+-]\\w*", 0);
        TreeMap<Integer, Fraction> cof = new TreeMap<>();
        for (String term : strings) {
            String[] tokens = term.split("[a-z]", 3);
            String coefficient = tokens[0].replace("*", "").trim();
            if (coefficient.isEmpty()) {
                coefficient = "1";
            }
            if (tokens.length == 1) {
                cof.put(0, Fraction.parse(coefficient));
            } else {
                String rawExponent = tokens[1].replace("^", "").trim();
                if (rawExponent.isEmpty()) {
                    cof.put(1, Fraction.parse(coefficient));
                } else {
                    int exponent = Integer.parseInt(rawExponent);
                    cof.put(exponent, Fraction.parse(coefficient));
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
}
