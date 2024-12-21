package io.polypen;

import org.apache.commons.numbers.fraction.Fraction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PolynomialTest {

    @Test
    void polynomialToString() {
        assertEquals("x^5 - x - 1", Polynomial.parse("-x + x^5 - 1").toString());
        assertEquals("2 x^6 - 4 x^2 - 2 x", Polynomial.parse("2x^6 - 4x^2 - 2x").toString());
    }

    @Test
    void add() {
        assertEquals(Polynomial.parse("2x + 3"), Polynomial.parse("x + 1").add("x + 2"));
    }

    @Test
    void polynomialEquals() {
        assertEquals(Polynomial.parse("x^5 - x - 1"), Polynomial.parse("-x + x^5 - 1"));
    }

    @Test
    void monomialMultiplication() {
        assertEquals(Polynomial.parse("2x^6 - 4x^2 - 2x"),
                new Monomial(Fraction.of(2), 1).multiply(Polynomial.parse("x^5 - 2x - 1")));
    }
}
