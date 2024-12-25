package io.polypen;

import java.util.List;

public class Expressions {
    public sealed interface Expression permits Product {
        String eval();
    }

    public record Product(List<String> factors) implements Expression {
        @Override
        public String eval() {
            List<Polynomial> polynomials = factors.stream().map(Polynomial::parse).toList();
            Polynomial result = Polynomial.ONE;
            for (Polynomial p : polynomials) {
                result = result.multiply(p);
            }
            return result.toString();
        }
    }
}
