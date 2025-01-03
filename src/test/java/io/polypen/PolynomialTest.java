package io.polypen;

import org.junit.jupiter.api.Test;

import static io.polypen.Polynomial.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PolynomialTest {

    @Test
    void polynomialToString() {
        assertEquals("x^5 - x - 1", parse("-x + x^5 - 1").toString());
        assertEquals("2x^6 - 4x^2 - 2x", parse("2x^6 - 4x^2 - 2x").toString());
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
                new Monomial(2, 1).multiply(parse("x^5 - 2x - 1")));
    }

    @Test
    void negative() {
        Polynomial p = parse("-(x + 1)");
        assertEquals(parse("-x - 1"), p);
    }

    @Test
    void doubleNegative() {
        Polynomial p = parse("-(x - 1)");
        assertEquals(parse("-x + 1"), p);
    }
}
