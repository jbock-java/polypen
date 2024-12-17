package io.polypen;

import org.junit.jupiter.api.Test;

class PolynomialTest {

    @Test
    void parse() {
        System.out.println(Polynomial.parse("x^5 - x - 1"));
    }

    @Test
    void split() {
        System.out.println(Polynomial.split("1 + 2 - 3"));
    }
}
