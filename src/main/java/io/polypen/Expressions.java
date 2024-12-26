package io.polypen;

import io.polypen.Parser.SignedString;

import java.util.List;

public class Expressions {
    public sealed interface Expression permits Product, Sum {
        String eval();
    }

    public record Product(List<SignedString> factors) implements Expression {
        @Override
        public String eval() {
            List<Polynomial> polynomials = factors.stream().map(SignedString::token).map(Polynomial::parse).toList();
            Polynomial result = Polynomial.ONE;
            for (Polynomial p : polynomials) {
                result = result.multiply(p);
            }
            return result.toString();
        }
    }

    public record Sum(List<SignedString> factors) implements Expression {
        @Override
        public String eval() {
            List<Polynomial> polynomials = factors.stream().map(Polynomial::parse).toList();
            Polynomial result = Polynomial.ZERO;
            for (Polynomial p : polynomials) {
                result = result.add(p);
            }
            return result.toString();
        }
    }
}
