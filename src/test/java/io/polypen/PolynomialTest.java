package io.polypen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PolynomialTest {

    @Test
    void polynomialToString() {
        assertEquals("x^5 - x - 1", Polynomial.parse("-x + x^5 - 1").toString());
    }

    @Test
    void add() {
        assertEquals(Polynomial.parse("2x + 3"), Polynomial.parse("x + 1").add("x + 2"));
    }

    @Test
    void polynomialEquals() {
        assertEquals(Polynomial.parse("x^5 - x - 1"), Polynomial.parse("-x + x^5 - 1"));
    }
}
