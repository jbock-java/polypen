package io.polypen.parse;

import io.polypen.Monomial;
import io.polypen.Polynomial;
import io.polypen.parse.Parser.Expr;
import io.polypen.parse.Parser.ListExpr;
import io.polypen.parse.Parser.NumberExpr;
import io.polypen.parse.Parser.VarExp;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.polypen.parse.Macro.applyStarMacro;
import static io.polypen.parse.Parser.MULT;
import static io.polypen.parse.Parser.PLUS;
import static io.polypen.parse.Parser.eval;
import static io.polypen.parse.Parser.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ParserTest {

    @Test
    void testParse() {
        ListExpr result = parse("(a_1^12 + b12^2) * 2");
        assertEquals(ListExpr.of(
                        ListExpr.of(VarExp.of("a_1", 12), PLUS, VarExp.of("b12", 2)), MULT, NumberExpr.of(2)),
                result);
    }

    @Test
    void starMacro1() {
        ListExpr result = parse("1 + 2 * 3");
        List<Expr> expanded = applyStarMacro(result);
        assertEquals(List.of(
                        NumberExpr.of(1), PLUS, ListExpr.of(NumberExpr.of(2), MULT, NumberExpr.of(3))),
                expanded);
    }

    @Test
    void starMacro5() {
        ListExpr result = parse("1 + 2 * 3 * 4");
        List<Expr> expanded = applyStarMacro(result);
        assertEquals(List.of(
                        NumberExpr.of(1), PLUS, ListExpr.of(NumberExpr.of(2), MULT, NumberExpr.of(3), MULT, NumberExpr.of(4))),
                expanded);
    }

    @Test
    void starMacro2() {
        ListExpr result = parse("1 + 2 * 3 + 4");
        List<Expr> expanded = applyStarMacro(result);
        assertEquals(List.of(
                        NumberExpr.of(1), PLUS, ListExpr.of(NumberExpr.of(2), MULT, NumberExpr.of(3)), PLUS, NumberExpr.of(4)),
                expanded);
    }

    @Test
    void starMacro3() {
        ListExpr result = parse("(1 + 2) * 3");
        List<Expr> expanded = applyStarMacro(result);
        assertEquals(
                List.of(
                        ListExpr.of(NumberExpr.of(1), PLUS, NumberExpr.of(2)),
                        MULT, NumberExpr.of(3)),
                expanded);
    }

    @Test
    void starMacro4() {
        ListExpr result = parse("1 * (2 + 3)");
        List<Expr> expanded = applyStarMacro(result);
        assertEquals(
                List.of(
                        NumberExpr.of(1), MULT,
                        ListExpr.of(NumberExpr.of(2), PLUS, NumberExpr.of(3))),
                expanded);
        Polynomial polynomial = eval(result);
        assertEquals(Monomial.constant(5).polynomial(), polynomial);
    }

    @Test
    void parseDifference() {
        assertEquals(eval(parse("2")),
                eval(parse("(a + 1) - (a - 1)")));
    }

    @Test
    void negativeLiteral() {
        assertEquals(eval(parse("-x - 1")),
                eval(parse("-(x + 1)")));
    }

    @Test
    void doubleNegative() {
        assertEquals(eval(parse("-x + 1")),
                eval(parse("-(x - 1)")));
    }
}
