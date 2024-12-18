package io.polypen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PolynomialTest {

    @Test
    void parse() {
        assertEquals("x^5 - x - 1", Polynomial.parse("-x + x^5 - 1").toString());
    }
}
