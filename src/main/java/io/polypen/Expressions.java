package io.polypen;

import java.util.List;

public class Expressions {
    public sealed interface Expression permits Product, Sum, Literal {
        Polynomial eval();
    }

    public record Product(List<String> factors) implements Expression {
        @Override
        public Polynomial eval() {
            Polynomial result = Polynomial.ONE;
            for (String factor : factors) {
                Polynomial p = Parser.parse(factor).eval();
                result = result.multiply(p);
            }
            return result;
        }
    }

    public record Sum(List<String> terms) implements Expression {
        @Override
        public Polynomial eval() {
            Polynomial result = Polynomial.ZERO;
            for (String term : terms) {
                Polynomial p = Parser.parse(term).eval();
                result = result.add(p);
            }
            return result;
        }
    }

    public record Literal(String term) implements Expression {
        @Override
        public Polynomial eval() {
            return Polynomial.parse(term);
        }
    }
}
