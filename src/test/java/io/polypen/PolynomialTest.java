package io.polypen;

import io.polypen.parse.Macro;
import io.polypen.parse.Parser;
import io.polypen.parse.Parser.ListToken;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.polypen.Polynomial.parse;
import static io.polypen.parse.Parser.eval;
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
        ListToken p = Parser.parse("2x^6 - 4x^2 - 2x");
        assertEquals(new Monomial(2, 1).multiply(parse("x^5 - 2x - 1")),
                eval(p));
    }

    @Test
    void monomialMultiplication2() {
        ListToken p = Parser.parse("2x - 1");
        List<Parser.Token> tokens = Macro.applyStarMacro(p).getExprs();
        for (int i = 0; i < tokens.size(); i++) {
            Parser.Token token = tokens.get(i);
            System.out.println(i + ": " + token);
        }
        assertEquals(new Monomial(2, 1).polynomial()
                        .add(new Monomial(-1, 0).polynomial()),
                eval(p));
    }

    @Test
    void monomialMultiplication3() {
        ListToken p = Parser.parse("2x^2 - x - 1");
        assertEquals(new Monomial(2, 2).polynomial()
                        .add(new Monomial(-1, 1).polynomial())
                        .add(new Monomial(-1, 0).polynomial()),
                eval(p));
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
