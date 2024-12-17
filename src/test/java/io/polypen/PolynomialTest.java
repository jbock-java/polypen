package io.polypen;

import org.junit.jupiter.api.Test;

class PolynomialTest {

    @Test
    void parse() {
        System.out.println(Polynomial.parse("x^5 - x - 1"));
    }
}
