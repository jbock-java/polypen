package io.polypen;

import io.polypen.Expressions.Expression;
import org.apache.commons.numbers.fraction.Fraction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.polypen.Polynomial.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class PolynomialTest {

    @Test
    void polynomialToString() {
        assertEquals("x^5 - x - 1", parse("-x + x^5 - 1").toString());
        assertEquals("2 x^6 - 4 x^2 - 2 x", parse("2x^6 - 4x^2 - 2x").toString());
    }

    @Test
    void add() {
        assertEquals(parse("2x + 3"), parse("x + 1").add("x + 2"));
    }

    @Test
    void multiply() {
        assertEquals(parse("x^2 - 1"), parse("x - 1").multiply("x + 1"));
    }

    @Test
    void polynomialEquals() {
        assertEquals(parse("x^5 - x - 1"), parse("-x + x^5 - 1"));
    }

    @Test
    void monomialMultiplication() {
        assertEquals(parse("2x^6 - 4x^2 - 2x"),
                new Monomial(Fraction.of(2), 1).multiply(parse("x^5 - 2x - 1")));
    }

    @Test
    void parseProduct() {
        Expression expression = Parser.parse("(a + 1) * (a - 1)");
        assertInstanceOf(Expressions.Product.class, expression);
        Expressions.Product product = (Expressions.Product) expression;
        assertEquals(List.of("a + 1", "a - 1"), product.factors().stream().map(Parser.SignedString::token).toList());
    }
}
