package io.polypen.parse;

import io.polypen.Monomial;
import io.polypen.Polynomial;
import org.junit.jupiter.api.Test;

import static io.polypen.parse.Macro.applyStarMacro;
import static io.polypen.parse.Parser.Token;
import static io.polypen.parse.Parser.ListToken;
import static io.polypen.parse.Parser.MULT;
import static io.polypen.parse.Parser.MultListToken;
import static io.polypen.parse.Parser.NumberToken;
import static io.polypen.parse.Parser.PLUS;
import static io.polypen.parse.Parser.PlusListToken;
import static io.polypen.parse.Parser.VarExp;
import static io.polypen.parse.Parser.eval;
import static io.polypen.parse.Parser.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ParserTest {

    @Test
    void testParse() {
        ListToken result = parse("(a_1^12 + b12^2) * 2");
        assertEquals(ListToken.of(
                        ListToken.of(VarExp.of("a_1", 12), PLUS, VarExp.of("b12", 2)), MULT, NumberToken.of(2)),
                result);
    }

    @Test
    void starMacro1() {
        ListToken result = parse("1 + 2 * 3");
        Token expanded = applyStarMacro(result.value());
        assertEquals(PlusListToken.of(
                        NumberToken.of(1), MultListToken.of(NumberToken.of(2), NumberToken.of(3))),
                expanded);
    }

    @Test
    void starMacro5() {
        ListToken result = parse("1 + 2 * 3 * 4");
        Token expanded = applyStarMacro(result.value());
        assertEquals(PlusListToken.of(
                        NumberToken.of(1), MultListToken.of(NumberToken.of(2), NumberToken.of(3), NumberToken.of(4))),
                expanded);
    }

    @Test
    void starMacro6() {
        ListToken result = parse("2 * 3 * 4");
        Token expanded = applyStarMacro(result.value());
        assertEquals(
                MultListToken.of(2, 3, 4),
                expanded);
    }

    @Test
    void starMacro2() {
        ListToken result = parse("1 + 2 * 3 + 4");
        Token expanded = applyStarMacro(result.value());
        assertEquals(PlusListToken.of(
                        NumberToken.of(1), MultListToken.of(2, 3), NumberToken.of(4)),
                expanded);
    }

    @Test
    void starMacro3() {
        ListToken result = parse("(1 + 2) * 3");
        Token expanded = applyStarMacro(result.value());
        assertEquals(
                MultListToken.of(
                        PlusListToken.of(NumberToken.of(1), NumberToken.of(2)), NumberToken.of(3)),
                expanded);
    }

    @Test
    void starMacro7() {
        ListToken result = parse("1 * 2");
        Token expanded = applyStarMacro(result.value());
        assertEquals(
                MultListToken.of(
                        NumberToken.of(1), NumberToken.of(2)),
                expanded);
    }

    @Test
    void starMacro8() {
        ListToken result = parse("-(x - 1)");
        Token expanded = applyStarMacro(result.getExprs());
        assertEquals(
                MultListToken.of(
                        NumberToken.of(-1),
                        PlusListToken.of(VarExp.of("x", 1),
                                MultListToken.of(-1, 1))),
                expanded);
    }

    @Test
    void starMacro4() {
        ListToken result = parse("1 * (2 + 3)");
        Token expanded = applyStarMacro(result.value());
        assertEquals(
                MultListToken.of(
                        NumberToken.of(1),
                        PlusListToken.of(NumberToken.of(2), NumberToken.of(3))),
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
