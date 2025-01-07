package io.polypen;

import io.polypen.parse.Macro;
import io.polypen.parse.Parser;
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
        Parser.ListExpr p = Parser.parse("2x^6 - 4x^2 - 2x");
        System.out.println(p);
        List<Parser.Expr> minused = Macro.minusMacro(p).getExprs();
        System.out.println(minused);
        for (int i = 0; i < minused.size(); i++) {
            Parser.Expr expr = minused.get(i);
            System.out.println(i + ": " + expr);
        }
        Parser.Expr x = Macro.applyStarMacro(minused);
        System.out.println(x);
        for (int i = 0; i < x.getExprs().size(); i++) {
            Parser.Expr expr = x.getExprs().get(i);
            System.out.println(i + ": " + expr);
        }
        assertEquals(eval(p), new Monomial(2, 1).multiply(parse("x^5 - 2x - 1")));
    }

    @Test
    void monomialMultiplication2() {
        Parser.ListExpr p = Parser.parse("2x - 1");
        System.out.println(p);
        List<Parser.Expr> minused = Macro.minusMacro(p).getExprs();
        System.out.println(minused);
        for (int i = 0; i < minused.size(); i++) {
            Parser.Expr expr = minused.get(i);
            System.out.println(i + ": " + expr);
        }
        Parser.Expr x = Macro.applyStarMacro(minused);
        System.out.println(x);
        for (int i = 0; i < x.getExprs().size(); i++) {
            Parser.Expr expr = x.getExprs().get(i);
            System.out.println(i + ": " + expr);
        }
        System.out.println(eval(p));
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
